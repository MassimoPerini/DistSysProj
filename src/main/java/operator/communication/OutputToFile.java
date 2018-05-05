package operator.communication;

import operator.recovery.DataKey;
import operator.types.OperatorType;
import utils.Debug;

import java.io.FileWriter;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by massimo on 02/03/18.
 * This class is used to write the result of the overall computation
 */
public class OutputToFile implements OperatorOutputQueue {

    private BlockingQueue<DataKey> messageData = new LinkedBlockingQueue<>();
    private OperatorType dataFeeder;
    int r;

    public OutputToFile(OperatorType dataFeeder)
    {
        this.dataFeeder=dataFeeder;
        Random r = new Random();
        this.r = r.nextInt(10000);
    }

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
                FileWriter fileWriter=new FileWriter("output"+r+".txt", true);
                fileWriter.write(msg.getOriginalKey()+","+msg.getData()+"\n");
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
