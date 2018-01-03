package supervisor;

import utils.Debug;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by massimo on 21/12/17.
 */
public class MainSupervisor {
    private static final int SOCKET_PORT = 3040;
    public static void main(String[] args) {
        Debug.setLevel(Debug.LEVEL_VERBOSE);
        Debug.printVerbose("Main Supervisor Lanciato");
        try {
            startServer();
        }catch(IOException e) {
            Debug.printError(e);
            Debug.printError("Error while starting the supervisor, please restart. Error message: " + e.getMessage());
        }
    }


    private static void startServer() throws IOException
    {
        int numConnection = 1;
        ServerSocket mainSupervisor = new ServerSocket(SOCKET_PORT);
        try {
            while(numConnection > 0 ) {
                Socket socket = mainSupervisor.accept();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Hello daemon, I'm the supervisor");
                Debug.printVerbose("Server started succesfully");
                //Riceve le connessioni con tutti i demoni
                //Riceve l'input dell'utente
                //Crea il DAG

            }
        } catch(IOException e) {
            Debug.printError(e);
            Debug.printError("Error connecting the nodes to the supervisor, please restart. Error message: " + e.getMessage());
        }
        finally{
            mainSupervisor.close();
        }
    }


}
