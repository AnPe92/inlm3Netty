
import java.io.IOException;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Node {


    private final ReentrantReadWriteLock lock;
    private int port;
    private List<Channel> nodeList;
    private Process process;
    private ConcurrentHashMap cachedData;

    private int requests;

    public Node(int port) {
        this.lock = new ReentrantReadWriteLock();
        this.port = port;
        this.nodeList = new ArrayList<>();
    }

    public void start(int port) throws IOException {
        var args = new String[] {
                "java",
                "-jar",
                "spring.jar",
                "--server.port=" + port
        };

        this.process = Runtime
                .getRuntime()
                .exec(args);

        System.out.println("Started node: " + process.pid());
    }

    public int getPort() {
        return port;
    }


    public void addConnection(Channel channel) {
        try {
            lock.writeLock().lock();
            this.nodeList.add(channel);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addRequest() {
        try {
            lock.writeLock().lock();
            this.requests++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeConnection(Channel channel) {
        try {
            lock.writeLock().lock();
            this.nodeList.remove(channel);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void resetRequests() {
        try {
            lock.writeLock().lock();
            this.requests = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getRequests() {
        return requests;
    }

    public void stop() {
        if (process == null)
            return;

        process.destroyForcibly();
        System.out.println("Destroyed node: " + process.pid());
        process = null;
    }
}
