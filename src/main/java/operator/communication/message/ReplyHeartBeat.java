package operator.communication.message;

import supervisor.Position;
import utils.Debug;

import java.io.Serializable;
import java.util.List;

/**
 * Created by massimo on 10/02/18.
 */
public class ReplyHeartBeat implements MessageOperator, Serializable {

    private final List<Position> failedPositions;

    public ReplyHeartBeat(List<Position> failedPositions) {
        this.failedPositions = failedPositions;
    }

    @Override
    public void execute() {
        if (this.failedPositions.size() > 0){
            Debug.printVerbose("E' fallito almeno un processo");
        }
    }
}
