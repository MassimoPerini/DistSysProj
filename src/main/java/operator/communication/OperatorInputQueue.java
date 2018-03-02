package operator.communication;

import operator.types.OperatorType;

/**
 * Created by massimo on 02/03/18.
 */
public interface OperatorInputQueue {

    void startReceiving(OperatorType operatorType);

}
