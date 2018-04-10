package supervisor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import supervisor.communication.SocketListener;
import supervisor.communication.SocketManager;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Graph;
import supervisor.graph_representation.Vertex;
import utils.CliPrinter;
import utils.Debug;

/**
 * This class contains a socketListener to list all the available daemons.
 * Upon user request, it deploys the graph given in input over the aforementioned daemons.
 */
public class GraphDeployer implements Runnable{
	
	/**
	 * The available daemons
	 */
	private SocketManager socketManager;
	
	/**
	 * @param manager the component in charge of waiting for daemons to connect
	 */
	public GraphDeployer(SocketManager manager)
	{
		this.socketManager=manager;
	}
	
	/**
	 * Wait for the user to input a graph; then, deploy it over the available daemons
	 * Currently, the graph must be input in the form of a json text (eg, {"vertices":{}} )
	 */
	@Override
	public void run() {
        String FILENAME = "graphDeployInput.json";
		FileReader fr = null;
		BufferedReader br  = null;
		StringBuilder inputGraphDeploy = new StringBuilder();
		try {

			//br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				inputGraphDeploy.append(sCurrentLine);
			}
			Debug.printVerbose(inputGraphDeploy.toString());
		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}



		//Scanner scanner=new Scanner(System.in);
		Debug.printVerbose("Waiting for JSON input");
        //String iString= scanner.nextLine();
		String iString = inputGraphDeploy.toString();
		if(iString != null) {
			Debug.printVerbose("JSON received");
			Type fooType = new TypeToken<Graph<OperatorDeployment>>() {
			}.getType();
			Debug.printVerbose("Token generated");
			Graph<OperatorDeployment> graph = new Gson().fromJson(iString, fooType);
			Debug.printVerbose("GSON processed" + graph.toString());
			List<Vertex<OperatorDeployment>> sortedGraph = graph.topologicalSort();
			if (sortedGraph != null) {
				try {
					deploy(sortedGraph);
					Debug.printVerbose("Deployed sorted graph");
                    Debug.printVerbose("Starting heartbeat");
                    socketManager.startHeartBeat(sortedGraph);
                } catch (NoDaemonAvailableException e) {
					try {
						Debug.printError("No daemon is currently available, waiting for 10 seconds in order to receiver all daemons");
						/*Thread.sleep((long) (10000));
						deploy(sortedGraph);*/
					}catch(Exception err){
						Debug.printError("Either no daemons available or sleep crashed",err);
					}
				}

			} else {
				Debug.printError("Cyclic graph");
			}
			//scanner.close();*/
		}
		else{
			Debug.printError("Failed to read from file, iString is null");
		}
	}
	
	/**
	 * Deploy a list of vertex. 
	 * @param graph	nodes must be ordered. In particular, first mustn't have any incoming edge, last must have 
	 * no outgoing edge
	 */
	public void deploy (List<Vertex<OperatorDeployment>> graph) throws NoDaemonAvailableException
	{
		/*
		if(this.socketManager.getNumberOfCurrentlyConnectedDaemons()==0)
		{
			throw new NoDaemonAvailableException(); 
		}*/
		//We could wait here for all the daemons...
		while(this.socketManager.getNumberOfCurrentlyConnectedDaemons() < graph.size()){
			Debug.printError("Not enough daemons are currently available, waiting for 2 seconds in order to receiver all daemons");
			try {
				Thread.sleep((long) (2000));
			} catch (InterruptedException e) {
				Debug.printError(e);
			}
		}
		//Reverses the topological order to simplify forward star implementation
		Collections.reverse(graph);
		int incrementalId = 0;
		for(Vertex<OperatorDeployment> curr:graph)
		{
			this.socketManager.deployNewOperator(incrementalId, curr.getData());
            incrementalId++;
			//int position=new Random().nextInt(this.socketManager.getNumberOfCurrentlyConnectedDaemons());
		}
	}

}
