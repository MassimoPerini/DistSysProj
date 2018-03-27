package operator.types;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import operator.recovery.Key;
import operator.recovery.RecoveryManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supervisor.Position;
import utils.Debug;

import javax.xml.crypto.Data;

/**
 * Created by massimo on 11/02/18.
 *
 * Represents the generic type of operator
 *
 */
public abstract class OperatorType {

    final int size;
    final int slide;
    
    private int sequenceNumber;
    
    private @Nullable Position source;
    private transient @NotNull List<OperatorOutputQueue> destination;
    //che fa socket description?
    private final @NotNull List<Position> socketDescription;
    private transient ExecutorService executorService;
    private transient BlockingQueue<DataKey> sourceMsgQueue; //messaggi input -> processo

    /**
     * This object is necessary to store the message being sent now.
     * After the message is Sent by all the output queues, this very object is used to delete it.
     */
    private RecoveryManager currentMessageRecoveryManager;
    /**
     * This set contains all the output queues that have Sent/sent the message
     */
    private Set<OperatorOutputQueue> socketsThatHaveSentCurrentMessage;
    
    public OperatorType(@NotNull List<Position> destination, int size, int slide)
    {
        this.socketDescription = destination;
        this.size = size;
        this.slide = slide;
        recoverySetup();
    }

    public OperatorType(@NotNull List<Position> destination, int size, int slide, @Nullable Position socket)
    {
        this.socketDescription = destination;
        this.size = size;
        this.slide = slide;
        this.source = socket;
        recoverySetup();
    }
    
    /**
     * Initialize the storage manager and the ack queue
     */
    private void recoverySetup()
    {
        if(source == null)
            this.currentMessageRecoveryManager=new RecoveryManager("output_handler_recovery"+ "origin");
        else {
            this.currentMessageRecoveryManager =
                    new RecoveryManager("output_handler_recovery" + source.toString());
        }
        this.socketsThatHaveSentCurrentMessage=Collections.synchronizedSet(new HashSet<>());
    }
    
    
    public void execute() {
        //this condition is a while true loop
    	while (Math.random() < 10) {
            List<DataKey> currentMsg = new LinkedList<>();
            //Put (and remove) at maximum this.size elements into currentMsg
            this.sourceMsgQueue.drainTo(currentMsg, this.size);
            int itemNeeded = this.size - currentMsg.size();
            //i take n elements from the Queue
            if (itemNeeded > 0)
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

            float result = this.operationType(currentMsg.stream().map(DataKey::getValue).collect(Collectors.toList()));
            List<Key> senders=currentMsg.stream().map(dk->dk.getAggregator()).collect(Collectors.toList());
            
           DataKey messageData = new DataKey(result, new Key(this.source, ++sequenceNumber),senders);
            //aggiungo i dati al recovery manager
            currentMsg.forEach(msg->currentMessageRecoveryManager.appendData(msg));

            executorService.submit(() -> sendMessage(messageData));

            /*waitForEverySocketToSaveMessageInHisFile(messageData);

            for(int i=0;i<this.slide;i++)
            {
            	DataKey d= currentMsg.remove(0);
            	currentMessageRecoveryManager.removeDataFromList(d);
            }*/
        }
    }
     protected abstract float operationType(List<Float> streamDatas);

  
    

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
            //If there's no socket description it means it is a leaf and we need to write on file the result
            this.destination.add(new OutputToFile());   //No socket output -> write on file
        }
        else
        {
            for (Position socketRepr : socketDescription) {
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
				Debug.printVerbose("Inside second operator");

                ServerSocket serverSocket=new ServerSocket(source.getPort());
                executorService.submit(this::execute);
				while(true)
				{
                    Debug.printDebug("Start accepting new incoming connections!");
                    Socket socket=serverSocket.accept();
					Debug.printDebug("A new socket input!");
					InputFromSocket receiver=new InputFromSocket(socket, source);
					executorService.submit(()->receiver.startReceiving(this));
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
        }

    }


    public void addToMessageQueue(DataKey messageData)
    {
        try {
            this.sourceMsgQueue.put(messageData);
        } catch (InterruptedException e) {
            Debug.printError(e);
        }
    }

    private void sendMessage(DataKey messageData)
    {
        for (OperatorOutputQueue operatorOutputQueue : destination) {
            Debug.printVerbose(destination.toString());
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
    
    /**
     * Wait for the given message to be written in all the files corresponding to the outgoing sockets
     */
    public void waitForEverySocketToSaveMessageInHisFile(DataKey messageData)
    {
    	synchronized (socketsThatHaveSentCurrentMessage) {
    		try {
    			while(socketsThatHaveSentCurrentMessage.size()<this.destination.size())
    	    	{
    	    	        Debug.printVerbose(socketsThatHaveSentCurrentMessage.toString());
    	    	        Debug.printVerbose(destination.toString());
    	    			socketsThatHaveSentCurrentMessage.wait();
    	    	}
    	
			} catch (InterruptedException e) {
				Debug.printDebug(e);
			}
    	}
    }

    public void pushOperatorToQueue(OperatorOutputQueue operator){
        this.socketsThatHaveSentCurrentMessage.add(operator);
        synchronized (socketsThatHaveSentCurrentMessage){
            socketsThatHaveSentCurrentMessage.notify();
        }
    }
}