package supervisor.communication.message;

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
    void execute();

}
