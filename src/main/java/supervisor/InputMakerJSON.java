package supervisor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import javafx.geometry.Pos;
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
 *
 *
 * Per creare un nuovo grafo bisogna:
 * 1) Identificare una topologia di rete (nodi, operatori sui nodi, livello di parallelismo per ogni nodo)
 * 2) Creare le rispettive Position composte da Address e Port.
 * Ogni OperatorType ha:
 * SOURCE => Position in cui apro il server. Se è IP:null, è il primo nodo e prendo in input da file
 * ListaListe => Per permettere il parallelismo, in output ho una lista di liste.
 * La prima lista mi identifica l'OutputToSocket, la seconda la SingleParallelSocket.
 * mappaDiPosition => per ogni position ho mappata una porta --> bind.
 * 3) Creare operator deployment utilizzando Source => sarà l'ip a cui il supervisore farà l'heartbeat
 */
public class InputMakerJSON {
    public static void main(String[] args) {
        Debug.setLevel(3);
        Debug.printVerbose("Main inputMaker started");
        Gson writeGson;
        Position firstSocket;
        Position parallelSocket;/*
        try {
            firstSocket = new Position(InetAddress.getLocalHost().
                    getCanonicalHostName(), 1340);
            parallelSocket = new Position(InetAddress.getLocalHost().
                    getCanonicalHostName(), 1345);
        }
        catch (UnknownHostException e){*/
            firstSocket = new Position("127.0.0.1", 1340);
            parallelSocket = new Position("127.0.0.1", 1345);
        //}

        Position secondSocket = new Position ("127.0.0.1", 1341);
        Position thirdSocket =  new Position ("127.0.0.1", 1349);;
        List<Position> emptyListPosition = new ArrayList<>();
        List<Integer> emptyListInteger = new ArrayList<>();

        List<Position> listPositionFirst = new ArrayList<>();
        List<Integer> listIntegerFirst = new ArrayList<>();
        listPositionFirst.add(secondSocket);
        listPositionFirst.add(thirdSocket);
        listIntegerFirst.add(51234);
        //used to connect the node to the supervisor
        Position pos1;
        Position pos2;
        Position pos3;/*
        try {
            pos1 = new Position(InetAddress.getLocalHost().
                    getCanonicalHostName(), 12345);
            pos2 = new Position(InetAddress.getLocalHost().
                    getCanonicalHostName(), 12346);
            pos3 = new Position(InetAddress.getLocalHost().
                    getCanonicalHostName(), 12347);
        }
        catch (UnknownHostException e){*/
            pos1 = new Position("127.0.0.1", 12345);
            pos2 = new Position("127.0.0.1", 12346);
            pos3 = new Position("127.0.0.1", 12347);
        //}



        List<List<Position>> out = new LinkedList<>();
        List<Position> out2 = new LinkedList<>();
        out2.add(firstSocket);
        out2.add(parallelSocket);
        out.add(out2);
        //source dove apro il sever
        //out lista di gente a cui devo inviare
        //exactPosition
        //position deve avere IP uguale a first socket.. => locale vs 10.... non funziona

        List<Position> fakeList = new LinkedList<>();
        /*
        mappaPrimoOperatore = inizializzaMappa(out, pos1, 5555);
        mappaSecondoOperatore = inizializzaMappa(fakeList, pos2, 5565);
        mappaTerzoOperatore = inizializzaMappa(fakeList, pos3, 5575);
        */
        List<Position> portToConnectWith = new LinkedList();
        String localAddress = "127.0.0.1";
        portToConnectWith.add(new Position(localAddress, 5555));
        portToConnectWith.add(new Position(localAddress, 5556));
        OperatorType operatorOne = new Sum(2,2, new Position(localAddress, -1), out, portToConnectWith);
        OperatorType operatorTwo = new Sum(3,3, firstSocket, new LinkedList<>(), fakeList);
        OperatorType operatorParallel = new Sum(3,3, parallelSocket, new LinkedList<>(), fakeList);


        //deployment è la position dell'heartbeat
        OperatorDeployment firstOperator = new OperatorDeployment(
                operatorOne, "");
        OperatorDeployment secondOperator = new OperatorDeployment(
                operatorTwo, "");
        OperatorDeployment parallelOperator = new OperatorDeployment(
                operatorParallel, "");

        Graph<OperatorDeployment> g =new Graph<>();
        Vertex<OperatorDeployment> v1=new Vertex<>(1,firstOperator);
        Vertex<OperatorDeployment> v2=new Vertex<>(2,secondOperator);
        Vertex<OperatorDeployment> v3=new Vertex<>(3,parallelOperator);

        g.asymmConnect(v1, v2, 0);
        g.asymmConnect(v2, v3, 0);

        g.addVertex(1, v1);
        g.addVertex(2, v2);
        g.addVertex(3, v3);


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

        Type fooTypeMap = new TypeToken<Map<Position, Position>>() {}.getType();
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
