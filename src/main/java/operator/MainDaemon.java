package operator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.communication.OperatorSocket;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public static void main(String [] args) throws URISyntaxException, IOException {

        String jarFile = "";
        if (args.length > 0) {
            jarFile = args[0];
        }

        System.out.println("I'm the daemon, I should contact the supervisor");
            //Process pro = Runtime.getRuntime().exec("java ProcessOperator");

        URI folderUri = ProcessOperator.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String packageClass = ProcessOperator.class.getCanonicalName();


        OperatorSocket operatorSocket = new OperatorSocket(new Socket("127.0.0.1", 1337));


        //Questo è quello che dovrebbe arrivare dal supervisor
        RuntimeTypeAdapterFactory rtTest = RuntimeTypeAdapterFactory.of(Operator.class, "class_type").registerSubtype(Sum.class);
        Gson gson  = new GsonBuilder().registerTypeAdapterFactory(rtTest).create();

        String outJson = gson.toJson(new Sum(3,3), Operator.class);
        System.out.println(outJson);

        /*
            socketOut.println(res);
            socketOut.flush();
         */

        System.out.println("Package class: "+packageClass+"\nfolderStart: "+folderUri.toString());

        //ProcessBuilder pb = new ProcessBuilder("java", packageClass, outJson);

        String unixClassPath = "target/*:target/dependency/*";
        String windClassPath = "target\\*;target\\dependency\\*";
        String osClassPath = "";

        if (File.separatorChar == '/')
        {
            osClassPath = unixClassPath;
        }
        else
        {
            osClassPath=windClassPath;
        }

        ProcessBuilder pb;
        if (jarFile.equals(""))
        {
            System.out.println("-Dexec.mainClass=\"it.polimi.distsys."+packageClass+"\"");
            pb = new ProcessBuilder("mvn", "exec:java", "-Dexec.mainClass="+packageClass, "-Dexec.args=\""+outJson+"\"");
        }
        else{
            pb = new ProcessBuilder("java", "-cp", jarFile, packageClass, outJson);
        }


        pb.directory(new File("."));

        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
