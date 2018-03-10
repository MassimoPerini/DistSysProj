package operator.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Vector;

import operator.communication.OperatorInputQueue;
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
	public InputFromSocket(Socket socket, Position ownPosition)
	{
		this.inputSocket=socket;
		this.manager=null;
		this.position=ownPosition;
	}

	@Override
	public void startReceiving(OperatorType operatorType) {
		this.addressee=operatorType;
		while(true)
		{
			try {
				DataKey number=(DataKey)new ObjectInputStream(inputSocket.getInputStream()).readObject();
				if(this.manager==null)		//TODO non è meglio metterlo nel costruttore?
					this.manager=new RecoveryManager(position.toString()+number.getSenderPosition().toString()+"arrival.txt");
				manager.appendData(number);
			} catch (IOException e) {
				Debug.printError(e);
			} catch (ClassNotFoundException e) {
				Debug.printError(e);
			}
		}
	}
	
	
}
