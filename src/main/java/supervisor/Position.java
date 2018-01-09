package supervisor;


/**
 * This class represents the location of operators
 */
public class Position {
	/**
	 * The machine where this position lives
	 */
	private String ipAddress;
	/**
	 * This number is the index of the position in the machine
	 */
	private int positionInMachine;
	
	public Position(String ipAddress, int i) {
		this.ipAddress=ipAddress;
		this.positionInMachine=i;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public int getPositionInMachine() {
		return positionInMachine;
	}
	public void setPositionInMachine(int positionInMachine) {
		this.positionInMachine = positionInMachine;
	}
}
