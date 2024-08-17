import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * The Coordinator class is the central coordinator in the distributed system handling requests 
 * from clients and managing other worker servers for executing tasks.
 */
public class Coordinator {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern
    ("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Logger logger = Logger.getLogger(Coordinator.class.getName());
    private static final int THREAD_POOL_SIZE = 5;
    private static final Object loggerLock = new Object();
    private static List<WorkerNode> workerNodes = new ArrayList<>();

    static {
        try {
            FileHandler fileHandler = new FileHandler("coordinator.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            synchronized (loggerLock) {
                logger.addHandler(fileHandler);
                logger.setUseParentHandlers(false);
            }
        } catch (IOException e) {
            printWithTimestamp("Error setting the logger up: " + e.getMessage());
        }
    }

    /**
     * Main method to start the Coordinator.
     * 
     * @param args Command line argument where the first argument is the port number along with
     *             multiple subsequent arguments which are the worker node addresses as pairs of 
     *             hostname:port number.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            printWithTimestamp
            ("Example Usage: java Coordinator <port number> <worker node addresses>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        for (int i = 1; i < args.length; i++) {
            String[] parts = args[i].split(":");
            String hostname = parts[0];
            int workerPort = Integer.parseInt(parts[1]);
            try {
                Registry registry = LocateRegistry.getRegistry(hostname, workerPort);
                WorkerNode workerNode = (WorkerNode) registry.lookup("WorkerNode");
                workerNodes.add(workerNode);
            } catch (Exception e) {
                printWithTimestamp("Error connecting to worker node(s): " + e.getMessage());
                return;
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            printWithTimestamp("Coordinator running on port " + port);
            CoordinatorHandlerImpl handler = new CoordinatorHandlerImpl(workerNodes, executor);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("CoordinatorHandler", handler);
            printWithTimestamp
            ("Coordinator started and binding with CoordinatorHandler confirmed.");
        } catch (IOException ex) {
            printWithTimestamp("Exception at Coordinator: " + ex.getMessage());
            logError("Exception at Coordinator: " + ex.getMessage());
            executor.shutdown();
        }
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
     * Logs error messages to the file logger.
     * 
     * @param message The error message to be logged.
     */
    private static void logError(String message) {
        synchronized (loggerLock) {
            logger.severe(message);
        }
    }
}
