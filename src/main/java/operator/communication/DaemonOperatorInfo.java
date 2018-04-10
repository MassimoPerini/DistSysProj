package operator.communication;

import supervisor.Position;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by massimo on 09/04/18.
 */
public class DaemonOperatorInfo {

    private final Map<Position, Process> dataStructure;

    public DaemonOperatorInfo()
    {
        this.dataStructure = new ConcurrentHashMap<>();
    }

    public synchronized void addProcess(Position pId, Process process)
    {
        this.dataStructure.put(pId, process);
    }

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
