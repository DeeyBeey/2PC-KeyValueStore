import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * RMIClient class implements an RMI Client that connects to an RMI Server for execution of commands.
 */
public class RMIClient {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern
    ("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Logger logger = Logger.getLogger(RMIClient.class.getName());

    /**
     * Static initialization to set the logger up for the RMI Client.
     */
    static {
        try {
            FileHandler fileHandler = new FileHandler
            ("rmiclient.log", true); // Use append mode
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            printWithTimestamp("Failed to set up logger: " + e.getMessage());
        }
    }

    /**
     * Main method for the RMI Client.
     * @param args Command Line Arguments to run the client: hostname and port number of the server.
     */
    public static void main(String[] args) {
        // Check for correct number of arguments to run the client
        if (args.length != 2) {
            printWithTimestamp("Sample Usage: java RMIClient <hostname> <port number>");
            return;
        }

        // Extract the hostname and port number from the command-line arguments
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, port);
            CoordinatorHandler coordinatorHandler = (CoordinatorHandler) 
            registry.lookup("CoordinatorHandler");

            prepopulateServer(coordinatorHandler);

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String text;

            // Prompt the user until 'exit' is entered
            while (true) {
                printWithTimestamp("Enter command: ");
                text = consoleReader.readLine();

                if (text.equalsIgnoreCase("exit")) {
                    break;
                }

                String[] textParts = text.split(" ", 2);
                String command = textParts[0];
                String[] commandArgs = textParts.length > 1 ? textParts[1]
                .split(" ", 2) : new String[]{};

                try {
                    // Read the response sent by the server
                    String response = coordinatorHandler.handleCommand(command, commandArgs);
                    printWithTimestamp("Server response: " + response);
                } catch (IOException e) {
                    // Handling the case when no response from the server is received
                    String errorMessage = "Error communicating with server: " + e.getMessage();
                    printWithTimestamp(errorMessage);
                    logger.warning(errorMessage);
                }
            }
        } catch (Exception ex) {
            // Handling the case when the server is not found
            String errorMessage = "Client exception: " + ex.getMessage();
            printWithTimestamp(errorMessage);
            logger.severe(errorMessage);
        }
    }

    /**
     * Prepopulate the server with 5 commands each GET, POST and DELETE.
     * 
     * @param commandHandler The RemoteCommandHandler to send commands over to the server.
     */
    private static void prepopulateServer(CoordinatorHandler commandHandler) {
        String[] commands = {
            "PUT key1 value1",
            "PUT key2 value2",
            "PUT key3 value3",
            "PUT key4 value4",
            "PUT key5 value5",
            "GET key1",
            "GET key2",
            "GET key3",
            "GET key4",
            "GET key5",
            "DELETE key1",
            "DELETE key2",
            "DELETE key3",
            "DELETE key4",
            "DELETE key5"
        };

        for (String command : commands) {
            String[] commandParts = command.split(" ", 2);
            String cmd = commandParts[0];
            String[] cmdArgs = commandParts.length > 1 ? commandParts[1].split
            (" ") : new String[]{};

            try {
                String response = commandHandler.handleCommand(cmd, cmdArgs);
                printWithTimestamp("Prepopulated command: " + command + ", response: " + response);
            } catch (IOException e) {
                String errorMessage = "Error during prepopulation: " + e.getMessage();
                printWithTimestamp(errorMessage);
                logger.warning(errorMessage);
            }
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
}
