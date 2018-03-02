package operator.communication;

import operator.communication.message.MessageData;
import operator.communication.message.MessageOperator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 02/03/18.
 */
public class Sink implements OperatorOutputQueue {

    private List<MessageData> messageData = Collections.synchronizedList(new LinkedList<>());

    @Override
    public void start()
    {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(this::keepSending);
    }

    public synchronized void keepSending()
    {
        try {
            System.out.println("Sink started");
            while (true) {
                while (messageData.size() == 0) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("WRITING " + messageData.get(0));
                messageData.remove(0);

            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void send(MessageData msg) {
        messageData.add(msg);
        this.notifyAll();
    }
}
