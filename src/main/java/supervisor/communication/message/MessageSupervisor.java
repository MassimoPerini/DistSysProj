package supervisor.communication.message;

import operator.communication.DaemonOperatorInfo;

/**
 * Created by massimo on 10/02/18.
 *
 * The generic message sent by the supervisor to the node
 *
 */
public interface MessageSupervisor {

    /***
     * Invoked by the node
     */
    void execute(DaemonOperatorInfo daemonOperatorInfo);

}
