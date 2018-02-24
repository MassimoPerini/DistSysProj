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
import java.lang.reflect.Type;

/**
 * Created by higla on 21/02/2018.
 */
public class InputMakerJSON {
    public static void main(String[] args) {
        Debug.setLevel(3);
        Debug.printVerbose("Main inputMaker started");
        Gson writeGson;
        OperatorDeployment firstOperator = new OperatorDeployment(new Sum(2,2), "");
        OperatorDeployment secondOperator = new OperatorDeployment(new Sum(3,3), "");
        OperatorDeployment thirdOperator = new OperatorDeployment(new Sum(4,4), "");

        Graph<OperatorDeployment> g =new Graph<>();
        Vertex<OperatorDeployment> v1=new Vertex<>(1,firstOperator);
        Vertex<OperatorDeployment> v2=new Vertex<>(2,secondOperator);
        Vertex<OperatorDeployment> v3=new Vertex<>(3,thirdOperator);

        g.asymmConnect(v1, v2, 0);
        g.asymmConnect(v2, v3, 0);

        g.addVertex(1, v1);
        g.addVertex(2, v2);
        g.addVertex(3, v3);


        RuntimeTypeAdapterFactory typeAdapterFactory = RuntimeTypeAdapterFactory.of(MessageSupervisor.class, "type")
                .registerSubtype(OperatorDeployment.class);


        writeGson = new GsonBuilder().registerTypeAdapterFactory(typeAdapterFactory)
                //.setPrettyPrinting()
                .create();  //setPrettyPrinting

        Type fooType = new TypeToken<Graph<OperatorDeployment>>() {}.getType();
        String output = writeGson.toJson(g, fooType);
        System.out.println(output);

        Graph<OperatorDeployment> graph = new Gson().fromJson(output, fooType);

        System.out.println(graph.toString());

        RecoveryManager rm = new RecoveryManager();
        DataKey d = new DataKey(1, "A");
        DataKey d2 = new DataKey(1, "B");
        rm.putDataInFile("example.json" , d);
        System.out.println(rm.readDataFromFile("example.json"));
        rm.putDataInFile("example.json", d2);
        System.out.println(rm.readDataFromFile("example.json"));

    }
}
