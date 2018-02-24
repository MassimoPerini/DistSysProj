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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by higla on 24/02/2018.
 */
public class RecoveryManager {
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
    /* todo:
    public DataKey readAndRemoveDataFromFile(String fileName){
        StringBuilder toConvertFromJSON = new StringBuilder();
        String temp = "";
        try {
            // FileReader reads text files in the default encoding.
            File file = new File(fileName);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            PrintWriter pw = new PrintWriter(new FileWriter(file));
            String line ;
            //Read from the original file and write to the new
            //unless content matches data to be removed.
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.trim().equals(lineToRemove)) {
                    pw.println(line);
                    pw.flush();
                }
            }

            /*
            while((temp = bufferedReader.readLine()) != null) {
                toConvertFromJSON.append(temp);
            }
            toConvertFromJSON.append(bufferedReader.readLine());
            // Always close files.
            bufferedReader.close();
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
    */
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


}
