import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The CoordinatorHandler interface defines the remote methods that may be invoked by any client
 * to interact with the Coordinator.
 */
public interface CoordinatorHandler extends Remote {

    /**
     * Handles any commands sent by the clients that are to be executed by the coordinator.
     * 
     * @param command The command to be executed.
     * @param args Arguments for the command, if any.
     * @return Response from the Coordinator after command has been executed.
     * @throws RemoteException If a remote communication error is raised.
     */
    String handleCommand(String command, String[] args) throws RemoteException;
}
