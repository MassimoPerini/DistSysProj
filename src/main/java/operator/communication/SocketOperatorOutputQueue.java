package operator.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import operator.communication.message.MessageData;
import org.jetbrains.annotations.NotNull;
import utils.Debug;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 02/03/18.
 */
public class SocketOperatorOutputQueue implements OperatorOutputQueue{


    private final Socket socket;

    private final BufferedReader socketIn;
    private final PrintWriter socketOut;
    private final Gson readGson;    //per ack
    private final Gson writeGson;
    private final ExecutorService executorService;

    public SocketOperatorOutputQueue(@NotNull Socket socket) throws IOException {
        this.socket = socket;

        this.socketIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.socketOut = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        readGson=new GsonBuilder().create();
        writeGson = new GsonBuilder().create();  //setPrettyPrinting
        executorService = Executors.newCachedThreadPool();

    }


    public void start()
    {
        this.executorService.submit(this::listen);
    }

    private void listen() {
        String input;
        Debug.printVerbose("operator queue out Socket receiving....");
    }

    public void send(@NotNull MessageData message){
        executorService.submit (() -> {
            try {
                String res = writeGson.toJson(message, MessageData.class);
                Debug.printVerbose("SERVER: SENDING " + res + " TO " + socket.getLocalAddress().getHostAddress() + " SOCKET");
                socketOut.println(res);
                socketOut.flush();
            }
            catch (Exception e)
            {
                Debug.printDebug(e);
            }
        });
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
