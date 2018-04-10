package supervisor.communication.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import operator.communication.DaemonOperatorInfo;
import operator.communication.message.MessageOperator;
import operator.communication.message.ReplyHeartBeat;
import supervisor.Position;
import utils.Debug;

import java.util.List;

/**
 * Created by massimo on 10/02/18.
 */
public class HeartbeatRequest implements MessageSupervisor {

    public HeartbeatRequest()
    {
    }

    @Override
    public MessageOperator execute(DaemonOperatorInfo daemonOperatorInfo) {
        List<Position> failedProcesses = daemonOperatorInfo.getAndRemoveFailedProcesses();
        Debug.printVerbose("FAILED PROCESSES: "+failedProcesses);

        ReplyHeartBeat replyHeartBeat = new ReplyHeartBeat(failedProcesses);

        return replyHeartBeat;
    }
}
