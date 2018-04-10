package supervisor.communication.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.ProcessOperator;
import operator.communication.DaemonOperatorInfo;
import operator.communication.message.LogMessageOperator;
import operator.communication.message.MessageOperator;
import operator.types.OperatorType;
import operator.types.Sum;
import org.jetbrains.annotations.NotNull;
import supervisor.Position;
import utils.Debug;

import java.io.File;
import java.net.URI;

/**
 * Created by massimo on 11/02/18.
 *
 *  This class represents the message that the supervisor sents to the Daemon when a new operator needs to be deployed on that node
 */
public class OperatorDeployment implements MessageSupervisor {

    //private final OperatorType operatorType;
    private final String jarFile;
    private String outJson;
    private final Position ownPosition;

    public Position getOwnPosition() {
        return ownPosition;
    }

    /***
     *
     * @param operatorType The operator that needs to be deployed
     * @param jarFile TEMPORARY PARAMETER, with "" it will run from IDE the new process, with the path of the jar will run the new class from the jar file
     */



    public OperatorDeployment(@NotNull OperatorType operatorType, String jarFile, Position position)
    {
        //this.operatorType = operatorType;
        this.jarFile = jarFile;
        RuntimeTypeAdapterFactory rtTest = RuntimeTypeAdapterFactory.of(OperatorType.class, "type")
                .registerSubtype(Sum.class);
        this.ownPosition = position;
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(rtTest).create();
        outJson = gson.toJson(operatorType, OperatorType.class);


    }
    /***
     * Invoked by the Daemon, will deploy a new operator (starting a new process)
     */
    @Override
    public MessageOperator execute(DaemonOperatorInfo daemonOperatorInfo) {
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
                osClassPath = "mvn";
            } else {
                osClassPath = "mvn.cmd";
            }

            ProcessBuilder pb;
            if (jarFile.equals("")) {
                Debug.printVerbose("-Dexec.mainClass=\"it.polimi.distsys." + packageClass + "\"");

                pb = new ProcessBuilder(osClassPath, "exec:java", "-Dexec.mainClass=" + packageClass, "-Dexec.args=\"" + outJson + "\"");
                //pb.environment().put("PATH", System.getProperty("PATH"));
            } else {
                pb = new ProcessBuilder("java", "-cp", jarFile, packageClass, outJson);
            }

            pb.directory(new File("."));

            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = pb.start();
            daemonOperatorInfo.addProcess(this.ownPosition, process);   //Adding the process to the process of the daemon


            if (Math.random()>0.5) {
                process.destroy();
                Debug.printError("Il processo è stato killato volontariamente");
            }

            //return new LogMessageOperator("Success");
            return null;

            /*
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Debug.printError(e);
            //return new LogMessageOperator("Error");
            return null;

        }
    }
}
