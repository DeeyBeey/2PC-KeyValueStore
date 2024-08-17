import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * WorkerNode is a remote interface representing a server node which is not a Coordinator, that
 * prepares, commits and rollbacks commands. 
 */
public interface WorkerNode extends Remote {

    /**
     * Prepares the worker server node to execute a command.
     * 
     * @param command The command to be prepared. 
     * @param args Arguments for the command, if any.
     * @return A string reflecting the result of the preparation.
     * @throws RemoteException If a remote communication error is raised.
     */
    String prepareCommand(String command, String[] args) throws RemoteException;

    /**
     * Commit the command that was prepared previously.
     * 
     * @param command The command to be committed.
     * @param args Arguments for the command, if any.
     * @return A string reflecting the result of the commit operation.
     * @throws RemoteException If a remote communication error is raised.
     */
    String commitCommand(String command, String[] args) throws RemoteException;

    /**
     * Rolls back the command that was prepared previously.
     * 
     * @param command The command to be rolled back.
     * @param args Arguments for the command, if any.
     * @return A string reflecting the result of the rollback operation.
     * @throws RemoteException If a remote communication error is raised.
     */
    String rollbackCommand(String command, String[] args) throws RemoteException;
}