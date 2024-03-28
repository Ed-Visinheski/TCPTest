import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader reader;
    private TCPServer server;
    private Writer writer;
    private String version = "1";
    private String nodeName, nodeAddress, ipAddress, port;

    public ClientHandler(Socket clientSocket, TCPServer server) throws IOException {
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = new OutputStreamWriter(clientSocket.getOutputStream());
        this.server = server;
    }

    public void run() {
        try {
            String message = reader.readLine();
            String[] command = message.split(" ");
            System.out.println("The client said : " + message);
            checkStart(command);
            String comVersion = comVersionCheck(command);
            String incomingNodeName = command[2];
            if (comVersion != null) {
                writer.write("START " + comVersion + " " + nodeName + "\n");
                writer.flush();
            }
            while (true) {
                message = reader.readLine();
                command = message.split(" ");
                switch (command[0]) {
                    case "END":
                        if(command.length < 1){
                            System.out.println(incomingNodeName+" said : " + message);
                            writer.write("END INVALID END: END <END MESSAGE>" + "\n");
                            writer.flush();
                            clientSocket.close();
                            break;
                        }
                        System.out.println(incomingNodeName+" said : " + message);
                        writer.write("END" + "\n");
                        writer.flush();
                        break;
                    case "ECHO?":
                        if (command.length > 1) {
                            System.out.println(incomingNodeName+" said : " + message);
                            writer.write("END" + "\n");
                            writer.flush();
                            clientSocket.close();
                            break;
                        } else {
                            System.out.println(incomingNodeName+" said : " + message);
                            writer.write("OHCE" + "\n");
                            writer.flush();
                            break;
                        }
                    case "PUT?":
                        if(command.length != 3){
                            System.out.println(incomingNodeName+" said : " + message);
                            writer.write("END INVALID PUT: PUT? <KEY> <VALUE>" + "\n");
                            writer.flush();
                            clientSocket.close();
                            break;
                        }
                        else{
                            System.out.println(incomingNodeName+" said : " + message);
                            writer.write("END" + "\n");
                            writer.flush();
                            break;
                        }
                    default:
                        System.out.println(incomingNodeName+" said : " + message);
                        writer.write("END Unknown Command" + "\n");
                        writer.flush();
                        clientSocket.close();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void checkLength(String[] command) throws IOException {
        if(command.length != 3){
            writer.write("END Incorrect START message: START <PROTOCOLVERSION> <NODENAMDE>" + "\n");
            writer.flush();
            clientSocket.close();
        }
    }
    private String comVersionCheck(String[] command) throws IOException {
        if(Integer.parseInt(command[1]) < Integer.parseInt(version) ||
                Integer.parseInt(command[1]) == Integer.parseInt(version)){
            return command[1];
        }
        if(Integer.parseInt(command[1]) > Integer.parseInt(version)){
            return version;
        }
        else{
            writer.write("END Incorrect START message: START <PROTOCOLVERSION> <NODENAMDE>" + "\n");
            writer.flush();
        }
        return null;
    }

    private void checkStart(String[] command) throws IOException {
        if(command[0].equals("START")){
            checkLength(command);
        }
        else{
            writer.write("END Incorrect START message: START <PROTOCOLVERSION> <NODENAMDE>" + "\n");
            writer.flush();
            clientSocket.close();
        }
    }
    private void argumentCheck(String[] args) throws IOException {
        try {
            if (args.length != 4) {
                throw new IOException();
            }
            else{
                nodeName = args[0];
                nodeAddress = args[1];
                ipAddress = args[2];
                port = args[3];
            }
        } catch (IOException e) {
            System.out.println("Incorrect usage: <nodeName> <nodeAddress> <ipAddress> <port>");
            System.exit(1);
        }
    }
}