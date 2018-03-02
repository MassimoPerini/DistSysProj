package operator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.types.OperatorType;
import operator.types.Sum;
import utils.Debug;

/**
 * Created by massimo on 22/12/17.
 */
public class ProcessOperator {
	
	/**
	 * Upon launch, create an operator according to the json in input. 
	 * Then open a socket and start listening for incoming elements to process.
	 * Open also one socket for each process which must receive the result of the computetion
	 * @param args a json object representing the operator to instantiate
	 */
    public static void main(String [] args)
    {
        Debug.setLevel(Debug.LEVEL_VERBOSE);
        Debug.printVerbose("Processo lanciato!!! " + args[0]);

        RuntimeTypeAdapterFactory<OperatorType> rtTest = RuntimeTypeAdapterFactory.of(OperatorType.class, "type").registerSubtype(Sum.class);
        Gson gson  = new GsonBuilder().registerTypeAdapterFactory(rtTest).create();
        OperatorType o = gson.fromJson(args[0], OperatorType.class);
        Debug.printVerbose("Operazione assegnata: "+o.toString());
        
        ExecutorService executor=Executors.newCachedThreadPool();
        
        try {
			ServerSocket serverSocket=new ServerSocket(o.getOwnPort().getPort());
			while(true)
			{
				Socket socket=serverSocket.accept();
				ElementReceiver receiver=new ElementReceiver(socket,o.getOwnPort());
				executor.submit(receiver);
			}
			
		} catch (IOException e) {
			Debug.printDebug(e);
		}
        
    }
    
    
}
