package operator.communication;

import operator.recovery.DataKey;
import operator.types.OperatorType;
import org.jetbrains.annotations.NotNull;
import utils.Debug;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by massimo on 02/03/18.
 */
public class OutputToSocket implements OperatorOutputQueue{


    private Socket socket;
    private ObjectInputStream socketIn;
    private ObjectOutputStream socketOut;
    private final ExecutorService executorService;
    private final BlockingQueue<DataKey> messageData;
    //this is the OperatorType that passes itself in order to stop sending datas to other outputSockets
    private final OperatorType dataFeeder;
    private final String addressToReconnectWith;
    private final int portToReconnectWith;


    public OutputToSocket(@NotNull Socket socket, OperatorType operatorType) throws IOException {
        this.socket = socket;
        this.socketOut = (new ObjectOutputStream(this.socket.getOutputStream()));
        this.socketIn = (new ObjectInputStream(this.socket.getInputStream()));
        this.executorService = Executors.newCachedThreadPool();
        this.messageData = new LinkedBlockingQueue<>();
        this.dataFeeder = operatorType;
        //todo: check if both work
        this.addressToReconnectWith = socket.getInetAddress().toString();
        this.portToReconnectWith = socket.getPort();
    }


    public void start()
    {

        this.executorService.submit(this::keepSending);
    }

    private void keepSending() {
        Debug.printDebug("Start send with socket...");
        //todo: while(true)
        while(Math.random() < 10)
        {
            try {
                // i take values from the Q
                DataKey messageData = this.messageData.take();
                Debug.printVerbose("operator queue out Socket sending....");

                this.socketOut.writeObject(messageData);
                this.socketOut.flush();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                dataFeeder.stopOutput(this);
                Boolean tryToReconnect = true;
                while(tryToReconnect){
                    try {
                        this.socket = new Socket(this.addressToReconnectWith, this.portToReconnectWith);
                        this.socketOut = (new ObjectOutputStream(this.socket.getOutputStream()));
                        this.socketIn = (new ObjectInputStream(this.socket.getInputStream()));
                        tryToReconnect = false;
                        this.dataFeeder.restartOutput(this);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        Debug.printVerbose("Waiting for the node to come back alive");
                        //todo check if it's okays
                        try {
                            Thread.sleep((long) (200));
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                            Debug.printVerbose("Thread sleep failed");
                        }
                    }
                }
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
        try {
            socketOut.close();
            socketIn.close();
            socket.close();
        }
        catch (IOException e)
        {
            Debug.printError("IOException on closing...");
        }
    }

}
