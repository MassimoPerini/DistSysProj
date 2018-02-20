package supervisor;

/**
 * This exception is thrown whenever a user tries to deploy a graph and no daemon has declared his
 * availability to the supervisor
 */
public class NoDaemonAvailableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7347593845088028408L;

}
