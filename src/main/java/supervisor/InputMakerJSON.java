package supervisor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.recovery.DataKey;
import operator.recovery.RecoveryManager;
import operator.types.OperatorType;
import operator.types.Sum;
import supervisor.communication.message.MessageSupervisor;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Graph;
import supervisor.graph_representation.Vertex;
import utils.Debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by higla on 21/02/2018.
 * This class is a temporary class used to generate the gson input
 */
public class InputMakerJSON {
    public static void main(String[] args) {
        Debug.setLevel(3);
        Debug.printVerbose("Main inputMaker started");
        Gson writeGson;
        Position firstSocket;
        try {
            firstSocket = new Position(InetAddress.getLocalHost().
                    getCanonicalHostName(), 1340);
        }
        catch (UnknownHostException e){
            firstSocket = new Position("127.0.0.1", 1340);
        }

        Position secondSocket = new Position ("127.0.0.1", 1341);
        Position thirdSocket = null;
        List<Position> emptyListPosition = new ArrayList<>();
        List<Integer> emptyListInteger = new ArrayList<>();

        List<Position> listPositionFirst = new ArrayList<>();
        List<Integer> listIntegerFirst = new ArrayList<>();
        listPositionFirst.add(secondSocket);
        listIntegerFirst.add(51234);
        Position pos1;
        Position pos2;
        try {
            pos1 = new Position(InetAddress.getLocalHost().
                    getCanonicalHostName(), 12345);
            pos2 = new Position(InetAddress.getLocalHost().
                    getCanonicalHostName(), 12346);
        }
        catch (UnknownHostException e){
            pos1 = new Position("127.0.0.1", 12345);
            pos2 = new Position("127.0.0.1", 12346);
        }



        List<List<Position>> out = new LinkedList<>();
        List<Position> out2 = new LinkedList<>();
        out2.add(firstSocket);
        out.add(out2);
        //source dove apro il sever
        //out lista di gente a cui devo inviare
        //exactPosition
        //position deve avere IP uguale a first socket.. => locale vs 10.... non funziona
        OperatorType operatorOne = new Sum(2,2, null, out, pos1);
        OperatorType operatorTwo = new Sum(3,3, firstSocket, new LinkedList<>(), pos2);
        /* todo: a cosa servivano esattamente?
        operatorOne.setPortToUseToConnectToPosition(listPositionFirst, listIntegerFirst);
        operatorTwo.setPortToUseToConnectToPosition(emptyListPosition, emptyListInteger);
        */
        OperatorDeployment firstOperator = new OperatorDeployment(
                operatorOne, "", firstSocket);
        OperatorDeployment secondOperator = new OperatorDeployment(
                operatorTwo, "", secondSocket);

        //OperatorDeployment thirdOperator = new OperatorDeployment(new Sum(4,4, null, new LinkedList<>()), "");
        //firstOperator.getOperatorType().getPortToUseToConnectToPosition().put(firstSocket, 51234);

        Graph<OperatorDeployment> g =new Graph<>();
        Vertex<OperatorDeployment> v1=new Vertex<>(1,firstOperator);
        Vertex<OperatorDeployment> v2=new Vertex<>(2,secondOperator);
        //Vertex<OperatorDeployment> v3=new Vertex<>(3,thirdOperator);

        g.asymmConnect(v1, v2, 0);
        //g.asymmConnect(v2, v3, 0);

        g.addVertex(1, v1);
        g.addVertex(2, v2);
        //g.addVertex(3, v3);


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

        Type fooType = new TypeToken<Graph<OperatorDeployment>>() {}.getType();
        String output = writeGson.toJson(g, fooType);
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
        /*
        RecoveryManager rm = new RecoveryManager("example.json");
        DataKey d = new DataKey(1, "A");
        DataKey d2 = new DataKey(1, "B");
        rm.putDataInFile("example.json" , d);
        System.out.println(rm.readDataFromFile("example.json"));
        rm.putDataInFile("example.json", d2);
        System.out.println(rm.readDataFromFile("example.json"));
        System.out.println(rm.readAndRemoveDataFromFile("example.json"));
        rm.appendDataInFileList(d);
        rm.appendDataInFileList(d2);
        rm.appendDataInFileList(d2);*/
        //Debug.printVerbose(data.toString());
    }
}
