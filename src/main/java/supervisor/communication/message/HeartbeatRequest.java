package supervisor.communication.message;

/**
 * Created by massimo on 10/02/18.
 */
public class HeartbeatRequest implements MessageSupervisor {

    private final int counter;

    public HeartbeatRequest(int timerCounter)
    {
        this.counter = timerCounter;
    }

    @Override
    public void execute() {

    }
}
