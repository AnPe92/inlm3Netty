import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NodeHandler {

    private final NettyProxyServer server;
    private final List<Node> currentNodes;
    private final LoadBalancer balancer;
    private int portCounter;
    private final ReentrantReadWriteLock lock;
    private boolean alive = true;

    public List<Node> getCurrentNodes() {
        return currentNodes;
    }

    public NodeHandler(NettyProxyServer server, LoadBalancer balancer) {
        this.server = server;
        this.currentNodes = new ArrayList<>();
        this.balancer = balancer;
        this.lock = new ReentrantReadWriteLock();
        this.portCounter = 8080;

        this.startNode(2);
        this.startThread();
    }


    public void addStartedNodesToList(Node node){
        currentNodes.add(node);
    }
    public Node next() {
        return balancer.next(currentNodes);
    }

 // <------------------------
    public void startNode(int amount){
        System.out.println("Starting " + amount + " nodes!");
        for (int i = 0; i < amount; i++) {

            var node = new Node(portCounter++);
            //startingQueue.add(node);

            try {
                node.start(portCounter);
                addStartedNodesToList(node);
                System.out.println(currentNodes.size() + " this is amount of nodes");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startThread() {
        var thread = new Thread(this::runThread);

        thread.start();
    }

    private void runThread() {
        try {
            lock.writeLock().lock();
            if (!alive) {
                return;
            }

            nodeTrafficEqualizer();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }

        try {
            //kollar var 10 s
            Thread.sleep(10000);
        } catch(Exception e) {
            e.printStackTrace();
        }

        runThread();
    }


    // check request per minute, if less than 1 per node remove node, if more than 2 per node add node

    public void nodeTrafficEqualizer(){

        var request = 0.0;

        for(var node : currentNodes) {
            request += node.getRequests();
            node.resetRequests();
        }

        var requestsPerNode = request / (double)currentNodes.size();
        request = 0.0;

        if(requestsPerNode < 2 && currentNodes.size() > 2) {
            var node = currentNodes.get(currentNodes.size()-1);
            node.stop();
        }

        if (requestsPerNode > 4) {
            this.startNode(1);
        }

        System.out.println("Amount of request per node every 10/s is: " + requestsPerNode);
    }
}
