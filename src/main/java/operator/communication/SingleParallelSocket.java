package operator.communication;

import operator.recovery.DataKey;
import operator.recovery.Key;
import operator.types.OperatorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
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
            Logger logger = LogManager.getLogger();
            logger.error(e);
        }
        this.dataFeeder = operatorType;
    }

    public void send(DataKey messageData) {
        try {
            this.socketOut.writeObject(messageData);
            this.socketOut.flush();
        } catch (IOException e) {
            Logger logger = LogManager.getLogger();
            logger.error(e);
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
                    logger.error(e1);
                    logger.error("Waiting for the node to come back alive");
                    //todo check if it's okays
                    try {
                        Thread.sleep((long) (200));
                    } catch (InterruptedException e2) {
                        logger.error(e2);
                    }
                }
            }
        }
    }

    public void keepAcknowledging()
    {
        Logger logger = LogManager.getLogger();
        ThreadContext.put("logFileName", "operator"+Debug.getUuid());
        while(true)
        {
            try {

                DataKey receivedAck=(DataKey)this.socketIn.readObject();
                logger.trace("Received an ack: "+receivedAck);
                 dataFeeder.manageAck(receivedAck.getOriginalKey(),receivedAck.getAggregator(),this);// todo fix here
            } catch (IOException | ClassNotFoundException e)
            {

                logger.error(e.getStackTrace());
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
            Logger logger = LogManager.getLogger();
            logger.error(e);
        }
    }
    
    public Position getOtherSideAddress()
    {
    	return new Position(this.addressToReconnectWith,this.portToReconnectWith);
    }
    

}
