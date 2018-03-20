package supervisor.communication;

import operator.types.OperatorType;
import operator.types.SocketRepr;
import operator.types.Sum;
import supervisor.Position;
import supervisor.communication.message.OperatorDeployment;
import utils.Debug;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 10/02/18.
 * This class listens to a socket and adds it to the socketManager it has been constructed with.
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

            int i=0;

            while (true) {
                //Waits for a new client to connect
                Socket inputSocket = serverSocket.accept();
                // creates the view (server side) associated with the new client
                Debug.printVerbose("New connection on Socket, assuming a daemon");
                TaskSocket taskSocket = new TaskSocket(inputSocket);
                NodeSocket nodeSocket = new NodeSocket(taskSocket);
                this.socketManager.addSocket(nodeSocket);

                //TODO SPOSTARE QUESTA PARTE
                i++;

                if (i == 2)
                {
                    //InetAddress addr = InetAddress.getByName("127.0.0.1");
                    Position firstSocket = new Position("127.0.0.1", 1340);


                    OperatorDeployment operatorDeploymentLast = new OperatorDeployment(new Sum(2,2, firstSocket, new LinkedList<>()),"");
                    this.socketManager.deployNewOperator(1, operatorDeploymentLast);

                    LinkedList<Position> outSocket = new LinkedList<>();
                    outSocket.add(firstSocket);

                    //source, destination
                    OperatorDeployment operatorDeployment = new OperatorDeployment(new Sum(2,2, null, outSocket),"");//TODO trovare qualche metodo migliore di passare il JAR
                    this.socketManager.deployNewOperator(0, operatorDeployment);
                }


            }
        }
        catch (Exception e)
        {
            Debug.printError(e);
            e.printStackTrace();
        }
    }
}
