public class NodeInfo {
    private final String id;
    private final String address;
    private final int port;
    private final String name;

    public NodeInfo(String id, String name, String address, int port) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
    }

    // Getters for id, address, and port
    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }
}
