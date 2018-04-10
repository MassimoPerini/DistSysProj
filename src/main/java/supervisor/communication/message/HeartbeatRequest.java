package supervisor.communication.message;

import operator.communication.DaemonOperatorInfo;
import operator.communication.message.ReplyHeartBeat;
import supervisor.Position;

import java.util.List;

/**
 * Created by massimo on 10/02/18.
 */
public class HeartbeatRequest implements MessageSupervisor {

    public HeartbeatRequest()
    {
    }

    @Override
    public void execute(DaemonOperatorInfo daemonOperatorInfo) {
        List<Position> failedProcesses = daemonOperatorInfo.getAndRemoveFailedProcesses();
        System.out.println("FAILED PROCESSES: "+failedProcesses);
        ReplyHeartBeat replyHeartBeat = new ReplyHeartBeat();
    }
}
