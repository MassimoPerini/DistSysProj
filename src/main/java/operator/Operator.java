package operator;

import java.net.Socket;

/**
 * This class is connected to a Supervisor through a Socket.
 * From that connection it is ordered what operations perform and how.
 */
public class Operator implements Runnable{
	/**
	 * The connection from which setup arrives
	 */
	private Socket supervisorSocket;
	
	/**
	 * This constructor instances an Operator specifying the connection to its Supervisor
	 * @param socket
	 */
	public Operator(Socket socket)
	{
		this.supervisorSocket=socket;
	}
	
	/**
	 * The main part of an operator's life cycle, consisting in waiting for incoming setup requests and for 
	 * numbers.
	 */
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
