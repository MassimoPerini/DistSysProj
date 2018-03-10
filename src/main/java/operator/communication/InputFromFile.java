package operator.communication;

import operator.communication.message.MessageData;
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

    public InputFromFile(String path)
    {
        this.path = path;
    }

    @Override
    public void startReceiving(OperatorType operatorType)
    {
        try {
            BufferedReader b = new BufferedReader(new FileReader(this.path));

            String readLine = "";
            Debug.printVerbose("Reading file using Buffered Reader");
            while ((readLine = b.readLine()) != null) {
                MessageData messageData = new MessageData(Double.parseDouble(readLine));
                operatorType.addToMessageQueue(messageData);
                Thread.sleep((long)(Math.random()*2000));
            }
        }
        catch (Exception e)
        {
            Debug.printError(e);
        }
    }

}
