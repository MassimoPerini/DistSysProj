package operator.communication;

import operator.communication.message.MessageData;
import operator.communication.message.MessageOperator;
import utils.Debug;

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
public class Sink implements OperatorOutputQueue {

    private BlockingQueue<MessageData> messageData = new LinkedBlockingQueue<>();

    @Override
    public void start()
    {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(this::keepSending);
    }

    public void keepSending()
    {
        try {
            Debug.printVerbose("Sink started");
            while (true) {

                MessageData msg = this.messageData.take();

                Debug.printVerbose("WRITING " + msg);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void send(MessageData msg) {
        try {
            messageData.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
