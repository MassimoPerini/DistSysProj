package operator.types;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import operator.communication.*;
import operator.recovery.DataKey;
import operator.recovery.Key;
import operator.recovery.RecoveryManager;

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
    
    private int sequenceNumber;
    
    private @Nullable Position source;
    private transient @NotNull List<OperatorOutputQueue> destination;
    //messageAddressees is the list of the next nodes
    private final @NotNull List<Position> messageAddressees;
    private transient ExecutorService executorService;
    private final Object stoppedByCrash = new Object();

    private transient List<DataKey> sourceMsgQueue; //messaggi input -> processo
    //list of fallen forward nodes
    private Set<OutputToSocket> outputToSocketFallen;
    private Map<Position, InputFromSocket> dataSenders;
    
    /**
     * This object is used to store the last processed window
     */
    private RecoveryManager lastProcessedWindowRecoveryManager;

    /**
     * This file contains all the messages that must be stored because addressees haven't received them yet.
     * Semantics: messages are appended without their sources.
     * Each source represents a node having acknowledged the message.
     * The list of sources is updated when acks are received.
     */
    private RecoveryManager recoveryManagerForMessagesSentAndNotAcknowledged;

    private  RecoveryManager lastMessageBySenderRecoveryManager;


    public OperatorType(@NotNull List<Position> destination, int size, int slide, @Nullable Position socket)
    {
        this.messageAddressees = destination;
        this.size = size;
        this.slide = slide;
        this.source = socket;
        dataSenders=new HashMap<>();
        outputToSocketFallen = new HashSet<>();
        recoverySetup();
    }
    
    /**
     * Initialize the storage manager and the ack queue
     */
    private void recoverySetup() {
        Debug.printError("Beginning startup");
        if (source == null)
        {
            this.lastProcessedWindowRecoveryManager = new RecoveryManager("output_handler_recovery" + "origin" + ".txt");
        this.recoveryManagerForMessagesSentAndNotAcknowledged = new RecoveryManager("not_acked" + "origin" + ".txt");
        this.lastMessageBySenderRecoveryManager = new RecoveryManager("last_message_by_sender" + "origin" + ".txt");
    }
        else {
            this.lastProcessedWindowRecoveryManager =
                    new RecoveryManager("output_handler_recovery" + source.toString()+".txt");
            this.recoveryManagerForMessagesSentAndNotAcknowledged=new RecoveryManager("not_acked"+source.toString()+".txt");
            this.lastMessageBySenderRecoveryManager=new RecoveryManager("last_message_by_sender"+source.toString()+".txt");
        }


        Debug.printVerbose("All managers were created");
    }
    
    //Here i aggregate data
    public void execute() {
        //this condition is a while true loop
    	while (Math.random() < 10) {
            List<DataKey> currentMsg = new LinkedList<>();
            //Put (and remove) at maximum this.size elements into currentMsg

            int itemNeeded = this.size - currentMsg.size();
            //i take n elements from the Queue

                for (int i = 0; i < itemNeeded; i++) {

                        while(sourceMsgQueue.size()<=i)
                        {
                            try{
                                synchronized (sourceMsgQueue)
                                {
                                    sourceMsgQueue.wait();
                                }
                            }
                            catch (InterruptedException e)
                            {
                                Debug.printError(e);
                            }
                        }
                        currentMsg.add(this.sourceMsgQueue.get(i));

                }


            Debug.printVerbose("Starting elaboration of "+currentMsg.size()+" elements");

            float result = this.operationType(currentMsg.stream().map(DataKey::getValue).collect(Collectors.toList()));
            List<Key> senders=currentMsg.stream().map(d->d.getAggregator()).collect(Collectors.toList());

           DataKey messageData = new DataKey(result, new Key(this.source, ++sequenceNumber),senders);
            //I add datas to recovery manager

            while(lastMessageBySenderRecoveryManager==null)
            {
            }

            currentMsg.forEach(msg-> lastProcessedWindowRecoveryManager.appendData(msg));



            changeLastProcessedWindow(messageData);
            DataKey aggregatedOnly=new DataKey(messageData.getData(),messageData.getAggregator(),new ArrayList<>());
            recoveryManagerForMessagesSentAndNotAcknowledged.appendData(aggregatedOnly);
            updateLastMessagesReceivedBySender(messageData.getSources().stream().map(d->new DataKey(0.0,d,new ArrayList<>())).collect(Collectors.toList()));

            executorService.submit(() -> sendMessage(messageData));
            slideWindow();
        }
    }


    /**
     * Acknowledge the messages which will never be needed again, and delete them from the queue
     */
    private void slideWindow()
    {
        Debug.printVerbose(dataSenders.toString());
        List<DataKey> messagesToBeAcknowledgedAndRemoved=this.sourceMsgQueue.subList(0,this.slide);
        if(source!=null)
        messagesToBeAcknowledgedAndRemoved.stream()
                .forEach(msg->msg.getSources().stream()
                        .forEach(src->dataSenders.get(src.getNode()).sendAck(src)));
        synchronized (sourceMsgQueue)
        {
            sourceMsgQueue.removeAll(messagesToBeAcknowledgedAndRemoved);
        }
    }

    /**
     * Modify the file containing the last message received from each socket
     * @param sources
     */
    private void updateLastMessagesReceivedBySender(@NotNull  List<DataKey> sources) {
        this.lastMessageBySenderRecoveryManager.keepOnlyTheMostRecentForEachSource(sources);
    }


    /**
     * Append the given window to the file containing the last processed one, then delete the oldest value.
     * @param messageData
     */
    private void changeLastProcessedWindow(DataKey messageData)
    {
        this.lastProcessedWindowRecoveryManager.appendData(messageData);
        this.lastProcessedWindowRecoveryManager.removeDataOldestValue();
    }

    protected abstract float operationType(List<Float> streamDatas);

  
    

    /**
     * Deploy the task on the operator.
     * This method is called by the ProcessOperator after an operation has been assigned by the MainSupervisor
     */
    public void deploy()
    {
        recoverySetup();
        this.sourceMsgQueue = Collections.synchronizedList(new ArrayList<>());
        executorService = Executors.newCachedThreadPool();
        this.destination = new LinkedList<>();

        //We check if there's no socket description.
        //In case, it means the node is a leaf and we need to write on file the result
        if (messageAddressees.isEmpty())
        {
            //No socket output -> write on file
            this.destination.add(new OutputToFile(this));
        }
        else
        {
            //this node isn't a leaf so we send to all sockets we've collected our result
            for (Position socketRepr : messageAddressees) {
                boolean keepLooping = false;
                do {
                    //for all position we try to open a new socket
                    try {
                        Socket socket = new Socket(socketRepr.getAddress(), socketRepr.getPort());
                        OutputToSocket outputToSocket = new OutputToSocket(socket, this);
                        this.destination.add(outputToSocket);
                        keepLooping = false;

                    } catch (IOException e) {
                        //If socket isn't ready i cycle waiting for it to be ready
                        Debug.printDebug(e);
                        Debug.printDebug("I can't establish connection to the other node input! (OperatorType)");
                        keepLooping = true;
                    }
                }while (keepLooping);
            }
        }

        //todo: è corretta questa cosa?
        //Ciclo creando una coda di output per ogni destination
        for (OperatorOutputQueue operatorOutputQueue : destination) {
            //Starting output threads. They will start to consume their queue. Right now they're waiting for datas
            operatorOutputQueue.start();
        }

        //If source == null it means this is the first node of the graph, and needs to read datas from a file
        //A possible modification could be having an operator spawned automatically and let it handle this thing,
        //for now we assume that the user gives us a well-formed input
        if (source == null)
        {
            InputFromFile inputFromFile = new InputFromFile("src/main/resources/input.txt");
            //Start input threads
            executorService.submit(() -> inputFromFile.startReceiving(this));
            this.execute();
        }
        else{
            //Deploy input from socket. I create a new ServerSocket
            try {
				Debug.printVerbose("Inside second operator");

                ServerSocket serverSocket= new ServerSocket(source.getPort());
                executorService.submit(this::execute);
                //i listen for all DataKey coming from the previous node.
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
            this.sourceMsgQueue.add(messageData);
            synchronized (sourceMsgQueue)
            {
                sourceMsgQueue.notifyAll();
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

    
    public void addDataSender(Position position,InputFromSocket socket)
    {
    	this.dataSenders.put(position, socket);
    }

    public int getSize()
    {
        return this.size;
    }
    public int getSlide()
    {
        return this.slide;
    }
    

    




    private boolean mustWait(){
        try {
            return (!this.outputToSocketFallen.isEmpty());
        }
        catch(NullPointerException e){
            Debug.printError("outPutToSocketFallen is null: maybe you need to update you graphDeployInput.json");
            return false;
        }
    }

    public void stopOutput(OutputToSocket elem){
        this.outputToSocketFallen.add(elem);
    }

    public void restartOutput(OutputToSocket elem){
        this.outputToSocketFallen.remove(elem);
        synchronized (stoppedByCrash) {
            stoppedByCrash.notify();
        }
    }

}