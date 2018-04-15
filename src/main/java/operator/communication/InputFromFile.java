package operator.communication;

import operator.recovery.DataKey;
import operator.recovery.Key;
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

            	Key sender=new Key(null, ++conta);
            	String [] row = readLine.split(",");
            	DataKey messageData = new DataKey(row[0], Double.parseDouble(row[1]), sender,null);
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
