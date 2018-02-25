package operator.recovery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Graph;
import supervisor.graph_representation.Vertex;
import utils.Debug;

import javax.xml.crypto.Data;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Created by higla on 24/02/2018.
 */
public class RecoveryManager {
    //todo: clear unused methods...
    public DataKey readDataFromFile(String fileName){
        StringBuilder toConvertFromJSON = new StringBuilder();
        String temp = "";
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            /*
            while((temp = bufferedReader.readLine()) != null) {
                toConvertFromJSON.append(temp);
            }*/
            toConvertFromJSON.append(bufferedReader.readLine());
            // Always close files.
            bufferedReader.close();
            fileReader.close();
        }
        catch(IOException ex) {
            Debug.printError("Unable to open file '" + fileName + "'");
        }
        //Type type = new TypeToken<List<DataKey>>() {}.getType();
        Debug.printVerbose(toConvertFromJSON.toString());
        DataKey dataKey = new Gson().fromJson(toConvertFromJSON.toString(), DataKey.class);
        Debug.printVerbose("GSON processed" + dataKey.toString());

        return dataKey;
    }


    public DataKey readDataFromTopOfTheQueue(String fileName){
        StringBuilder toConvertFromJSON = new StringBuilder();
        String temp = "";
        try {
            // FileReader reads text files in the default encoding.
            File file = new File(fileName);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line ;
            //Read from the original file and write to the new
            //unless content matches data to be removed.
            while((line = bufferedReader.readLine()) != null) {
                toConvertFromJSON.append(line);
            }
            Debug.printVerbose("vuoto " + toConvertFromJSON.toString());
            bufferedReader.close();
        }
        catch(IOException ex) {
            Debug.printError("Unable to open file '" + fileName + "'");
        }
        //Type type = new TypeToken<List<DataKey>>() {}.getType();
        Debug.printVerbose(toConvertFromJSON.toString());
        //List<DataKey> dataKey = new Gson().fromJson(toConvertFromJSON.toString(), type);
        DataKey dataKey = new Gson().fromJson(toConvertFromJSON.toString(), DataKey.class);
        Debug.printVerbose("GSON processed" + dataKey.toString());
        return dataKey;
        //return dataKey.get(0);
    }

    public void putDataInFile(String fileName, DataKey dataKey){
        Gson writeGson = new GsonBuilder().create();
        String toWrite = writeGson.toJson(dataKey);
        try {
            // Assume default encoding.
            FileWriter fileWriter = new FileWriter(fileName, true);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            // Note that write() does not automatically
            // append a newline character.
            printWriter.write(toWrite);
            bufferedWriter.newLine();


            // Always close files.
            printWriter.close();
            bufferedWriter.close();
            fileWriter.close();
        }
        catch(IOException ex) {
            Debug.printError("Error writing in file'" + fileName + "'");
        }

        return;
    }

    /**
     * this method appends to an existing file, new Datas.
     * If the file doesn't exist it creates one. Note that if the file already exists (and it has old data),
     * the method will append datas anyway. So first it's better to use a safer method that creates a new file
     * anyway, then use this method.
     * @param fileName is the name of the File that will be used for the path
     * @param dataKey is the value to append to the file
     */
    public void appendDataInFileList(String fileName, DataKey dataKey){
        String s;
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
            Debug.printError("Error1, file not found!");
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
            Debug.printError("Error1, file not found!");
        }
        catch(IOException e)
        {
            Debug.printError("Error2, IO exception!");
        }
        //todo: handle null?
        return value;
    }
}

