package operator.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

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

	/**
	 * This object is used to interact with the storage
	 */
	private RecoveryManager manager;

	private OperatorType addressee;
	
	private final Position position;
	/**
	 * Read elements from the given socket and push them to the file with given name
	 * @param socket it's the source of the data
	 * @param ownPosition
	 */
	public InputFromSocket(Socket socket, Position ownPosition) throws IOException {
		this.inputSocket=socket;
        this.socketOut = (new ObjectOutputStream(this.inputSocket.getOutputStream()));
		this.socketIn = (new ObjectInputStream(this.inputSocket.getInputStream()));

		//TODO PERCHE'?
		this.manager=null;
		this.position=ownPosition;
	}

	@Override
	public void startReceiving(OperatorType operatorType) {
		this.addressee=operatorType;
		while(true)
		{
			try {

				MessageData messageData = (MessageData) this.socketIn.readObject();
				operatorType.addToMessageQueue(messageData);
				System.out.println("Received "+ messageData);

				/*
				DataKey number=(DataKey)new ObjectInputStream(inputSocket.getInputStream()).readObject();
				if(this.manager==null)		//TODO non è meglio metterlo nel costruttore?
					this.manager=new RecoveryManager(position.toString()+number.getSenderPosition().toString()+"arrival.txt");
				manager.appendData(number);
				*/
			} catch (IOException e) {
				Debug.printError(e);
			} catch (ClassNotFoundException e) {
				Debug.printError(e);
			}
		}
	}
	
	
}
