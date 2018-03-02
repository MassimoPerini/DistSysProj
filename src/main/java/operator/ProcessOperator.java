package operator;

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

    public static void main(String [] args)
    {
        Debug.setLevel(Debug.LEVEL_VERBOSE);
        Debug.printVerbose("Processo lanciato!!! " + args[0]);

        RuntimeTypeAdapterFactory rtTest = RuntimeTypeAdapterFactory.of(OperatorType.class, "type").registerSubtype(Sum.class);
        Gson gson  = new GsonBuilder().registerTypeAdapterFactory(rtTest).create();
        OperatorType o = gson.fromJson(args[0], OperatorType.class);
        Debug.printVerbose("Operazione assegnata: "+o.toString());
        o.deploy();

    }

}
