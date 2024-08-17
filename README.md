# Distributed Systems Project #3

This project implements a multithreaded key-value store using RPC (Java RMI) along with replication across mutliple distinct servers. The project includes the following components:

1. `Coordinator` -  Coordinator Implementation
2. `CoordinatorHandler` - RMI Interface for handling coordinator commands.
3. `CoordinatorHandlerImpl` - Implementation of the `CoordinatorHandler` interface.
4. `WorkerNode` - RMI Interface for handling worker node commands.
5. `WorkerNodeImpl` - Implementation of the `WorkerNode` interface.
6. `WorkerHandler` - Callable for handling worker node operations.
7. `RMI Client` - RMI Client implementation to interact with the coordinator.  

## Prerequisites

Ensure that you have the Java Development Kit (JDK) installed on your system. You can download it from [here](https://www.oracle.com/java/technologies/javase-downloads.html).

## Compilation
To compile the Java files, open a terminal or command prompt and navigate to the directory containing the source files. Use the following command to compile each file:

```
javac *.java
```

## Running the Servers
To start the RMI servers (worker nodes), use the following commands. Replace `<port>` with the port number you wish to use (e.g., 32000).

>Note: Perform this process 5 times to have 5 active servers.

```
java WorkerNodeImpl <port>
```
## Running thr Coordinator
To start the Coordinator, use the following command. Replace `<port>` with the port number you wish to use and `<list of RMI Servers>` with the `localhost:port` of the 5 distinct servers that have been started in the previous step.

```
java Coordinator <port> <list of RMI Servers>
```

## Running the Client
To start the RMI client, use the following commands. Replace `<hostname>` with the server's hostname or IP address (e.g., localhost), and `<port>` with the same port number used for the coordinator.

``` 
java RMIClient <hostname> <port>
```

## Example Usage
Here is an example of how to run the coordinator, servers and clients.

### Starting the RMI Servers
```
java WorkerNodeImpl 32001
```
```
java WorkerNodeImpl 32002
```
```
java WorkerNodeImpl 32003
```
```
java WorkerNodeImpl 32004
```
```
java WorkerNodeImpl 32005
```
>Note: Ensure the above instances are on separate terminals.

### Starting the Coordinator
```
java Coordinator 32000 localhost:32001 localhost:32002 localhost:32003 localhost:32004 localhost:32005 
```
### Running the RMI Client
```
java RMIClient localhost 32000
```

>Note: Ensure that the server is running first and then setup the coordinator. Use separate terminals or consoles for each client/server/coordinator.