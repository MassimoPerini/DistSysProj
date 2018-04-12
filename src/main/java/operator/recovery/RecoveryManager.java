package operator.recovery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import utils.Debug;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Created by higla on 24/02/2018.
 */
public class RecoveryManager {
	
	private String destinationFile;

	public RecoveryManager(String destinationFile)
	{
	    Debug.printVerbose("\n\n\nCreando il file...");
		this.destinationFile=destinationFile;
		this.createNewFile(this.destinationFile);
	}

	private void createNewFile(String destinationFile){

	    File f = new File(destinationFile);
        if(!f.exists())
            try {
                if(f.createNewFile())
                    Debug.printVerbose("File "+ destinationFile+" correctly created");
                else
                    Debug.printError("File "+ destinationFile+" not created");
            } catch (IOException e) {
                Debug.printError(e);
            }
        else
            Debug.printVerbose("File already exists");

    }

	public void appendData(@NotNull DataKey elem)
	{

		appendDataInFileList(destinationFile, elem);
	}

	
    /**
     * this method appends to an existing file, new Datas.
     * If the file doesn't exist it creates one. Note that if the file already exists (and it has old data),
     * the method will append datas anyway. So first it's better to use a safer method that creates a new file
     * anyway, then use this method.
     * This method also checks if the data is already there. If it is, data is not added.
     * @param fileName is the name of the File that will be used for the path
     * @param dataKey is the value to append to the file
     */
    private void appendDataInFileList(String fileName, DataKey dataKey){
        String s;
        fileName = this.destinationFile;
        StringBuilder toConvertFromJSON = new StringBuilder();
        try
        {
            FileReader fr=new FileReader(fileName);
            Debug.printDebug("Reading json from file: "+fileName);
            BufferedReader br=new BufferedReader(fr);

            while((s=br.readLine())!=null)
            {
                toConvertFromJSON.append(s);

            }
            Type type = new TypeToken<List<DataKey>>() {}.getType();
            List<DataKey> datas = new Gson().fromJson(toConvertFromJSON.toString(), type);
            if(datas != null) {
                //i check if dataKey isn't already in the file
                for (DataKey data : datas) {
                    //TODO: do we want to handle diffrently?
                    if(data.equals(dataKey)){
                        return;
                    }

                }
                datas.add(dataKey);
            }
            else{
                datas = new ArrayList<>();
                datas.add(dataKey);
            }
            try{

           //     Path filePath = Paths.get(fileName);
                FileWriter writer=new FileWriter(fileName);

             //   BufferedWriter bufferedWriter= Files.newBufferedWriter(filePath, TRUNCATE_EXISTING);
                writer.write( new GsonBuilder().setPrettyPrinting().create().toJson(datas, type));
                Debug.printVerbose("Data read : " + datas.toString());
                writer.close();

            }
            catch(FileNotFoundException e)
            {
                Debug.printError("File was not found while trying to add data! - appendDataInFileList()");
            }
            catch(IOException e)
            {
                Debug.printError("An error has occured while extrecting data from file! (IOException)- appendDataInFileList()");
            }

            br.close();
        }
        catch(FileNotFoundException e)
        {
            Debug.printError("Error1, file not found while appending data! This file "+ fileName.toString()
            + " was not found");
        }
        catch(IOException e)
        {
            Debug.printError("Error2, IO exception! From this file " + fileName.toString());
        }
    }

    /**
     * This method draws a value from a file and returns it. It also overwrites the file with the new list.
     * @param fileName is the name of the File that will be used for the path
     * @return the value drawn from the list.
     */
    public DataKey drawDataFromFileList(String fileName){
        DataKey value = null;


            Type type = new TypeToken<List<DataKey>>() {}.getType();
            List<DataKey> datas = getAll();
            if(datas != null) {
                value = datas.get(0);
                datas.remove(value);
            }
            try{
                Path filePath = Paths.get(fileName);
                BufferedWriter bw= Files.newBufferedWriter(filePath, TRUNCATE_EXISTING);
                bw.write( new GsonBuilder().setPrettyPrinting().create().toJson(datas, type));
                Debug.printVerbose(datas.toString());
                bw.close();
            }
            catch(FileNotFoundException e)
            {
                Debug.printError("File was not found while reading it! - drawDataFromFileList()");
            }
            catch(IOException e)
            {
                Debug.printError("No value found! (IOException) - drawDataFromFileList()");
            }

            return value;

    }

    public void removeDataFromList(DataKey dataToCheck){

            List<DataKey> datas = getAll();
            if(datas != null) {
                //i look for the data
                List<DataKey> toRemove;
                toRemove=datas.stream().filter(dat->dat.checkSameData(dataToCheck)).collect(Collectors.toList());
                datas.removeAll(toRemove);
            }
            store(datas);

    }

    /**
     * Save to file the given list of elements
     * @param toStore
     */
    public synchronized void store(List<DataKey> toStore)
    {
        Type type = new TypeToken<List<DataKey>>() {}.getType();

        try{
            Path filePath = Paths.get(destinationFile);
            BufferedWriter bw= Files.newBufferedWriter(filePath, TRUNCATE_EXISTING);
            bw.write( new GsonBuilder().setPrettyPrinting().create().toJson(toStore, type));
            Debug.printVerbose("New datas on file : " + toStore.toString());
            bw.close();
        }
        catch(FileNotFoundException e)
        {
            Debug.printError("File wasn't found!");
        }
        catch(IOException e)
        {
            Debug.printError("No file found!");
        }
    }
    public void removeDataOldestValue()
    {
        drawDataFromFileList(this.destinationFile);
    }

    /**
     * Remove from file all values older than anyone in the given list.
     * @param sources
     */
    public void keepOnlyTheMostRecentForEachSource(@NotNull List<DataKey> sources)
    {
        List<DataKey> current=this.getAllOrEmptyList();

        current.addAll(sources);

        List<DataKey> toRemove=current.stream()
                .filter(currentlyInFileOrNewer->current.stream()
                        .anyMatch(moreRecent->moreRecent.otherHasSameSenderButOlderSequenceNumber(currentlyInFileOrNewer)))
                .collect(Collectors.toList());
        Debug.printVerbose("Those should be eliminated"+toRemove);
        current.removeAll(toRemove);
        store(current);
    }


    /**
     * Return the list of all elements, or null if empty
     */
    private List<DataKey> getAll()
    {
        String s;
        StringBuilder toConvertFromJSON = new StringBuilder();

        FileReader fr=null;
        try {
            fr = new FileReader(destinationFile);
            BufferedReader br=new BufferedReader(fr);

            while((s=br.readLine())!=null)
            {
                toConvertFromJSON.append(s);

            }
            Type type = new TypeToken<List<DataKey>>() {}.getType();
            List<DataKey> datas = new Gson().fromJson(toConvertFromJSON.toString(), type);
            if(datas!=null)
                return datas;
        } catch (FileNotFoundException e) {
            Debug.printError(e);
        } catch (IOException e) {
            Debug.printError(e);
        }
        finally {
            try {
                fr.close();
            } catch (IOException e) {
                Debug.printError(e);
            }
        }
        return null;

    }

    private List<DataKey> getAllOrEmptyList()
    {
        List<DataKey> toRet=getAll();
        return toRet==null?new ArrayList<>():toRet;
    }
}

