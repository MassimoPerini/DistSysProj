package operator.communication;

import operator.recovery.DataKey;
import operator.recovery.Key;
import operator.types.OperatorType;
import supervisor.Position;
import utils.Debug;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by massimo on 05/05/18.
 */
public class SingleParallelSocket {
    private Socket socket;

    private ObjectInputStream socketIn;
    private ObjectOutputStream socketOut;

    private final String addressToReconnectWith;
    private final int portToReconnectWith;
    private final OperatorType dataFeeder;


    public SingleParallelSocket(Socket socket, OperatorType operatorType) {
    	this.socket = socket;
        this.addressToReconnectWith = socket.getInetAddress().toString();
        this.portToReconnectWith = socket.getPort();
        
        try {
            this.socketOut = (new ObjectOutputStream(this.socket.getOutputStream()));
            this.socketIn = (new ObjectInputStream(this.socket.getInputStream()));

        } catch (IOException e) {
            Debug.printDebug(e);    
        }
        this.dataFeeder = operatorType;
    }

    public void send(DataKey messageData) {
        try {
            this.socketOut.writeObject(messageData);
            this.socketOut.flush();
        } catch (IOException e) {
            Debug.printDebug(e);
            dataFeeder.stopOutput(this); //todo fix here
            Boolean tryToReconnect = true;
            while(tryToReconnect){
                try {
                    this.socket = new Socket(this.addressToReconnectWith, this.portToReconnectWith);
                    this.socketOut = (new ObjectOutputStream(this.socket.getOutputStream()));
                    this.socketIn = (new ObjectInputStream(this.socket.getInputStream()));
                    tryToReconnect = false;
                    this.dataFeeder.restartOutput(this); //todo fix here
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Debug.printVerbose("Waiting for the node to come back alive");
                    //todo check if it's okays
                    try {
                        Thread.sleep((long) (200));
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                        Debug.printVerbose("Thread sleep failed");
                    }
                }
            }
        }
    }

    public void keepAcknowledging()
    {
        while(true)
        {
            try {

                Key receivedAck=(Key)this.socketIn.readObject();
                Debug.printVerbose("Received an ack: "+receivedAck);
                 dataFeeder.manageAck(receivedAck,this);// todo fix here
            } catch (IOException e)
            {
                Debug.printError(e);
            } catch (ClassNotFoundException e)
            {
                Debug.printError(e);
            }
        }
    }

    public void finish()
    {
        try {
            socketOut.close();
            socketIn.close();
            socket.close();
        }
        catch (IOException e)
        {
            Debug.printError("IOException on closing...");
        }
    }
    
    public Position getOtherSideAddress()
    {
    	return new Position(this.addressToReconnectWith,this.portToReconnectWith);
    }
    

}
