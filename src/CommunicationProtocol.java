import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CommunicationProtocol {
    private RoutingTable routingTable;
    private Map<String, String> keyValueStore;
    private Node node;

    public CommunicationProtocol(Node node) {
        this.routingTable = node.getRoutingTable();
        this.keyValueStore = node.getKeyValueStore();
        this.node = node;
    }

    // Method to handle communication between nodes
    public void handleCommunication(Socket socket, Node node) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Send START message
            out.println("START 1 " + node.getName());
            // Receive START message from the other node
            String startMessage = in.readLine();
            // Process START message (if required)

            // Continue communication until END message is received
            String request;
            while ((request = in.readLine()) != null) {
                String[] parts = request.split(" ");
                String command = parts[0];
                switch (command) {
                    case "ECHO?":
                        out.println("OHCE");
                        break;
                    case "PUT?":
                        handlePutRequest(parts[1], parts[2], node.getipAddress(), node.getPort(), node.getName());
                        break;
                    case "GET?":
                        handleGetRequest(parts[1]);
                        break;
                    case "NOTIFY?":
                        handleNotifyRequest(parts[1], parts[2]);
                        break;
                    case "NEAREST?":
                        handleNearestRequest(parts[1]);
                        break;
                    case "END":
                        String reason = parts.length > 1 ? parts[1] : "No reason provided";
                        System.out.println("Connection ended: " + reason);
                        return;
                    default:
                        // Handle unknown command
                        out.println("Unknown command: " + command);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String calculateHashID(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    // Convert byte array to hexadecimal string
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Method to handle PUT? request
    public String handlePutRequest(String key, String value, String localAddress, int localPort, String localName) throws NoSuchAlgorithmException {
        // Compute hashID for the key
        String keyHash = calculateHashID(key);
        // Find the three closest nodes in the routing table
        List<NodeInfo> closestNodes = routingTable.findClosestNodes(keyHash);
        // Get the local node's information
        NodeInfo localNode = routingTable.getLocalNode(localName, localAddress, localPort);
        // Check if the local node is one of the closest nodes
        if (closestNodes.contains(localNode)) {
            // Store the (key, value) pair if this node is one of the closest nodes
            keyValueStore.put(key, value);
            return "SUCCESS";
        } else {
            // Forward the PUT request to nodes that are closer to the key
            for (NodeInfo node : closestNodes) {
                if (!node.equals(localNode)) {
                    // Forward the PUT request to the closer node
                    String result = forwardPutRequest(node, key, value);
                    if (result.equals("SUCCESS")) {
                        return "SUCCESS";
                    }
                }
            }
            return "FAILED"; // No closer node available to forward the request
        }
    }


    private String forwardPutRequest(NodeInfo nextNode, String key, String value) {
        if (nextNode == null) {
            return "FAILED"; // No more nodes to forward the request to
        }

        // Establish communication with the next node
        try (Socket socket = new Socket(nextNode.getAddress(), nextNode.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send the PUT request to the next node
            out.println("PUT " + key + " " + value);
            out.flush();

            // Wait for response from the next node
            String response = in.readLine();
            if (response != null && response.equals("SUCCESS")) {
                return "SUCCESS"; // Successfully stored the key-value pair
            } else {
                // If storing the key-value pair failed, return "FAILED"
                return "FAILED";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "FAILED"; // Error occurred during communication
        }
    }


    // Method to handle GET request
    public String handleGetRequest(String key) {
        // Look up the value for the given key in the key-value store
        String value = keyValueStore.get(key);
        if (value != null) {
            return value; // Return value if key exists
        } else {
            return "NOPE"; // Key not found
        }
    }

    public void handleNotifyRequest(String nodeName, String nodeAddress) throws NoSuchAlgorithmException {
        // Create a NodeInfo object for the notified node
        String[] parts = nodeAddress.split(":");
        NodeInfo newNode = new NodeInfo(calculateHashID(nodeName),nodeName, parts[0], Integer.parseInt(parts[1]));
        // Update the routing table with the information of the notified node
        routingTable.addNode(newNode);

        System.out.println("Received NOTIFY request from node '" + nodeName + "' with address " + nodeAddress);
    }

    // Method to handle NEAREST? request
    // Method to handle NEAREST? request
    public String handleNearestRequest(String hashID) {
        // Find the closest nodes to the given hashID
        List<NodeInfo> closestNodes = routingTable.findClosestNodes(hashID);

        // Build the response string
        StringBuilder response = new StringBuilder("NEAREST_NODES ");
        for (NodeInfo node : closestNodes) {
            response.append(node.getName()).append(" ").append(node.getAddress()).append(" ");
        }
        return response.toString().trim();
    }

}