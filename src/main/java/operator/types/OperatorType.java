package operator.types;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import operator.communication.InputFromSocket;
import operator.communication.OperatorOutputQueue;
import operator.communication.OutputToFile;
import operator.communication.OutputToSocket;
import operator.communication.InputFromFile;
import operator.communication.message.MessageData;
import operator.recovery.DataKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supervisor.Position;
import utils.Debug;

/**
 * Created by massimo on 11/02/18.
 *
 * Represents the generic type of operator
 *
 */
public abstract class OperatorType {

    final int size;
    final int slide;
    private @Nullable SocketRepr source;
    private transient @NotNull List<OperatorOutputQueue> destination;
    private final @NotNull List<SocketRepr> socketDescription;
    private transient ExecutorService executorService;
    private transient BlockingQueue<MessageData> sourceMsgQueue; //messaggi input -> processo


    public OperatorType(@NotNull List<SocketRepr> destination, int size, int slide)
    {
        this.socketDescription = destination;
        this.size = size;
        this.slide = slide;
    }

    public OperatorType(@NotNull List<SocketRepr> destination, int size, int slide, @Nullable SocketRepr socket)
    {
        this.socketDescription = destination;
        this.size = size;
        this.slide = slide;
        this.source = socket;
    }

    public void execute() {
        //this condition is a while true loop
        while (Math.random() < 10) {

            List<MessageData> currentMsg = new LinkedList<>();

            this.sourceMsgQueue.drainTo(currentMsg, this.size); //Put (and remove) at maximum this.size elements into currentMsg
            int itemNeeded = this.size - currentMsg.size();
            if (itemNeeded > 0) //If I need other elements...
            {
                for (int i = 0; i < itemNeeded; i++) {
                    try {
                        currentMsg.add(this.sourceMsgQueue.take());

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Debug.printVerbose("Starting elaboration of "+currentMsg.size()+" elements");

            double result = this.operationType(currentMsg.stream().map(MessageData::getValue).collect(Collectors.toList()));
            MessageData messageData = new MessageData(result);


            executorService.submit(() -> sendMessage(messageData));
        }
    }
     protected abstract double operationType(List<Double> streamDatas);

    /**
     * This attribute contains the position of all the nodes to which the output must be sent
     */
    private List<Position> forwardStar; //TODO come socketDescription ???
    
    /**
     * This is the port from which the input arrives.
     */
    private Position ownPort;   //TODO come attributo source ???

    /***
     * Deploy the task on the operator
     */
    public void deploy()
    {
        this.sourceMsgQueue = new LinkedBlockingQueue<>();
        executorService = Executors.newCachedThreadPool();
        this.destination = new LinkedList<>();
        if (socketDescription.size() == 0)
        {
            this.destination.add(new OutputToFile());   //No socket output -> write on file

        }
        else
        {
            for (SocketRepr socketRepr : socketDescription) {
                boolean keepLooping = false;
                do {
                    try {
                        Socket socket = new Socket(socketRepr.getAddress(), socketRepr.getPort());
                        OutputToSocket outputToSocket = new OutputToSocket(socket);
                        this.destination.add(outputToSocket);
                        keepLooping = false;

                    } catch (IOException e) {
                        //TODO Il socket in output (quindi l'altro operatore) non è pronto. Dovrei ciclare?
                        Debug.printDebug(e);
                        Debug.printDebug("I can't establish connection to the other node input!!!!");
                        keepLooping = true;
                    }
                }while (keepLooping);
            }
        }

        for (OperatorOutputQueue operatorOutputQueue : destination) {
            operatorOutputQueue.start();    //Starting output threads
        }

        if (source == null)
        {
            InputFromFile inputFromFile = new InputFromFile("src/main/resources/input.txt");
            executorService.submit(() -> inputFromFile.startReceiving(this));    //Start input threads
            this.execute();
            //executorService.submit(this::execute);            WHY???
        }
        else{       //Deploy input socket
            try {
				//ServerSocket serverSocket=new ServerSocket(ownPort.getPort());
                ServerSocket serverSocket=new ServerSocket(source.getPort());
                executorService.submit(this::execute);
				while(true)
				{
                    Debug.printDebug("Start accepting new incoming connections!");
                    Socket socket=serverSocket.accept();
					Debug.printDebug("A new socket input!");
					InputFromSocket receiver=new InputFromSocket(socket, ownPort);
					executorService.submit(()->receiver.startReceiving(this));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

    }

    public Position getOwnPort() {
        return ownPort;
    }

    public void addToMessageQueue(MessageData messageData)
    {
        try {
            this.sourceMsgQueue.put(messageData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(MessageData messageData)
    {
        for (OperatorOutputQueue operatorOutputQueue : destination) {
            operatorOutputQueue.send(messageData);
        }
        //TODO output the message (a new thread wants to send this message)
    }

    public int getSize()
    {
        return this.size;
    }
    public int getSlide()
    {
        return this.slide;
    }

}