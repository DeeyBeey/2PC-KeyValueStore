import java.util.HashMap;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * WorkerNodeImpl is the implementation of the WorkerNode interface. It represents a server worker
 * node which is not a Coordinator. It is capable of handling the GET, PUT and DELETE operations and
 * supports the "PREPARE", "COMMIT" and "ROLLBACK" phases. 
 */
public class WorkerNodeImpl extends UnicastRemoteObject implements WorkerNode {
    private final HashMap<String, String> keyValueStore;
    private final ExecutorService executor;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern
    ("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Logger logger = Logger.getLogger(WorkerNodeImpl.class.getName());
    private static final Object loggerLock = new Object();

        static {
        try {
            FileHandler fileHandler = new FileHandler("workerNode.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            synchronized (loggerLock) {
                logger.addHandler(fileHandler);
                logger.setUseParentHandlers(false);
            }
        } catch (IOException e) {
            printWithTimestamp("Error setting up logger: " + e.getMessage());
        }
    }


    /**
     * Constructs a WorkerNodeImpl with the executor service that is specified.
     * 
     * @param executor Executor service used to handle commands. 
     * @throws RemoteException If a remote communication error is raised.
     */
    protected WorkerNodeImpl(ExecutorService executor) throws RemoteException {
        super();
        this.keyValueStore = new HashMap<>();
        this.executor = executor;
    }

    /**
     * Main method to start the WorkerNode.
     * 
     * @param args Command line arguments, requiring only the port number as its argument.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            printWithTimestamp("Example Usage: java WorkerNodeImpl <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            WorkerNodeImpl worker = new WorkerNodeImpl(executor);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("WorkerNode", worker);
            printWithTimestamp("WorkerNode started on port " + port);
        } catch (Exception e) {
            logError("Error starting WorkerNode: " + e.getMessage());
        }
    }

    /**
     * Prepares the worker server node to execute a command.
     * 
     * @param command The command to be prepared. 
     * @param args Arguments for the command, if any.
     * @return A string reflecting the result of the preparation.
     * @throws RemoteException If a remote communication error is raised.
     */
    @Override
    public String prepareCommand(String command, String args[]) throws RemoteException {
        logClientRequest(command, args);
        return "PREPARED";
    }

    /**
     * Commit the command that was prepared previously.
     * 
     * @param command The command to be committed.
     * @param args Arguments for the command, if any.
     * @return A string reflecting the result of the commit operation.
     * @throws RemoteException If a remote communication error is raised.
     */
    @Override
    public String commitCommand(String command, String args[]) throws RemoteException {
        Callable<String> task = () -> {
            String result;
            switch (command) {
                case "PUT":
                    result = put(args);
                    break;
                case "GET":
                    result = get(args);
                    break;
                case "DELETE":
                    result = delete(args);
                    break;
                default:
                    result = "Invalid Command.";
            }
            logClientResponse(result);
            return result;
        };
        Future<String> future = executor.submit(task);
        try {
            return future.get();
        } catch (Exception e) {
            return "Error during commit: " + e.getMessage();
        }
    }

    /**
     * Rolls back the command that was prepared previously.
     * 
     * @param command The command to be rolled back.
     * @param args Arguments for the command, if any.
     * @return A string reflecting the result of the rollback operation.
     * @throws RemoteException If a remote communication error is raised.
     */
    @Override
    public String rollbackCommand(String command, String args[]) throws RemoteException {
        logClientRequest("ROLLBACK", args);
        return "ROLLBACK_SUCCESS";
    }

    /**
     * Handles the PUT operation.
     * 
     * @param args The arguments for the PUT operations (PUT <key> <value>)
     * @return A string indicating the result of the PUT operation.
     */
    private String put(String[] args) {
        if (args.length != 2) {
            return "PUT command requires key and value.";
        }
        keyValueStore.put(args[0], args[1]);
        return "PUT_SUCCESS";
    }

    /**
     * Handles the GET operation.
     * 
     * @param args The arguments for the GET operations (GET <key>)
     * @return A string indicating the result of the GET operation.
     */
    private String get(String[] args) {
        if (args.length != 1) {
            return "GET command requires key.";
        }
        return keyValueStore.getOrDefault(args[0], "KEY_NOT_FOUND");
    }

    /**
     * Handles the DELETE operation.
     * 
     * @param args The arguments for the DELETE operations (DELETE <key>)
     * @return A string indicating the result of the DELETE operation.
     */
    private String delete(String[] args) {
        if (args.length != 1) {
            return "DELETE command requires key.";
        }
        return keyValueStore.remove(args[0]) != null ? "DELETE_SUCCESS" : "KEY_NOT_FOUND";
    }

    /**
     * Helper method to print the message with a timestamp.
     * 
     * @param message The message to be printed along with the timestamp on the console.
     */
    private static void printWithTimestamp(String message) {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] " + message);
    }

    /**
     * Logs the requests received from the client.
     * 
     * @param command The command received by the client.
     * @param args The arguments for the command. 
     */
    private void logClientRequest(String command, String[] args) {
        printWithTimestamp("Received command: " + command + " with args: " 
        + String.join(", ", args));
    }

    /**
     * Logs the response to be sent to the client.
     * 
     * @param response The response sent to the client.
     */
    private void logClientResponse(String response) {
        printWithTimestamp("Sending response: " + response);
    }

    /**
     * Logs an error message.
     *
     * @param message The error message to be logged.
     */
    private static void logError(String message) {
        printWithTimestamp(message);
        synchronized (loggerLock) {
            logger.severe(message);
        }
    }
}