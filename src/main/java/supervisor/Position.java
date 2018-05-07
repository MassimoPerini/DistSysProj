package supervisor;

import java.io.Serializable;
import java.net.SocketImpl;

import operator.types.SocketRepr;


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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Position position = (Position) o;

		if (port != position.port) return false;
		return ipAddress.equals(position.ipAddress);
	}

	@Override
	public int hashCode() {
		int result = ipAddress.hashCode();
		result = 31 * result + port;
		return result;
	}

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
		return  this.ipAddress +"--"+ this.port;
	}

	public String toStringFile(){
		return this.ipAddress.toString().replace('.', '_') + '-' + this.port;
	}
}
