import java.util.*;

public class RoutingTable {
    private final int k; // The maximum number of nodes per bucket
    private final int bucketSize = 64; // The number of bits in the hash ID
    private final String localNodeId; // The local node's ID
    private final List<List<NodeInfo>> buckets; // List of buckets, each containing nodes

    public RoutingTable(int k, String localNodeId) {
        this.k = k;
        this.localNodeId = localNodeId;
        this.buckets = new ArrayList<>(bucketSize);
        initializeBuckets();
    }

    // Initialize the routing table with empty buckets
    private void initializeBuckets() {
        for (int i = 0; i < bucketSize; i++) {
            buckets.add(new ArrayList<>());
        }
    }

    // Add a node to the routing table
    public void addNode(NodeInfo node) {
        int bucketIndex = getBucketIndex(node.getId());
        List<NodeInfo> bucket = buckets.get(bucketIndex);

        // Check if the node already exists in the bucket
        if (!bucket.contains(node)) {
            if (bucket.size() < k) {
                // Bucket is not full, add the node
                bucket.add(node);
            } else {
                // Bucket is full, apply replacement policy
                // For simplicity, we replace the last node in the bucket
                bucket.remove(bucket.size() - 1);
                bucket.add(node);
            }
        }
    }

    // Remove a node from the routing table
    public void removeNode(NodeInfo node) {
        int bucketIndex = getBucketIndex(node.getId());
        List<NodeInfo> bucket = buckets.get(bucketIndex);
        bucket.remove(node);
    }

    // Get the bucket index for a given node ID
    private int getBucketIndex(String nodeId) {
        // Compute XOR distance between the local node and the target node
        String xorDistance = calculateXOR(localNodeId, nodeId);
        // Find the leftmost non-zero bit index (bucket index)
        for (int i = 0; i < xorDistance.length(); i++) {
            if (xorDistance.charAt(i) == '1') {
                return i;
            }
        }
        // If all bits are zero, return the last bucket index
        return bucketSize - 1;
    }

    // Calculate XOR distance between two node IDs
    private String calculateXOR(String nodeId1, String nodeId2) {
        // Placeholder implementation, assumes both node IDs have the same length
        StringBuilder xorResult = new StringBuilder();
        for (int i = 0; i < nodeId1.length(); i++) {
            xorResult.append(nodeId1.charAt(i) ^ nodeId2.charAt(i));
        }
        return xorResult.toString();
    }

    // Get the nodes in a specific bucket
    public List<NodeInfo> getBucketNodes(int index) {
        return buckets.get(index);
    }

    // Get all nodes in the routing table
    public List<NodeInfo> getAllNodes() {
        List<NodeInfo> allNodes = new ArrayList<>();
        for (List<NodeInfo> bucket : buckets) {
            allNodes.addAll(bucket);
        }
        return allNodes;
    }

    // Get the local node's information
    public NodeInfo getLocalNode(String name, String address, int port) {
        return new NodeInfo(localNodeId, name, address, port);
    }

    // Find the closest nodes to a given hash ID
    public List<NodeInfo> findClosestNodes(String hashID) {
        // Convert hashID to byte array
        byte[] targetHashID = hashID.getBytes();
        // Initialize a list to store closest nodes
        List<NodeInfo> closestNodes = new ArrayList<>();
        // Initialize minimum distance to the maximum possible value
        int minDistance = Integer.MAX_VALUE;

        // Iterate through all nodes in the routing table
        for (List<NodeInfo> bucket : buckets) {
            for (NodeInfo node : bucket) {
                // Calculate distance between the target hash ID and node's ID
                int distance = calculateDistance(targetHashID, node.getId().getBytes());
                // If distance is less than the minimum distance, update the closest nodes list
                if (distance < minDistance) {
                    closestNodes.clear();
                    closestNodes.add(node);
                    minDistance = distance;
                } else if (distance == minDistance) {
                    closestNodes.add(node);
                }
            }
        }
        return closestNodes;
    }

    // Calculate the distance between two hash IDs
    private int calculateDistance(byte[] hashID1, byte[] hashID2) {
        // Assume both hash IDs have the same length
        int distance = 0;
        for (int i = 0; i < hashID1.length; i++) {
            distance += Integer.bitCount(hashID1[i] ^ hashID2[i]);
        }
        return distance;
    }
}
