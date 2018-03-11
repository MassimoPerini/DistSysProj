package operator.recovery;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import operator.communication.message.MessageData;
import utils.Debug;

import javax.xml.crypto.Data;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Created by higla on 24/02/2018.
 */
public class RecoveryManager {
	
	private String destinationFile;
	
	public RecoveryManager(String destinationFile)
	{
		this.destinationFile=destinationFile;
	}
	
	public void appendData(DataKey elem)
	{
		appendDataInFileList(destinationFile, elem);
	}
	
	public void appendData(MessageData elem)
	{
		appendData(new DataKey(elem));
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
        //added after Fulvio put destinationFile
        fileName = this.destinationFile;
        StringBuilder toConvertFromJSON = new StringBuilder();
        try
        {
            FileReader fr=new FileReader(fileName);
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
                    if(data.getData() == dataKey.getData() && data.getProgressiveKey().equals(dataKey.getProgressiveKey())){
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

                Path filePath = Paths.get(fileName);
                BufferedWriter bufferedWriter= Files.newBufferedWriter(filePath, TRUNCATE_EXISTING);
                bufferedWriter.write( new Gson().toJson(datas, type));
                Debug.printVerbose("Data read : " + datas.toString());
                bufferedWriter.close();

            }
            catch(FileNotFoundException e)
            {
                Debug.printError("File was not found while trying to add data!");
            }
            catch(IOException e)
            {
                Debug.printError("No file found!");
            }

            br.close();
        }
        catch(FileNotFoundException e)
        {
            Debug.printError("Error1, file not found while appending data!");
        }
        catch(IOException e)
        {
            Debug.printError("Error2, IO exception!");
        }
    }

    /**
     * This method draws a value from a file and returns it. It also overwrites the file with the new list.
     * @param fileName is the name of the File that will be used for the path
     * @return the value drawn from the list.
     */
    public DataKey drawDataFromFileList(String fileName){
        String s;
        StringBuilder toConvertFromJSON = new StringBuilder();
        DataKey value = null;
        try
        {
            FileReader fr=new FileReader(fileName);
            BufferedReader br=new BufferedReader(fr);

            while((s=br.readLine())!=null)
            {
                toConvertFromJSON.append(s);

            }
            Type type = new TypeToken<List<DataKey>>() {}.getType();
            List<DataKey> datas = new Gson().fromJson(toConvertFromJSON.toString(), type);
            if(datas != null) {
                value = datas.get(0);
                datas.remove(value);
            }
            try{
                Path filePath = Paths.get(fileName);
                BufferedWriter bw= Files.newBufferedWriter(filePath, TRUNCATE_EXISTING);
                bw.write( new Gson().toJson(datas, type));
                Debug.printVerbose(datas.toString());
                bw.close();
            }
            catch(FileNotFoundException e)
            {
                Debug.printError("File was not found!");
            }
            catch(IOException e)
            {
                Debug.printError("No file found!");
            }
            br.close();
            return value;
        }
        catch(FileNotFoundException e)
        {
            Debug.printError("Error1, file not found drawing data!");
        }
        catch(IOException e)
        {
            Debug.printError("Error2, IO exception!");
        }
        //todo: handle null?
        return value;
    }

    public void removeDataFromList(DataKey dataToCheck){
        String s;
        StringBuilder toConvertFromJSON = new StringBuilder();
        try
        {
            FileReader fr=new FileReader(destinationFile);
            BufferedReader br=new BufferedReader(fr);

            while((s=br.readLine())!=null)
            {
                toConvertFromJSON.append(s);

            }
            Type type = new TypeToken<List<DataKey>>() {}.getType();
            List<DataKey> datas = new Gson().fromJson(toConvertFromJSON.toString(), type);
            if(datas != null) {
                //i look for the data
                for(DataKey data:datas){
                    if(data.checkSameData(dataToCheck))
                        datas.remove(data);
                }
            }
            try{
                Path filePath = Paths.get(destinationFile);
                BufferedWriter bw= Files.newBufferedWriter(filePath, TRUNCATE_EXISTING);
                bw.write( new Gson().toJson(datas, type));
                Debug.printVerbose("New datas on file : " + datas.toString());
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
            br.close();
        }
        catch(FileNotFoundException e)
        {
            Debug.printError("Error1, file not found removing data!");
        }
        catch(IOException e)
        {
            Debug.printError("Error2, IO exception!");
        }

    }

	public void removeDataFromList(MessageData d) 
	{
		removeDataFromList(new DataKey(d));
	}


}

