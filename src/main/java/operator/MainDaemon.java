package operator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import utils.Debug;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by massimo on 21/12/2017.
 *
 * ATTENZIONE: se si compila con maven, prima di eseguire lanciare mvn dependency:copy-dependencies
 * Che copia i .jar di dipendenze in una sottocartella
 * In questo modo quando creo il nuovo processo carico anche le dipendenze nel classpath
 * L'alternativa sarebbe inserire il classpath di maven o eseguire il programma con mvn invece che con java (ovviamente se si sposta su un'altra macchina
 * che non ha maven non funziona)
 *
 */
public class MainDaemon {

    public static void main(String [] args) throws URISyntaxException, IOException {
        Debug.setLevel(3);
        Debug.printVerbose("I'm the daemon, I should contact the supervisor");
            //Process pro = Runtime.getRuntime().exec("java ProcessOperator");

        URI folderUri = ProcessOperator.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String packageClass = ProcessOperator.class.getCanonicalName();

        //Questo è quello che dovrebbe arrivare dal supervisor
        RuntimeTypeAdapterFactory rtTest = RuntimeTypeAdapterFactory.of(Operator.class, "class_type").registerSubtype(Sum.class);
        Gson gson  = new GsonBuilder().registerTypeAdapterFactory(rtTest).create();

        String outJson = gson.toJson(new Sum(3,3), Operator.class);
        Debug.printVerbose(outJson);

        /*
            socketOut.println(res);
            socketOut.flush();
        */

        Debug.printVerbose("Package class: "+packageClass+"\nfolderStart: "+folderUri.toString());

        //ProcessBuilder pb = new ProcessBuilder("java", packageClass, outJson);

        String unixClassPath = "target/*:target/dependency/*";
        //String windClassPath = "target\\*;target\\dependency\\*";
        String windClassPath = "target/classes;target\\dependency\\*;target\\*";
        String osClassPath = "";

        if (File.separatorChar == '/')
        {
            osClassPath = unixClassPath;
        }
        else
        {
            osClassPath = windClassPath;
        }

        ProcessBuilder pb = new ProcessBuilder("java", "-classpath",osClassPath,packageClass, outJson);

        pb.directory(new File("."));

        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.start();

    }
}
