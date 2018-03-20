package operator.communication;

import operator.recovery.DataKey;
import operator.types.OperatorType;
import utils.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by massimo on 02/03/18.
 */
public class InputFromFile implements OperatorInputQueue{

    private final String path;
    private int conta;
    public InputFromFile(String path)
    {
        this.path = path;
        this.conta = 0;
    }

    @Override
    public void startReceiving(OperatorType operatorType)
    {
        try {
            BufferedReader b = new BufferedReader(new FileReader(this.path));

            String readLine = "";
            Debug.printVerbose("Reading file using Buffered Reader");
            while ((readLine = b.readLine()) != null) {
                StringBuilder stringBuilder = new StringBuilder("file");
                stringBuilder.append(++conta);
                DataKey messageData = new DataKey(Double.parseDouble(readLine), stringBuilder.toString());
                operatorType.addToMessageQueue(messageData);
                Debug.printVerbose("Printo" + this.conta);
                //Thread.sleep((long)(Math.random()*2000));
            }
        }
        catch (Exception e)
        {
            Debug.printError(e);
        }
    }

}
