package operator.communication;

import supervisor.Position;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by massimo on 09/04/18.
 * This class is used by the daemon to have info on the operators
 */
public class DaemonOperatorInfo {
    //it keeps track of the position of the processes the daemon spawned that are currently alive
    private final Map<Position, Process> dataStructure;

    public DaemonOperatorInfo()
    {
        this.dataStructure = new ConcurrentHashMap<>();
    }

    /*
    this method is used to add a process in the map
     */
    public synchronized void addProcess(Position pId, Process process)
    {
        this.dataStructure.put(pId, process);
    }

    /**
     * this method is used to check if a process failed. In such case, i remove it from the list
     * of active process and i
     * @return a list of failed processes
     */
    public synchronized List<Position> getAndRemoveFailedProcesses()
    {
        List<Position> notActive = new LinkedList<>();
        for (Map.Entry<Position, Process> stringProcessEntry : this.dataStructure.entrySet()) {
            if (!stringProcessEntry.getValue().isAlive())
            {
                notActive.add(stringProcessEntry.getKey());

            }
        }

        for (Position s : notActive) {
            this.dataStructure.remove(s);
        }

        return notActive;
    }

}
