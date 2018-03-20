package operator.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import operator.communication.message.MessageData;
import operator.recovery.DataKey;
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


    private final Socket socket;
    private final ObjectInputStream socketIn;
    private final ObjectOutputStream socketOut;
    private final ExecutorService executorService;
    private final BlockingQueue<DataKey> messageData;


    public OutputToSocket(@NotNull Socket socket) throws IOException {
        this.socket = socket;
        this.socketOut = (new ObjectOutputStream(this.socket.getOutputStream()));
        this.socketIn = (new ObjectInputStream(this.socket.getInputStream()));
        this.executorService = Executors.newCachedThreadPool();
        this.messageData = new LinkedBlockingQueue<>();
    }


    public void start()
    {

        this.executorService.submit(this::keepSending);
    }

    private void keepSending() {
        Debug.printDebug("Start send with socket...");
        while(true)
        {
            try {
                DataKey messageData = this.messageData.take();
                Debug.printVerbose("operator queue out Socket sending....");

                this.socketOut.writeObject(messageData);
                this.socketOut.flush();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
