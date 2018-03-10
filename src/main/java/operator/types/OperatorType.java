package operator.types;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import operator.ElementReceiver;
import operator.communication.OperatorOutputQueue;
import operator.communication.Sink;
import operator.communication.SocketOperatorOutputQueue;
import operator.communication.StreamReader;
import operator.communication.message.MessageData;
import operator.communication.message.MessageOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supervisor.Position;
import supervisor.communication.SocketManager;
import utils.Debug;

/**
 * Created by massimo on 11/02/18.
 *
 * Represents the generic type of operator
 *
 */
public abstract class OperatorType {

    protected final int size;
    final int slide;
    private @Nullable SocketRepr source;
    private transient @NotNull List<OperatorOutputQueue> destination;
    private final @NotNull List<SocketRepr> socketDescription;
    transient ExecutorService executorService;
    transient BlockingQueue<MessageData> sourceMsgQueue; //messaggi input -> processo

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
        source = socket;
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
    private List<Position> forwardStar;
    
    /**
     * This is the port from which the input arrives.
     */
    private Position ownPort;

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
            this.destination.add(new Sink());   //No socket output -> write on file

        }
        else
        {
            for (SocketRepr socketRepr : socketDescription) {
                try {
                    SocketOperatorOutputQueue socketOperatorOutputQueue = new SocketOperatorOutputQueue(new Socket(socketRepr.getAddress(),socketRepr.getPort()));
                    this.destination.add(socketOperatorOutputQueue);

                } catch (IOException e) {
                    Debug.printDebug(e);
                }
            }
        }

        for (OperatorOutputQueue operatorOutputQueue : destination) {
            operatorOutputQueue.start();    //Starting output threads
        }

        if (source == null)
        {
            StreamReader streamReader = new StreamReader("src/main/resources/input.txt");
            executorService.submit(() -> streamReader.startReceiving(this));    //Start input threads
            executorService.submit(this::execute);
        }
        else{
            try {
				ServerSocket serverSocket=new ServerSocket(ownPort.getPort());
				while(true)
				{
					Socket socket=serverSocket.accept();
					ElementReceiver receiver=new ElementReceiver(socket, ownPort);
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