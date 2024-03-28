import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

public class Node {
    private String name;
    private String ipaddress;
    private int port;
    private String hashID;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private RoutingTable routingTable;
    private Map<String, String> keyValueStore;
    private CommunicationProtocol protocol;

    public Node() throws NoSuchAlgorithmException {
        this.executorService = Executors.newCachedThreadPool();
        this.keyValueStore = new ConcurrentHashMap<>(); // Use ConcurrentHashMap for thread safety
        this.protocol = new CommunicationProtocol(this); // Pass this Node to the CommunicationProtocol
    }

    private String calculateHashID(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append((hex.length() == 1) ? "0" : "").append(hex);
        }
        return hexString.toString();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Node '" + name + "' is running as a server on port " + port);
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    handleIncomingConnection(socket);
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    private void handleIncomingConnection(Socket socket) {
        executorService.execute(() -> protocol.handleCommunication(socket, this));
    }

    public void connectToNode(String nodeName, String nodeipAddress, int nodePort) {
        try (Socket socket = new Socket(nodeipAddress, nodePort)) {
            System.out.println("Connected to node '" + nodeName + "' at " + nodeipAddress + ":" + nodePort);
            NodeInfo nodeInfo = new NodeInfo(calculateHashID(nodeName), nodeName, nodeipAddress, nodePort);
            routingTable.addNode(nodeInfo);
            // Communication with the connected node is handled by the CommunicationProtocol
            protocol.handleCommunication(socket, this);
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Error connecting to node: " + e.getMessage());
        }
    }

    public void stopServer() {
        try {
            serverSocket.close();
            executorService.shutdownNow();
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public Map<String, String> getKeyValueStore() {
        return keyValueStore;
    }

    public String getName() {
        return name;
    }

    public String getipAddress() {
        return ipaddress;
    }

    public int getPort() {
        return port;
    }

    private void setArgs(String[] args) throws NoSuchAlgorithmException {
        this.name = args[0];
        String[] addressParts = args[1].split(":");
        this.ipaddress = addressParts[0];
        this.port = Integer.parseInt(addressParts[1]);
        this.hashID = this.calculateHashID(name);
        this.routingTable = new RoutingTable(3, hashID);

    }

    // Main method for testing
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Incorrect usage: <nodeName> <nodeAddress:port>");
                System.exit(1);
            }
            Node node = new Node();
            node.setArgs(args);
            // Start the server in a new thread to keep the main thread free
            new Thread(node::startServer).start();
            // Further initialization or connections can be handled here
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error initializing node: " + e.getMessage());
        }
    }

}
