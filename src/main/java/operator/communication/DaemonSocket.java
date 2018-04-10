package operator.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.communication.message.MessageOperator;
import operator.communication.message.ReplyHeartBeat;
import org.jetbrains.annotations.NotNull;
import supervisor.communication.message.HeartbeatRequest;
import supervisor.communication.message.MessageSupervisor;
import supervisor.communication.message.OperatorDeployment;
import utils.Debug;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 10/02/18.
 * This class it's the deamonSocket.
 * Once it is is started, it receives a gson with the type of the operations
 * and spawns a new Operator
 * todo: MessageSupervisor should also be used to communicate an HeatbeatRequest, under the assumption that nodes don't fall
 */
public class DaemonSocket {

    private final Socket socket;

    private final BufferedReader socketIn;
    private final PrintWriter socketOut;
    private final Gson readGson;
    private final Gson writeGson;
    private final ExecutorService executorService;

    public DaemonSocket(@NotNull Socket socket) throws IOException {
        this.socket = socket;

        this.socketIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.socketOut = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));

        RuntimeTypeAdapterFactory typeAdapterFactory2 = RuntimeTypeAdapterFactory.of(MessageSupervisor.class, "type")
                .registerSubtype(OperatorDeployment.class)
                .registerSubtype(HeartbeatRequest.class)
                ;
        readGson=new GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory2).create();
        RuntimeTypeAdapterFactory typeAdapterFactory = RuntimeTypeAdapterFactory.of(MessageOperator.class, "type")
                .registerSubtype(ReplyHeartBeat.class)
                ;
        writeGson = new GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory).create();  //setPrettyPrinting
        executorService = Executors.newCachedThreadPool();

    }

    /**
     * This method is called from the MainDaemon class as soon as it is created.
     */
    public void start(DaemonOperatorInfo daemonOperatorInfo)
    {
        try {
            this.executorService.submit(() -> listen(daemonOperatorInfo));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method either allow the Daemon to submit a thread to:
     * 1) Spawn a new Operator
     * todo: 2) Listen for the heartbeat
     */
    private void listen(DaemonOperatorInfo daemonOperatorInfo) {
        String input;
        Debug.printVerbose("node Socket receiving....");

        try {
            while (true) {
                while ((input = socketIn.readLine()) != null) {
                    Debug.printVerbose("node:" + input);
                    MessageSupervisor messageSupervisor = readGson.fromJson(input, MessageSupervisor.class);
                    this.executorService.submit(() -> messageSupervisor.execute(daemonOperatorInfo));
                }
            }
        }
        catch (IOException e) {
            Debug.printError(e);
        }
        catch (Exception e)
        {
            Debug.printError(e);
            e.printStackTrace();
        }
        //receive
        //Expected login here
    }

    void send(@NotNull MessageSupervisor messageServer){
        executorService.submit (() -> {
            try {
                String res = writeGson.toJson(messageServer, MessageSupervisor.class);
                Debug.printVerbose("SERVER: SENDING " + res + " TO " + socket.getLocalAddress().getHostAddress() + " SOCKET");
                socketOut.println(res);
                socketOut.flush();
            }
            catch (Exception e)
            {
                Debug.printError(e);
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
