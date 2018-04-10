package operator.communication;

import operator.recovery.DataKey;

/**
 * Created by massimo on 02/03/18.
 */
public interface OperatorOutputQueue {

    void send(DataKey msg);
    void start();

}
