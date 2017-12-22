package operator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import helpers.Printer;


/**
 * Created by higla on 21/12/2017.
 * This class waits for a supervisor to start a connection. 
 * Upon connection, a thread to handle requests incoming from the supervisor is created.
 */
public class MainDaemon 
{
	/**
	 * The port to which supervisor's requests should arrive
	 */
	public static final int SUPERVISOR_PORT=8848;
	
	/**
	 * The timeout for the socket connection
	 */
	public static final int TCP_TIMEOUT=50;
	
	/**
	 * The socket server who waits for incoming connection requests
	 */
	private ServerSocket serverSocket;
	
	/**
	 * All the operators relying upon this daemon
	 */
	private Set<Operator> allocatedOperators;
	
	/**
	 * This boolean is used to gracefully terminate the operator: when set to true, 
	 * the allocated operators and the server itself will be terminated
	 */
	private boolean stop;
	
	/**
	 * This constructor just initialize the daemon.
	 */
	public MainDaemon()
	{
		this.allocatedOperators=new HashSet<Operator>();
	}
	
	/**
	 * Start waiting for connection requests incoming to the specified port
	 */
	public void start()
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		Printer.out("Starting operator...", Printer.OK);
		try {
			this.serverSocket = new ServerSocket(SUPERVISOR_PORT);
			Printer.out("Waiting for requests...", Printer.OK);
			do {
				try {
					Socket socket = serverSocket.accept();
					socket.setSoLinger(true	,TCP_TIMEOUT);
					Operator operator=new Operator(socket);
					allocatedOperators.add(operator);
					executor.submit(new Thread(operator));
				} catch(IOException e) 
				{
					Printer.out("Error while accepting connection: "+e.getMessage(),Printer.MEDIUM);
					break;
				}
			}while(!stop);
		
		executor.shutdown();
		
		serverSocket.close();
		}catch(Exception e){
			Printer.out("Unable to shut down socket server",Printer.MEDIUM);
		}
		Printer.out("Socket server was shut down",Printer.OK);
	}
	
}
