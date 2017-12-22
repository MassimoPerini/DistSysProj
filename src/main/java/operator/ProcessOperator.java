package operator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

/**
 * Created by massimo on 22/12/17.
 */
public class ProcessOperator {

    public static void main(String [] args)
    {
        System.out.println("Processo lanciato!!! "+ args[0]);

        RuntimeTypeAdapterFactory rtTest = RuntimeTypeAdapterFactory.of(Operator.class, "class_type").registerSubtype(Sum.class);
        Gson gson  = new GsonBuilder().registerTypeAdapterFactory(rtTest).create();
        Operator o = gson.fromJson(args[0], Operator.class);
        System.out.println("Operazione assegnata: "+o.toString());

    }

}
