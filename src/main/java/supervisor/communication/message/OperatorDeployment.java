package supervisor.communication.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.ProcessOperator;
import operator.communication.OperatorSocket;
import operator.types.OperatorType;
import operator.types.Sum;
import org.jetbrains.annotations.NotNull;
import utils.Debug;

import java.io.File;
import java.net.Socket;
import java.net.URI;

/**
 * Created by massimo on 11/02/18.
 *
 *  This class represents the message that the supervisor sents to the Daemon when a new operator needs to be deployed on that node
 */
public class OperatorDeployment implements MessageSupervisor {

    //private final OperatorType operatorType;
    private final String jarFile;
    private final String outJson;

    /***
     *
     * @param operatorType The operator that needs to be deployed
     * @param jarFile TEMPORARY PARAMETER, with "" it will run from IDE the new process, with the path of the jar will run the new class from the jar file
     */

    public OperatorDeployment(@NotNull OperatorType operatorType, String jarFile)
    {
        //this.operatorType = operatorType;
        this.jarFile = jarFile;
        RuntimeTypeAdapterFactory rtTest = RuntimeTypeAdapterFactory.of(OperatorType.class, "type")
                .registerSubtype(Sum.class);
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(rtTest).create();
        outJson = gson.toJson(operatorType, OperatorType.class);

    }

    /***
     * Invoked by the Daemon, will deploy a new operator (starting a new process)
     */
    @Override
    public void execute() {
        try {
            URI folderUri = ProcessOperator.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            String packageClass = ProcessOperator.class.getCanonicalName();
            Debug.printVerbose(outJson);

        /*
            socketOut.println(res);
            socketOut.flush();
         */

            Debug.printVerbose("Package class: " + packageClass + "\nfolderStart: " + folderUri.toString());

            String unixClassPath = "target/*:target/dependency/*";
            String windClassPath = "target\\*;target\\dependency\\*";
            String osClassPath = "";

            if (File.separatorChar == '/') {
                osClassPath = unixClassPath;
            } else {
                osClassPath = windClassPath;
            }

            ProcessBuilder pb;
            if (jarFile.equals("")) {
                Debug.printVerbose("-Dexec.mainClass=\"it.polimi.distsys." + packageClass + "\"");
                pb = new ProcessBuilder("mvn", "exec:java", "-Dexec.mainClass=" + packageClass, "-Dexec.args=\"" + outJson + "\"");
            } else {
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
        catch (Exception e)
        {
            Debug.printError(e);
        }

    }
}
