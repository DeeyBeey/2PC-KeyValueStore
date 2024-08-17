import java.rmi.RemoteException;
import java.util.concurrent.Callable;

/**
 * WorkerHandler is a Callable implementation that handles the interactions that take place with
 * the WorkerNode for various phases of the two phase commit protocol.
 */
public class WorkerHandler implements Callable<String> {
    private final WorkerNode workerNode;
    private final String command;
    private final String[] args;
    private final String phase;

    /**
     * Constructs a WorkerHandler interface.
     * 
     * @param workerNode The worker node with which interaction will be done.
     * @param command The command to be executed.
     * @param args Arguments for the command, if any.
     * @param phase Command execution phases like "PREPARE", "COMMIT" and "ROLLBACK"
     */
    public WorkerHandler(WorkerNode workerNode, String command, String[] args, String phase) {
        this.workerNode = workerNode;
        this.command = command;
        this.args = args;
        this.phase = phase;
    }

    /**
     * Executes a command on the worker node based on the respective phase.
     * 
     * @return The result of execution of command on the worker node.
     * @throws RemoteException If a remote communication error is raised.
     */
    @Override
    public String call() throws RemoteException {
        switch (phase) {
            case "PREPARE":
                return workerNode.prepareCommand(command, args);
            case "COMMIT":
                return workerNode.commitCommand(command, args);
            case "ROLLBACK":
                return workerNode.rollbackCommand(command, args);
            default:
                throw new IllegalArgumentException("Unknown Phase: " + phase);
        }
    }
}