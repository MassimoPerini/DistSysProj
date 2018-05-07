package operator.communication;

import operator.recovery.DataKey;
import operator.recovery.Key;
import operator.types.OperatorType;
import org.jetbrains.annotations.NotNull;
import utils.Debug;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by massimo on 02/03/18.
 */
public class OutputToSocket implements OperatorOutputQueue{


    private final List<SingleParallelSocket> singleParallelSocket;
    private final ExecutorService executorService;
    private final BlockingQueue<DataKey> messageData;



    //this is the OperatorType that passes itself in order to stop sending datas to other outputSockets


    public OutputToSocket(@NotNull List<Socket> socket, OperatorType operatorType) {
        this.singleParallelSocket = new ArrayList<>();
        for (Socket socket1 : socket) {
            this.singleParallelSocket.add(new SingleParallelSocket(socket1, operatorType));
        }

        this.executorService = Executors.newCachedThreadPool();
        this.messageData = new LinkedBlockingQueue<>();
    }


    public void start()
    {

        this.executorService.submit(this::keepSending);

        for (SingleParallelSocket parallelSocket : this.singleParallelSocket) {
            this.executorService.submit(parallelSocket::keepAcknowledging);
        }
    }

    /**
     * Read acks from the socket, then inform the feeder.
     */


    private void keepSending() {
        Debug.printDebug("Start send with socket...");
        // while(true)
        while(Math.random() < 10)
        {
            try {
                // i take values from the Q
                DataKey messageData = this.messageData.take();
                Debug.printVerbose("operator queue out Socket sending....");

                int hash = messageData.getOriginalKey().hashCode();
                int index = Math.abs(hash % this.singleParallelSocket.size());
                singleParallelSocket.get(index).send(messageData);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void send(@NotNull DataKey message){

        try {
            this.messageData.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    void finish(){
        for (SingleParallelSocket parallelSocket : singleParallelSocket) {
            parallelSocket.finish();
        }
    }

}
