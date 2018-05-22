package operator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.types.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import supervisor.Position;
import utils.Debug;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by massimo on 22/12/17.
 */
public class ProcessOperator {

    public static void main(String [] args)
    {
        Logger logger = LogManager.getLogger();
        Debug.setUuid(UUID.randomUUID());
        ThreadContext.put("logFileName", "operator"+Debug.getUuid());

        Debug.setLevel(Debug.LEVEL_VERBOSE);
        logger.debug("Processo lanciato!!! " + args[0]);


        Debug.getCrashSendPosition().add(new Position("127.0.0.1", 11340));
        Debug.getCrashSendNMessage().add(3);


        RuntimeTypeAdapterFactory rtTest = RuntimeTypeAdapterFactory.of(OperatorType.class, "type").registerSubtype(Sum.class)
                .registerSubtype(Min.class).registerSubtype(Avg.class).registerSubtype(Max.class);
        Gson gson  = new GsonBuilder().enableComplexMapKeySerialization().registerTypeAdapterFactory(rtTest).create();

        //Type fooTypeMap = new TypeToken<Map<Position, Position>>() {}.getType();
        OperatorType o = gson.fromJson(args[0], OperatorType.class);
        logger.debug("Operazione assegnata: "+o.toString());
        o.deploy();

    }

}
