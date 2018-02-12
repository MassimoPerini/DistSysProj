package operator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.communication.OperatorSocket;
import operator.types.OperatorType;
import operator.types.Sum;
import utils.Debug;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by massimo on 21/12/2017.
 * NON PIU' VERO
 * ATTENZIONE: se si compila con maven, prima di eseguire lanciare mvn dependency:copy-dependencies
 * Che copia i .jar di dipendenze in una sottocartella
 * In questo modo quando creo il nuovo processo carico anche le dipendenze nel classpath
 * L'alternativa sarebbe inserire il classpath di maven o eseguire il programma con mvn invece che con java (ovviamente se si sposta su un'altra macchina
 * che non ha maven non funziona)
 *
 */
public class MainDaemon {

    private static final int PORT = 1337;

    public static void main(String [] args) throws URISyntaxException, IOException {

        Debug.setLevel(Debug.LEVEL_VERBOSE);

        String jarFile = "";
        if (args.length > 0) {
            jarFile = args[0];
        }

        System.out.println("I'm the daemon, I should contact the supervisor");
            //Process pro = Runtime.getRuntime().exec("java ProcessOperator");

        URI folderUri = ProcessOperator.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String packageClass = ProcessOperator.class.getCanonicalName();

        OperatorSocket operatorSocket = new OperatorSocket(new Socket("127.0.0.1", PORT));
        operatorSocket.start();

    }
}
