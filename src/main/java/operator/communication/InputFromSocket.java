package operator.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import operator.recovery.DataKey;
import operator.recovery.Key;
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
	private final BlockingQueue<Key> acksToSend;

	private final Position positionOfTheOtherSide;

	/**
	 * The operator type which will process my data. This parameter is initialized when Input from Socket starts receiving
	 */
	private  OperatorType messagesAddressee;
	
	/**
	 * Read elements from the given socket and push them to the file with given name
	 * @param socket it's the source of the data
	 * @param positionOfTheOtherSide
	 */
	public InputFromSocket(Socket socket, Position positionOfTheOtherSide) throws IOException {
		this.inputSocket=socket;
        this.socketOut = (new ObjectOutputStream(this.inputSocket.getOutputStream()));
		this.socketIn = (new ObjectInputStream(this.inputSocket.getInputStream()));

		this.positionOfTheOtherSide =new Position(socket.getInetAddress().toString(),socket.getPort());
		acksToSend=new LinkedBlockingQueue<>();
		Debug.printVerbose(positionOfTheOtherSide.toString());
	}

	@Override
	public void startReceiving(OperatorType operatorType) {
		messagesAddressee=operatorType;
		new Thread(this::keepSendingAcksWhenReady).start();
		while(true)
		{
			try {

				DataKey messageData = (DataKey) this.socketIn.readObject();
				messageData.setAggregator(positionOfTheOtherSide);
				operatorType.addToMessageQueue(messageData);
				Debug.printVerbose("Received "+ messageData);
			} catch (IOException e) {
				Debug.printError(e);
			} catch (ClassNotFoundException e) {
				Debug.printError(e);
			}
		}
	}
	
	public void sendAck(Key ack)
	{
		try {
			acksToSend.put(ack);
		} catch (InterruptedException e) {
			Debug.printError(e);
		}
	}
	
	public void keepSendingAcksWhenReady()
	{
		while(true)
		{
			try {
				socketOut.writeObject(acksToSend.take());
				Debug.printVerbose("Sending an ack");
			} catch (IOException e) {
				Debug.printError(e);
			} catch (InterruptedException e) {
				Debug.printError(e);
			}
		}
	}

	public Position getOtherSidePosition()
	{
		return positionOfTheOtherSide;
	}
}
