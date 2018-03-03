package operator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Vector;


import operator.recovery.DataKey;
import operator.recovery.RecoveryManager;
import supervisor.Position;
import utils.Debug;

/**
 * This class receives input from one socket and writes it to a file
 */
public class ElementReceiver implements Runnable{
	
	/**
	 * This socket is used to receive input
	 */
	private Socket inputSocket;
	

	/**
	 * This object is used to interact with the storage
	 */
	private RecoveryManager manager;

	
	private Position position;
	/**
	 * Read elements from the given socket and push them to the file with given name
	 * @param socket it's the source of the data
	 * @param ownPosition
	 */
	public ElementReceiver(Socket socket,Position ownPosition)
	{
		this.inputSocket=socket;
		this.manager=null;
		this.position=ownPosition;
	}
	
	@Override
	public void run() {
		while(true)
		{
			try {
				DataKey number=(DataKey)new ObjectInputStream(inputSocket.getInputStream()).readObject();
				if(this.manager==null)
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
