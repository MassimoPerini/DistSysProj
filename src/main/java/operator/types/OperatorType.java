package operator.types;

import java.awt.Container;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import operator.communication.*;
import operator.recovery.DataKey;
import operator.recovery.Key;
import operator.recovery.RecoveryManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supervisor.Position;
import utils.Debug;

import javax.xml.crypto.Data;

/**
 * Created by massimo on 11/02/18.
 *
 * Represents the generic type of operator
 *  source => porta in cui apro il server
 *  listaListe => in ogni sottolista ho i socket in parallelo a cui devo comunicare
 *  exactPosition (dovrebbe diventare una mappa) => sono i socket lato processo quando si fa una connessione
 */
public abstract class OperatorType implements Serializable {
	List<DataKey> messagesRecovered;
	final int size;
	final int slide;
	//this list maps what socket i want to create considering the port into what position i want it to communicate
	final List<Position> exactPosition;
	private int sequenceNumber;

	private final Position source;

	/**
	 * This map is used to retrieve the position of subsequent nodes that sent
	 * an ack. It is updated when new output queues are built. Both this and
	 * messageAddresses are needed because messageAddressees cannot be
	 * serialized (It contains sockets)
	 */
	private transient @NotNull Map<OperatorOutputQueue, List<Position>> destination; // Ho
																						// aggiunto
																						// la
																						// lista

	/**
	 * this variable is used to guarantee to have the same port connection
	 */
	private @NotNull List<Position> portToUseToConnectToPosition;
	private @NotNull List<Integer> portToUseToConnectToPositionPort;

	//private boolean isResendingUnackedMessage;
	//private Object isResendingUnackedMessageLock;
	/**
	 * messageAddressees is the list of the next nodes It is used to initialize
	 * the operator type.
	 */
	private final @NotNull List<List<Position>> messageAddressees;

	private transient ExecutorService executorService;

	// 'String' is the address of the data sender
	// the List is the dataKey structure that the sender sends
	private transient Map<String, List<DataKey>> sourceMsgQueue; // messaggi
																	// input ->
																	// processo
	// private transient Set<String> sourceMsgKeys;

	// list of fallen forward nodes
	//private Set<SingleParallelSocket> outputToSocketFallen;

	/**
	 * The relation between the position of nodes which send me data and their
	 * InputFromSocket.
	 */
	private Map<Position, InputFromSocket> dataSenders;

	/**
	 * This object is used to store the last processed window for each key
	 * -output_handler_recovery-
	 */
	private transient RecoveryManager lastProcessedWindowRecoveryManager;

	/**
	 * This file contains all the messages that must be stored because
	 * addressees haven't received them yet. Semantics: messages are appended
	 * without their sources. Each source represents a node having acknowledged
	 * the message. The list of sources is updated when acks are received.
	 */
	private transient RecoveryManager recoveryManagerForMessagesSentAndNotAcknowledged;

	private transient RecoveryManager lastMessageBySenderRecoveryManager;
	/*
	 * public OperatorType(){ this.messageAddressees = new LinkedList<>();
	 * this.size = 0; this.slide = 0; }
	 */
    /*
        source => porta in cui apro il server
        listaListe => in ogni sottolista ho i socket in parallelo a cui devo comunicare
        exactPosition (dovrebbe diventare una mappa) => sono i socket lato processo quando si fa una connessione

     */
	public OperatorType(@NotNull List<List<Position>> messageAddressees, int size, int slide, @Nullable Position socket,
                        List<Position> exactPosition) {
		this.messageAddressees = messageAddressees;
		this.size = size;
		this.slide = slide;
		this.exactPosition = exactPosition;
		this.source = socket;
		//this.isResendingUnackedMessage = Boolean.FALSE;
		dataSenders = new HashMap<>();
		//outputToSocketFallen = new HashSet<>();
		// this.portToUseToConnectToPosition = new HashMap<>();
		// this.destination = new HashMap<>();
		recoverySetup();
	}

	/**
	 * Initialize the storage manager and the ack queue. Necessary because gson
	 * doesn't invoke constructor
	 */
	private void recoverySetup() {
		if (source.getPort() < 0) {
			this.lastProcessedWindowRecoveryManager = new RecoveryManager(
					"output_handler_recovery" + "origin" + ".txt");
			this.recoveryManagerForMessagesSentAndNotAcknowledged = new RecoveryManager(
					"not_acked" + "origin" + ".txt");
			this.lastMessageBySenderRecoveryManager = new RecoveryManager("last_message_by_sender" + "origin" + ".txt");
		} else {
			this.lastProcessedWindowRecoveryManager = new RecoveryManager(
					"output_handler_recovery" + source.toStringFile() + ".txt");
			this.recoveryManagerForMessagesSentAndNotAcknowledged = new RecoveryManager(
					"not_acked" + source.toStringFile() + ".txt");
			this.lastMessageBySenderRecoveryManager = new RecoveryManager(
					"last_message_by_sender" + source.toStringFile() + ".txt");
		}
		Logger logger = LogManager.getLogger();
		logger.debug("All managers were created");
	}

	// Here i aggregate data
	public void execute() {
		/*
		 * How does parallelization works We have two maps. sourceMsgQueue is
		 * shared among all threads changedKeys is just owned by the proper
		 * thread
		 */

		// this condition is a while true loop
		// mappa contenente i messaggi da processare
		Map<String, List<DataKey>> currentMsg = new HashMap<>();

		Logger logger = LogManager.getLogger();


		if(!messagesRecovered.isEmpty())
		{
			logger.debug("recovery file is NOT empty");

			for (DataKey dataKey : messagesRecovered) {
				logger.debug("adding data....");
				addToMessageQueue(dataKey);
			}
			//i empty the list
			messagesRecovered = new LinkedList<>();
		}
		else
			logger.debug("recovery file IS empty");

int cont=0;
		do{
			// Put (and remove) at maximum this.size elements into currentMsg
			// id dei messaggi nuovi
			List<String> changedKeys = new LinkedList<>();

			/*synchronized (isResendingUnackedMessageLock)
			{
				logger.debug(("Checking unacked messages"));

				if(isResendingUnackedMessage) {
					logger.debug(("Sending unacked messages"));
					isResendingUnackedMessage = false;
					this.resendUnackedMessages();

				}
			}*/


			synchronized (sourceMsgQueue) {
				// se la mappa condivisa è vuota aspetta


				while (this.sourceMsgQueue.size() == 0)
				{
					try {
						sourceMsgQueue.wait();
					} catch (InterruptedException e) {
						logger.error(e);
					}
				}
				// here i take values from the global map
				for (Map.Entry<String, List<DataKey>> stringListEntry : this.sourceMsgQueue.entrySet()) { // mappa
																											// condivisa
																											// ->
																											// mappa
																											// del
																											// thread
					if (currentMsg.containsKey(stringListEntry.getKey())) {
						currentMsg.get(stringListEntry.getKey()).addAll(stringListEntry.getValue());
					} else {
						currentMsg.put(stringListEntry.getKey(), stringListEntry.getValue());
					}
					changedKeys.add(stringListEntry.getKey());
				}
				this.sourceMsgQueue.clear(); // una volta che ho "scaricato" la
												// mappa condivisa la svuoto
			}

			// ---- ended copy, now aggregates
			// Map<String, List<DataKey>> results = new HashMap<>();
			ThreadContext.put("logFileName", "operator"+Debug.getUuid());


			// printing data
			for (Map.Entry<String, List<DataKey>> stringListEntry : currentMsg.entrySet()) {
				logger.trace(stringListEntry.getKey());
				for (DataKey dataKey : stringListEntry.getValue()) {
					logger.trace("-> " + dataKey.getData());

				}
			}

			// For each tuples i remember the keys i added into the local map,
			// to optimize
			for (String changedKey : changedKeys) { // aggrego i dati per chiave
				List<DataKey> data = currentMsg.get(changedKey);
				// for those data i check if they changed
				// i check if i can pick enough data with window size
				while (data.size() >= this.size) {
					// i take a list of Datakeys
					List<DataKey> subRes = data.subList(0, this.size);
					float result = this
							.operationType(subRes.stream().map(DataKey::getData).collect(Collectors.toList()));
					List<Key> senders = subRes.stream().map(DataKey::getAggregator).collect(Collectors.toList());
					// this is the aggregated result:
					DataKey messageData = new DataKey(changedKey, result, new Key(this.source, ++sequenceNumber),
							senders);

					logger.debug("\" result: \" + changedKey + \" : \" + result" + messageData.getData());

					// 1- Appending processed data to -output_handler_recovery-
					changeLastProcessedWindow(messageData);

					DataKey aggregatedOnly = new DataKey(messageData.getOriginalKey(), messageData.getData(),
							messageData.getAggregator(), new ArrayList<>());

					// 2- Appending data not aggregated to not acked file
					recoveryManagerForMessagesSentAndNotAcknowledged.appendData(aggregatedOnly);

					// 3- we tell the recovery manager the keys of the new
					// arrived elements
					
					/*
					updateLastMessagesReceivedBySender(messageData.getSources().stream()
							.map(d -> new DataKey(messageData.getOriginalKey(), messageData.getData(), d, new ArrayList<>()))
							.collect(Collectors.toList()));
					*/
					updateLastMessagesReceivedBySender(subRes);
					slideWindow(data);
					logger.debug("Window slide called" );

					executorService.submit(() -> {
                        Debug.increaseMessageSent(1);
                        sendMessage(messageData);
                    });
					//executorService.submit(() -> sendMessage(messageData));

				}
			}
			logger.debug("end of a aggregation");

			// send

			/*
			 * int itemNeeded = this.size - currentMsg.size(); //i take n
			 * elements from the Queue
			 * 
			 * for (int i = 0; i < itemNeeded; i++) {
			 * 
			 * while(sourceMsgQueue.size()<=i) { try{ synchronized
			 * (sourceMsgQueue) { sourceMsgQueue.wait(); } } catch
			 * (InterruptedException e) { Debug.printError(e); } }
			 * currentMsg.add(this.sourceMsgQueue.get(i));
			 * 
			 * }
			 * 
			 * Debug.printVerbose("Starting elaboration of "+currentMsg.size()
			 * +" elements");
			 * 
			 * float result =
			 * this.operationType(currentMsg.stream().map(DataKey::getValue).
			 * collect(Collectors.toList())); List<Key>
			 * senders=currentMsg.stream().map(d->d.getAggregator()).collect(
			 * Collectors.toList());
			 * 
			 * DataKey messageData = new DataKey(result, new Key(this.source,
			 * ++sequenceNumber),senders); //I add datas to recovery manager
			 */

			// we use this to allow precedent optimization
			changedKeys.clear();
			// results.clear();
			cont++;
		}while (Math.random() < 2) ;
	}

	/**
	 * Acknowledge window size Delete window slide from memory
	 *
	 * @param data
	 */
	private void slideWindow(List<DataKey> data) {
		List<DataKey> removeEl = data.subList(0, this.slide);
		List<DataKey> toSendAck = data.subList(0, this.size);

		if (source.getPort() > 0) {
			for (DataKey key : toSendAck) {
				//dataSenders.get(key.getAggregator().getNode()).sendAck(key.getAggregator());
				dataSenders.get(key.getAggregator().getNode()).sendAck(key);
			}
		}
		removeEl.clear();
	}

	/**
	 * Modify the file containing the last message received from each socket
	 *
	 * @param sources
	 */
	private void updateLastMessagesReceivedBySender(@NotNull List<DataKey> sources) {
		this.lastMessageBySenderRecoveryManager.keepOnlyTheMostRecentForEachSource(sources);
	}

	/**
	 * Append the given window to the file containing the last processed one,
	 * then delete the oldest value.
	 *
	 * @param messageData
	 */
	private void changeLastProcessedWindow(DataKey messageData) {
		this.lastProcessedWindowRecoveryManager.appendData(messageData);
		this.lastProcessedWindowRecoveryManager.removeDataOldestValueByKey(messageData);
	}

	protected abstract float operationType(List<Float> streamDatas);

	/**
	 * Deploy the task on the operator. This method is called by the
	 * ProcessOperator after an operation has been assigned by the
	 * MainSupervisor
	 */
	public void deploy() {
		//getLastWindowProcessed
		recoverySetup();

		//this.isResendingUnackedMessageLock = new Object();
		this.messagesRecovered = lastProcessedWindowRecoveryManager.getAllOrEmptyList();
		List<DataKey> lastMessagesBySender = lastMessageBySenderRecoveryManager.getAllOrEmptyList();
		//è last message by sender
		if(!lastMessagesBySender.isEmpty())
		{
			int temp = 0;
			for(DataKey seqNumber:lastMessagesBySender)
				temp += seqNumber.getAggregator().getSequenceNumber();
			//non so se necessario...
			this.sequenceNumber = (int)Math.floor(temp / this.slide)+1;
		}
		this.sourceMsgQueue = new ConcurrentHashMap<>();
		// this.sourceMsgKeys = Collections.newSetFromMap(new
		// ConcurrentHashMap<String, Boolean>());

		Logger logger = LogManager.getLogger();

		executorService = Executors.newCachedThreadPool();
		this.destination = new HashMap<>();

		// We check if there's no socket description.
		// In case, it means the node is a leaf and we need to write on file the
		// result
		if (messageAddressees.isEmpty()) {
			// No socket output -> write on file
			this.destination.put(new OutputToFile(this), null);
		} else {
			int count = 0;
			for (List<Position> messageAddressee : messageAddressees) {
				// this is an output
				List<Socket> sockets = new LinkedList<>();
				for (Position position : messageAddressee) {
					boolean keepLooping = false;
					do {
						// for all position we try to open a new socket
						try {
							/*
							 * Position exactPosition = source==null? new
							 * Position(InetAddress.getLocalHost().
							 * getCanonicalHostName(), 12345):source;
							 */

							Socket socket = new Socket();
							socket.setReuseAddress(true);
							Position currentPointingPosition = this.exactPosition.get(count);
							logger.debug(currentPointingPosition.toString());
							socket.bind(new InetSocketAddress(currentPointingPosition.getAddress(),
                                    currentPointingPosition.getPort()));
							socket.setReuseAddress(true);

							socket.connect(new InetSocketAddress(position.getAddress(), position.getPort()));
							socket.setReuseAddress(true);


							sockets.add(socket);
							logger.error(socket.toString());
							count++;
							keepLooping = false;
						} catch (IOException e) {
							// If socket isn't ready i cycle waiting for it to
							// be ready
							logger.error(e);
							// todo: check here - ogni tanto mi compariva questo
							// errore
							logger.error("I can't establish connection to the other node input! (OperatorType)");
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e1) {
								Debug.printDebug(e1);
							}
							keepLooping = true;
						}
					} while (keepLooping);
				}

				OutputToSocket outputToSocket = new OutputToSocket(sockets, this);
				this.destination.put(outputToSocket, messageAddressee);
			}
		}

		// Ciclo creando una coda di output per ogni destination
		for (OperatorOutputQueue operatorOutputQueue : destination.keySet()) {
			// Starting output threads. They will start to consume their queue.
			// Right now they're waiting for datas
			operatorOutputQueue.start();
		}
		resendUnackedMessages();

		//isResendingUnackedMessage = false;

		// If source == null it means this is the first node of the graph, and
		// needs to read datas from a file
		// A possible modification could be having an operator spawned
		// automatically and let it handle this thing,
		// for now we assume that the user gives us a well-formed input
		if (source.getPort() < 0) {
			InputFromFile inputFromFile = new InputFromFile("src/main/resources/input.txt");
			// Start input threads
			executorService.submit(() -> inputFromFile.startReceiving(this));
			this.execute();
		} else {
			// Deploy input from socket. I create a new ServerSocket
			try {
				logger.debug("Inside second operator");
				ServerSocket serverSocket = new ServerSocket(source.getPort());
				serverSocket.setReuseAddress(true);
				executorService.submit(this::execute);
				// i listen for all DataKey coming from the previous node.
				while (true) {
					logger.debug("Start accepting new incoming connections!");
					Socket socket = serverSocket.accept();
					socket.setReuseAddress(true);
					logger.debug("A new socket input!");
					InputFromSocket receiver = new InputFromSocket(socket, source);
					// rimuovere dalla mappa
					dataSenders.put(receiver.getOtherSidePosition(), receiver);
					logger.trace(dataSenders.toString());
					executorService.submit(() -> receiver.startReceiving(this));
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	/**
	 * Load from file all the messages previously sent and not fully acked, and resend them.
	 */
	public void resendUnackedMessages()
	{
		List<DataKey> notAcked=recoveryManagerForMessagesSentAndNotAcknowledged.getAllOrEmptyList();
		Logger logger = LogManager.getLogger();
		ThreadContext.put("logFileName", "operator"+Debug.getUuid());
		logger.debug("Inside resend Unacked messages");
		Set<String> differentKeys=notAcked.stream().map(ms->ms.getOriginalKey()).distinct().collect(Collectors.toSet());
		for(String string:differentKeys)
		{
			List<DataKey> current = reorderMessages(notAcked, string);
			for(int i = 0; i< current.size(); i++)
				sendMessage(current.get(i));
		}
	}

	public static List<DataKey> reorderMessages(List<DataKey> notAcked, String originalKey) {
		List<DataKey> current = notAcked.stream().filter(msg -> msg.getOriginalKey().equals(originalKey))
				.sorted((m1, m2) -> +m1.getAggregator().getSequenceNumber() - m2.getAggregator().getSequenceNumber())
				.collect(Collectors.toList());
		return current;
	}

	public void addToMessageQueue(DataKey messageData) {
		// this.sourceMsgQueue.put(messageData.getOriginalKey(), messageData);
		// this.sourceMsgKeys.add(messageData.getOriginalKey());
		// this.sourceMsgKeys.put(messageData.getOriginalKey());
		// this.sourceMsgQueue.add(messageData);


		if (Debug.getCrashRecPosition().contains(this.source) && Debug.getCrashRecNMessage().contains(Debug.getMessageReceived()))
		{
			Logger logger = LogManager.getLogger();
			//ThreadContext.put("logFileName", "operator"+Debug.getUuid());
			logger.info("Crashing on purpose....");
			System.exit(-2);
		}
		Debug.setMessageReceived(Debug.getMessageReceived()+1);

		if(lastMessageBySenderRecoveryManager.isDuplicated(messageData) && source.getPort() > 0)
		{
				//dataSenders.get(key.getAggregator().getNode()).sendAck(key.getAggregator());
			dataSenders.get(messageData.getAggregator().getNode()).sendAck(messageData);
			return;
		}

		synchronized (sourceMsgQueue) {
			if (this.sourceMsgQueue.containsKey(messageData.getOriginalKey())) {
				this.sourceMsgQueue.get(messageData.getOriginalKey()).add(messageData);
			} else {
				List<DataKey> dataKeys = new LinkedList<>();
				dataKeys.add(messageData);
				this.sourceMsgQueue.put(messageData.getOriginalKey(), dataKeys);
			}

			sourceMsgQueue.notifyAll();
		}
	}

	private void sendMessage(DataKey messageData) {
		Logger logger = LogManager.getLogger();
		ThreadContext.put("logFileName", "operator"+Debug.getUuid());

		logger.info("COUNTER: "+Debug.getMessageSent());

		if (Debug.getCrashSendPosition().contains(this.source) && Debug.getCrashSendNMessage().contains(Debug.getMessageSent()))
		{
			logger.info("Crashing on purpose....");
			System.exit(-2);
		}

		for (OperatorOutputQueue operatorOutputQueue : destination.keySet()) {
			logger.trace(destination.toString());
			logger.error("Sto mandando a " + destination.toString() + messageData.toString(), this.source.toString());
			operatorOutputQueue.send(messageData);

		}
		// TODO output the message (a new thread wants to send this message)
	}

	public void addDataSender(Position position, InputFromSocket socket) {
		this.dataSenders.put(position, socket);
	}

	public int getSize() {
		return this.size;
	}

	public int getSlide() {
		return this.slide;
	}

	public void canResendUnackedMessage()
	{
		/*
		synchronized (isResendingUnackedMessageLock)
		{

			Debug.printVerbose("inside synch to update boolean");
			isResendingUnackedMessage = true;
		}
		*/
		executorService.submit(this::resendUnackedMessages);
	}

	/*public void stopOutput(SingleParallelSocket elem) {
		this.outputToSocketFallen.add(elem);
	}

	public void restartOutput(SingleParallelSocket elem) {
		this.outputToSocketFallen.remove(elem);
		this.resendUnackedMessages();
	}*/

	/**
	 * Update the list of nodes that have received a certain message. If last,
	 * delete it from file
	 *
	 * @param receivedAck
	 *            the message I sent before and that has now been acknowledged
	 * @param outputToSocket
	 *            the socket I used to send the message (equivalent: the node to
	 *            which I sent it)
	 */
	public void manageAck(String originalKey,Key receivedAck, SingleParallelSocket outputToSocket) {
		Logger logger = LogManager.getLogger();
		logger.debug("Ack received: " + receivedAck);
		Position ackSenderPosition = outputToSocket.getOtherSideAddress();
		List<Position> allSubsequentNodes = new ArrayList<>();
		for (List<Position> curr : this.destination.values()) {
			allSubsequentNodes.addAll(curr);
		}
		this.recoveryManagerForMessagesSentAndNotAcknowledged.reactToAck(originalKey,receivedAck, ackSenderPosition,
				allSubsequentNodes);

	}

	public void setPortToUseToConnectToPosition(List<Position> portToUseToConnectToPosition,
			List<Integer> portToUseToConnectToPort) {
		this.portToUseToConnectToPosition = portToUseToConnectToPosition;
		this.portToUseToConnectToPositionPort = portToUseToConnectToPort;
	}

    public Position getSource() {
        return source;
    }
}