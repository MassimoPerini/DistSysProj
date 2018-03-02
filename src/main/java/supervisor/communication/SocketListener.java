package supervisor.communication;

import operator.types.Sum;
import supervisor.communication.message.OperatorDeployment;
import utils.Debug;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 10/02/18.
 */
public class SocketListener{

    private static final int PORT = 1337;
    private final SocketManager socketManager;

    public SocketListener (SocketManager socketManager)
    {
        this.socketManager = socketManager;
    }

    /**
     * runs the server and handles listening for connections
     */

    public void run() {
        try {
            //creats the socket
            ServerSocket serverSocket = new ServerSocket(PORT);
            Debug.printVerbose("SERVER SOCKET READY ON PORT" + PORT);

            while (true) {
                //Waits for a new client to connect
                Socket inputSocket = serverSocket.accept();
                // creates the view (server side) associated with the new client
                Debug.printVerbose("New connection on Socket, assuming a daemon");
                TaskSocket taskSocket = new TaskSocket(inputSocket);
                NodeSocket nodeSocket = new NodeSocket(taskSocket);
                this.socketManager.addSocket(nodeSocket);

                //TODO SPOSTARE QUESTA PARTE
                OperatorDeployment operatorDeployment = new OperatorDeployment(new Sum(2,2),"");//TODO trovare qualche metodo migliore di passare il JAR
                this.socketManager.deployNewOperator(0, operatorDeployment);

            }
        }
        catch (Exception e)
        {
            Debug.printError(e);
        }
    }
}
