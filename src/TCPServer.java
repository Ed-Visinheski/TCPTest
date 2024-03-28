import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {
    private BufferedReader reader;
    private Writer writer;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private String version = "1";
    private String nodeName, nodeAddress, ipAddress, port;
    private Map<String, ServerInfo> networkMap;

    public TCPServer() {
        networkMap = new HashMap<>();
    }

    public static void main(String[] args) throws IOException {
        TCPServer tcpServer = new TCPServer();
        tcpServer.startServer(args);
    }


    private void startServer(String[] args) throws IOException {
        argumentCheck(args);
        InetAddress host = InetAddress.getByName(ipAddress);
        serverSocket = new ServerSocket(Integer.parseInt(port));
        System.out.println(nodeName + " listening on port " + port);

        while (true) {
            clientSocket = serverSocket.accept();
            Thread clientThread = new Thread(new ClientHandler(clientSocket, this));
            clientThread.start();
        }
    }

    private static class ServerInfo {
        private String nodeName;
        private String nodeAddress;

        public ServerInfo(String nodeName, String nodeAddress) {
            this.nodeName = nodeName;
            this.nodeAddress = nodeAddress;
        }
    }




    private void argumentCheck(String[] args) throws IOException {
        try {
            if (args.length != 4) {
                throw new IOException();
            }
            else{
                this.nodeName = args[0];
                this.nodeAddress = args[1];
                this.ipAddress = args[2];
                this.port = args[3];
            }
        } catch (IOException e) {
            System.out.println("Incorrect usage: <nodeName> <nodeAddress> <ipAddress> <port>");
            System.exit(1);
        }
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    public String getNodeAddress() {
        return nodeAddress;
    }
    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }
}
