package operator.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import operator.communication.OperatorInputQueue;
import operator.communication.message.MessageData;
import operator.recovery.DataKey;
import operator.recovery.RecoveryManager;
import operator.types.OperatorType;
import supervisor.Position;
import utils.Debug;

/**
 * This class receives input from one socket and writes it to a file
 */
public class InputFromSocket implements OperatorInputQueue{
	
	/**
	 * This socket is used to receive input
	 */
	private final Socket inputSocket;
	private final ObjectInputStream socketIn;
	private final ObjectOutputStream socketOut;
	private final BlockingQueue<Ack> acksToSend;
	

	
	/**
	 * Read elements from the given socket and push them to the file with given name
	 * @param socket it's the source of the data
	 * @param ownPosition
	 */
	public InputFromSocket(Socket socket, Position ownPosition) throws IOException {
		this.inputSocket=socket;
        this.socketOut = (new ObjectOutputStream(this.inputSocket.getOutputStream()));
		this.socketIn = (new ObjectInputStream(this.inputSocket.getInputStream()));

	
		acksToSend=new LinkedBlockingQueue<>();
	}

	@Override
	public void startReceiving(OperatorType operatorType) {
		new Thread(this::keepSendingAcksWhenReady).start();
		while(true)
		{
			
			try {

				DataKey messageData = (DataKey) this.socketIn.readObject();
				operatorType.addToMessageQueue(messageData);
				Debug.printVerbose("Received "+ messageData);
			} catch (IOException e) {
				Debug.printError(e);
			} catch (ClassNotFoundException e) {
				Debug.printError(e);
			}
		}
	}
	
	public void sendAck(Ack ack)
	{
		try {
			acksToSend.put(ack);
		} catch (InterruptedException e) {
			Debug.printError(e);
		}
	}
	
	public void keepSendingAcksWhenReady()
	{
		try {
			socketOut.writeObject(acksToSend.take());
		} catch (IOException e) {
			Debug.printError(e);
		} catch (InterruptedException e) {
			Debug.printError(e);
		}
	}
}
