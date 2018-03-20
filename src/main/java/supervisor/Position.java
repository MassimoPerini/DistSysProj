package supervisor;

import java.io.Serializable;
import java.net.SocketImpl;

import operator.types.SocketRepr;

//todo: position forse è IP+PORTA
/**
 * This class represents the location of operators
 */
public class Position implements Serializable{
	/**
	 * The machine where this position lives
	 */
	private final String ipAddress;
	/**
	 * This number is the index of the position in the machine
	 */
	//private final int positionInMachine;
	
	private final int port;

	public Position(String ipAddress, int i) {
		this.ipAddress=ipAddress;
		this.port=i;
		//this is a random index
		//this.positionInMachine = 1;
	}

	/*
	public Position(String ipAddress, int port, int i) {
		this.ipAddress=ipAddress;
		this.port = port;
		this.positionInMachine=i;
	}*/

	public String getAddress() {
		return ipAddress;
	}

	/*public int getPositionInMachine() {
		return positionInMachine;
	}
	*/
	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return  this.ipAddress + this.port;
	}
}
