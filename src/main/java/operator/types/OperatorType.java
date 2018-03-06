package operator.types;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import operator.ElementReceiver;
import operator.communication.OperatorOutputQueue;
import operator.communication.Sink;
import operator.communication.SocketOperatorOutputQueue;
import operator.communication.StreamReader;
import operator.communication.message.MessageData;
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
    transient List<MessageData> messageDatas; //messaggi input -> processo

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

    public synchronized void execute() {
        //this condition is a while true loop
        while (Math.random() < 10) {
            while (messageDatas.size() < this.size) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Debug.printDebug(e);
                }
            }

            Debug.printVerbose("Starting elaboration...");

            List<MessageData> currentMsg = messageDatas.subList(0,size);

            double result = this.operationType(currentMsg.stream().map(MessageData::getValue).collect(Collectors.toList()));
            MessageData messageData = new MessageData(result);
            messageDatas.subList(0, slide).clear();

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
        messageDatas = Collections.synchronizedList(new LinkedList<>());
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

    public synchronized void addToMessageQueue(MessageData messageData)
    {
        this.messageDatas.add(messageData);
        if (messageDatas.size() >=size)
        {
            notifyAll();    //start the "operational" thread
        }
    }

    public void sendMessage(MessageData messageData)
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