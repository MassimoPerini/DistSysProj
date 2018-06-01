package supervisor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import operator.types.OperatorType;
import operator.types.Sum;
import supervisor.communication.message.MessageSupervisor;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Graph;
import supervisor.graph_representation.Vertex;
import utils.Debug;

public class Makerv2 {
	public static void main(String[] args) {
        Debug.setLevel(3);
        Debug.printVerbose("Main inputMaker started");

        Logger logger = LogManager.getLogger();
        ThreadContext.put("logFileName", "inputMaker");
        int k = 10000;
        Gson writeGson;
        Position firstSocket;
        firstSocket = new Position("127.0.0.1", 1340+k);
        //}

        Position thirdSocket =  new Position ("127.0.0.1", 1349+k);;



        //la prima lista è per definire le outputQueue, la seconda è per i signleParallelSocket
        List<List<Position>> out = new LinkedList<>();
        List<Position> out2 = new LinkedList<>();
      
        out2.add(firstSocket);

        out.add(out2);


        List<List<Position>> outFinal = new LinkedList<>();
        List<Position> outFinalPos = new LinkedList<>();
        outFinalPos.add(thirdSocket);
        outFinal.add(outFinalPos);
        //source dove apro il sever
        //out lista di gente a cui devo inviare
        //exactPosition
        //position deve avere IP uguale a first socket.. => locale vs 10.... non funziona

        List<Position> fakeList = new LinkedList<>();

        List<Position> portToConnectWith = new LinkedList<>();
        String localAddress;
        try {
            localAddress = "127.0.0.1";
        }catch(Exception e)
        {
            localAddress = "192.168.0.1";
        }
        portToConnectWith.add(new Position(localAddress, 5555+k));
        List<Position> portNodeTwo = new LinkedList<>();

        portNodeTwo.add(new Position(localAddress, 6555+k));
    
        OperatorType operatorOne = new Sum(2,2, new Position(localAddress, -1), out, portToConnectWith);
        OperatorType operatorTwo = new Sum(3,3, firstSocket, outFinal, portNodeTwo);
        OperatorType operatorFour = new Sum(2, 2, thirdSocket, new LinkedList<>(), fakeList);

        //deployment è la position dell'heartbeat
        OperatorDeployment firstOperator = new OperatorDeployment(
                operatorOne, "");
        OperatorDeployment secondOperator = new OperatorDeployment(
                operatorTwo, "");

        OperatorDeployment fourthOperator = new OperatorDeployment(
                operatorFour, "");


        Graph<OperatorDeployment> g =new Graph<>();
        Vertex<OperatorDeployment> v1=new Vertex<>(1,firstOperator);
        Vertex<OperatorDeployment> v2=new Vertex<>(2,secondOperator);
        Vertex<OperatorDeployment> v4=new Vertex<>(4, fourthOperator);

        g.asymmConnect(v1, v2, 0);
        g.asymmConnect(v2, v4, 0);
    
        g.addVertex(1, v1);
        g.addVertex(2, v2);
        g.addVertex(4, v4);


        RuntimeTypeAdapterFactory typeAdapterFactory = RuntimeTypeAdapterFactory.of(MessageSupervisor.class, "type")
                .registerSubtype(OperatorDeployment.class);
        /*
        RuntimeTypeAdapterFactory typeAdapterFactory2 = RuntimeTypeAdapterFactory.of(Position.class, "type")
                .registerSubtype(Position.class);
        */

        writeGson = new GsonBuilder().enableComplexMapKeySerialization().registerTypeAdapterFactory(typeAdapterFactory)
                //.registerTypeAdapterFactory(typeAdapterFactory2)
                .setPrettyPrinting()
                .create();  //setPrettyPrinting

        //Type fooTypeMap = new TypeToken<Map<Position, Position>>() {}.getType();
        Type fooType = new TypeToken<Graph<OperatorDeployment>>() {}.getType();

        String output = writeGson.toJson(g, fooType);
        //output = writeGson.toJson(output, fooTypeMap);
        //Debug.printVerbose(output);
        try {
            String fileName = "graphDeployInput.json";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(output);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Graph<OperatorDeployment> graph = new Gson().fromJson(output, fooType);

        Debug.printVerbose(graph.toString());

    }

    private static Map<Position,Position> inizializzaMappa(List<List<Position>> out, Position pos1, int porta){
        Map<Position, Position> mappaOperatore = new HashMap<>();
        for (List<Position> positions : out) {
            for (Position position : positions) {
                mappaOperatore.put(position, new Position(pos1.getAddress(), porta));
                porta++;
            }
        }
        Debug.printVerbose(mappaOperatore.toString());
        return mappaOperatore;
    }
}
