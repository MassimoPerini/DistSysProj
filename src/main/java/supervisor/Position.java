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
	private String ipAddress;
	/**
	 * This number is the index of the position in the machine
	 */
	private int positionInMachine;
	
	private int port;
	
	public Position(String ipAddress, int i) {
		this.ipAddress=ipAddress;
		this.positionInMachine=i;
	}

	public Position(String ipAddress, int port, int i) {
		this.ipAddress=ipAddress;
		this.port = port;
		this.positionInMachine=i;
	}
	//TODO
	public Position(SocketRepr repr)
	{
		
	}
	public String getAddress() {
		return ipAddress;
	}
	public void setAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public int getPositionInMachine() {
		return positionInMachine;
	}
	public void setPositionInMachine(int positionInMachine) {
		this.positionInMachine = positionInMachine;
	}
	public int getPort() {
		return port;
	}
}
