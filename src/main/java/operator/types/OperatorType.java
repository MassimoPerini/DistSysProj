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

    /**
     * This map is used to retrieve the position of subsequent nodes that sent an ack.
     * It is updated when new output queues are built.
     * Both this and messageAddresses are needed because messageAddressees cannot be serialized (It contains sockets)
     */
    private transient @NotNull Map<OperatorOutputQueue,Position> destination;
    /**
     * messageAddressees is the list of the next nodes
     * It is used to initialize the operator type.
     */
    private final @NotNull List<Position> messageAddressees;


    private transient ExecutorService executorService;
    private final Object stoppedByCrash = new Object();

    private transient List<DataKey> sourceMsgQueue; //messaggi input -> processo
    //list of fallen forward nodes
    private Set<OutputToSocket> outputToSocketFallen;

    /**
     * The relation between the position of nodes which send me data and their InputFromSocket.
     */
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
                Debug.printError("Still null");
            }

            currentMsg.forEach(msg-> lastProcessedWindowRecoveryManager.appendData(msg));


            changeLastProcessedWindow(messageData);
            DataKey aggregatedOnly=new DataKey(messageData.getData(),messageData.getAggregator(),new ArrayList<>());
            recoveryManagerForMessagesSentAndNotAcknowledged.appendData(aggregatedOnly);
            updateLastMessagesReceivedBySender(messageData.getSources().stream().map(d->new DataKey(0.0,d,new ArrayList<>())).collect(Collectors.toList()));

            slideWindow();

            executorService.submit(() -> sendMessage(messageData));
        }
    }


    /**
     * Acknowledge the messages which will never be needed again, and delete them from the queue
     */
    private void slideWindow()
    {

        List<DataKey> messagesToBeAcknowledgedAndRemoved=this.sourceMsgQueue.subList(0,this.slide);
        if(source!=null)
        {
            for(DataKey key:messagesToBeAcknowledgedAndRemoved)
            {
                    dataSenders.get(key.getAggregator().getNode()).sendAck(key.getAggregator());
            }
        }
        sourceMsgQueue.removeAll(messagesToBeAcknowledgedAndRemoved);

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
        this.destination = new HashMap<>();

        //We check if there's no socket description.
        //In case, it means the node is a leaf and we need to write on file the result
        if (messageAddressees.isEmpty())
        {
            //No socket output -> write on file
            this.destination.put(new OutputToFile(this),null);
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
                        this.destination.put(outputToSocket,socketRepr);
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
        for (OperatorOutputQueue operatorOutputQueue : destination.keySet()) {
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
					dataSenders.put(receiver.getOtherSidePosition(),receiver);
					Debug.printVerbose(dataSenders.toString());
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
        for (OperatorOutputQueue operatorOutputQueue : destination.keySet()) {
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

    /**
     * Update the list of nodes that have received a certain message. If last, delete it from file
     * @param receivedAck the message I sent before and that has now been acknowledged
     * @param outputToSocket the socket I used to send the message (equivalent: the node to which I sent it)
     */
    public void manageAck(Key receivedAck, OutputToSocket outputToSocket)
    {
        Debug.printVerbose("Ack received: "+receivedAck);
        Position ackSenderPosition=this.destination.get(outputToSocket);
        this.recoveryManagerForMessagesSentAndNotAcknowledged.reactToAck(receivedAck,ackSenderPosition,this.destination.values());
    }

}