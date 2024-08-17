import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the CoordinatorHandler interface. This class is responsible for distribution
 * and coordination of commands across various worker nodes.
 */
public class CoordinatorHandlerImpl extends UnicastRemoteObject implements CoordinatorHandler {
    private static final Logger logger = Logger.getLogger(CoordinatorHandlerImpl.class.getName());
    private final List<WorkerNode> workerNodes;
    private final ExecutorService executor;

    /**
     * Constructs an instance of CoordinatorHandlerImpl.
     * 
     * @param workerNodes A list of worker nodes that will coordinate.
     * @param executor Executor service to manage threads of the worker nodes.
     * @throws RemoteException If a remote communication error is raised.
     */
    protected CoordinatorHandlerImpl(List<WorkerNode> workerNodes, ExecutorService executor) 
    throws RemoteException {
        super();
        this.workerNodes = workerNodes;
        this.executor = executor;
    }
    
    /**
     * Handles commands by preparing, committing or rolling back operations performed on worker nodes.
     * 
     * @param command The command to be executed.
     * @param args Arguments for the command, if any.
     * @return Response from the Coordinator after command has been executed.
     * @throws RemoteException If a remote communication error is raised.
     */
    @Override
    public String handleCommand(String command, String[] args) throws RemoteException {
        boolean prepared = prepare(command, args);
        if (prepared) {
            return commit(command, args);
        } else {
            rollback(command, args);
            return "Operation failed during preparation.";
        }
    }

    /**
     * Preparation phase for the worker nodes to execute commands.
     * 
     * @param command The command to be executed.
     * @param args Arguments for the command, if any.
     * @return true if all worker nodes are prepared, else false.
     */
    private boolean prepare(String command, String[] args) {
        List<Future<String>> futures = new ArrayList<>();
        for (WorkerNode workerNode : workerNodes) {
            WorkerHandler workerHandler = new WorkerHandler
            (workerNode, command, args, "PREPARE");
            futures.add(executor.submit(workerHandler));
        }

        for (Future<String> future : futures) {
            try {
                String response = future.get();
                if (!response.equals("PREPARED")) {
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Commits the execution of commands on the worker nodes.
     * 
     * @param command The command to be executed.
     * @param args Arguments for the command, if any.
     * @return The result after the command has been executed.
     */
    private String commit(String command, String[] args) {
        StringBuilder result = new StringBuilder();
        List<Future<String>> futures = new ArrayList<>();
        for (WorkerNode workerNode : workerNodes) {
            WorkerHandler workerHandler = new WorkerHandler
            (workerNode, command, args, "COMMIT");
            futures.add(executor.submit(workerHandler));
        }

        for (Future<String> future : futures) {
            try {
                String response = future.get();
                if (!result.toString().contains(response)) {
                    result.append(response).append("\n");
                }
            } catch (InterruptedException | ExecutionException e) {
                result.append("Error committing on worker node: ")
                .append(e.getMessage()).append("\n");
            }
        }
        return result.toString().trim();
    }

    /**
     * Rolls back command execution on the worker nodes.
     * 
     * @param command The command to be executed.
     * @param args Arguments for the command, if any.
     */
    private void rollback(String command, String[] args) {
        List<Future<String>> futures = new ArrayList<>();
        for (WorkerNode workerNode : workerNodes) {
            WorkerHandler workerHandler = new WorkerHandler
            (workerNode, command, args, "ROLLBACK");
            futures.add(executor.submit(workerHandler));
        }

        for (Future<String> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.SEVERE, 
                "Error while performing rollback on worker node: ", e);
            }
        }
    }
}
