package supervisor.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.communication.message.LogMessageOperator;
import org.jetbrains.annotations.NotNull;
import supervisor.communication.message.HeartbeatRequest;
import supervisor.communication.message.MessageSupervisor;
import operator.communication.message.MessageOperator;
import operator.communication.message.ReplyHeartBeat;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Vertex;
import utils.Debug;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 10/02/18.
 */
public class TaskSocket{

    private final Socket socket;
    private final BufferedReader socketIn;
    private final PrintWriter socketOut;
    private final Gson readGson;
    private final Gson writeGson;
    private final ExecutorService executorService;

    public TaskSocket(@NotNull Socket socket) throws IOException {
        this.socket = socket;
        this.socket.getInputStream();
        this.socketIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.socketOut = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));

        RuntimeTypeAdapterFactory typeAdapterFactory2 = RuntimeTypeAdapterFactory.of(MessageOperator.class, "type")
                .registerSubtype(ReplyHeartBeat.class)
                .registerSubtype(LogMessageOperator.class)
                ;
        readGson=new GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory2).create();
        RuntimeTypeAdapterFactory typeAdapterFactory = RuntimeTypeAdapterFactory.of(MessageSupervisor.class, "type")
                .registerSubtype(OperatorDeployment.class)
                .registerSubtype(HeartbeatRequest.class)
                ;
        writeGson = new GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory).create();  //setPrettyPrinting
        executorService = Executors.newCachedThreadPool();
    }


    public void listen(List<Vertex<OperatorDeployment>> sortedGraph) {
        //receive
        String input;
        //Expected login here
        Debug.printVerbose("Socket receiving....");

        try {
            //while ((input = socketIn.readLine()) != null) {
                input = socketIn.readLine();
                Debug.printVerbose("RECEIVED: " + input);
                MessageOperator messageSupervisor = readGson.fromJson(input, MessageOperator.class);
                List <MessageSupervisor> operatorDeployments = messageSupervisor.execute(sortedGraph);

                if (operatorDeployments != null) {
                    for (MessageSupervisor operatorDeployment : operatorDeployments) {
                        this.send(operatorDeployment);
                    }
                }

            //}
        } catch (IOException e) {
            Debug.printError(e);
        }
        catch (Exception e)
        {
            Debug.printError(e);
        }
    }

    void send(@NotNull MessageSupervisor messageServer){
        executorService.submit (() -> {
            try {
                String res = writeGson.toJson(messageServer, MessageSupervisor.class);
                Debug.printVerbose("SERVER: SENDING " + res + " TO " + socket.getLocalAddress().getHostAddress() + " SOCKET");
                socketOut.println(res);
                socketOut.flush();
            }catch (Exception e)
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
