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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
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
	private final BlockingQueue<DataKey> acksToSend;

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
		Logger logger = LogManager.getLogger();
		ThreadContext.put("logFileName", "operator"+Debug.getUuid());

		new Thread(this::keepSendingAcksWhenReady).start();
		while(true)
		{
			try {

				DataKey messageData = (DataKey) this.socketIn.readObject();
				messageData.setAggregator(positionOfTheOtherSide);
				operatorType.addToMessageQueue(messageData);
				logger.debug("Received "+ messageData + "with key" + messageData.getOriginalKey());


			} catch (IOException e) {
				Debug.printError("InputFromSocket IOException"+e);
				this.finish();
				return;

			} catch (ClassNotFoundException e) {
				Debug.printError(e);
			}
		}
	}
	
	public void sendAck(DataKey ack)
	{
		try {
			acksToSend.put(ack);
		} catch (InterruptedException e) {
			Debug.printError(e);
		}
	}
	
	public void keepSendingAcksWhenReady()
	{
		Logger logger = LogManager.getLogger();
		while(true)
		{
			try {
				socketOut.writeObject(acksToSend.take());

				logger.trace("Sending an ack ");
			} catch (IOException e) {
				Debug.printError("Sending an ack "+e);
			} catch (InterruptedException e) {
				Debug.printError(e);
			}
		}
	}

	private void finish()
	{
		try {
			inputSocket.close();
			socketIn.close();
			socketOut.close();
		} catch (IOException e) {
			Debug.printError(e);
		}
	}

	public Position getOtherSidePosition()
	{
		return positionOfTheOtherSide;
	}
}
