package operator.communication.message;

import utils.Debug;

/**
 * Created by massimo on 10/04/18.
 */
public class LogMessageOperator implements MessageOperator {

    private final String message;

    public LogMessageOperator(String msg)
    {
        this.message = msg;
    }

    @Override
    public void execute() {
        Debug.printVerbose(this.message);
    }
}
