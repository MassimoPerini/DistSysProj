package operator.communication;

import operator.communication.message.MessageData;
import operator.communication.message.MessageOperator;
import operator.recovery.DataKey;
import utils.Debug;

import java.io.FileWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by massimo on 02/03/18.
 */
public class OutputToFile implements OperatorOutputQueue {

    private BlockingQueue<DataKey> messageData = new LinkedBlockingQueue<>();

    @Override
    public void start()
    {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(this::keepSending);
    }

    public void keepSending()
    {
        try {
            Debug.printVerbose("OutputToFile started");
            while (true) {

                DataKey msg = this.messageData.take();

                Debug.printVerbose("WRITING " + msg);
                FileWriter fileWriter=new FileWriter("output.txt");
                fileWriter.write(msg.getData()+"");
                fileWriter.flush();
                
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void send(DataKey msg) {
        try {
            messageData.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
