package operator.communication;

import operator.communication.message.MessageData;

/**
 * Created by massimo on 02/03/18.
 */
public interface OperatorOutputQueue {

    void send(MessageData msg);
    void start();

}
