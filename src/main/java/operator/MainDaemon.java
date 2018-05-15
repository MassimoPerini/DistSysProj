package operator;

import operator.communication.DaemonOperatorInfo;
import operator.communication.DaemonSocket;
import org.apache.logging.log4j.ThreadContext;
import supervisor.InputMakerJSON;
import utils.Debug;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
    //one for each node so it's fixed
    private static final int PORT = 1337;

    public static void main(String [] args) throws URISyntaxException, IOException {

        Logger logger = LogManager.getLogger();
        ThreadContext.put("logFileName", "daemon");

        Debug.setLevel(Debug.LEVEL_VERBOSE);
        DaemonOperatorInfo daemonOperatorInfo = new DaemonOperatorInfo();

        String jarFile = "";
        if (args.length > 0) {
            jarFile = args[0];
        }

        Debug.printVerbose("I'm the daemon, I should contact the supervisor");
            //Process pro = Runtime.getRuntime().exec("java ProcessOperator");

        URI folderUri = ProcessOperator.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String packageClass = ProcessOperator.class.getCanonicalName();

        DaemonSocket daemonSocket = new DaemonSocket(new Socket("127.0.0.1", PORT));
        daemonSocket.start(daemonOperatorInfo);

    }
}
