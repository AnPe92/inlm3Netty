import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Scanner;

public class NettyProxyServer {

    private final int port;
    private final NodeHandler nodeHandler;
    private final EventLoopGroup bossGroup, workerGroup;

    private final InMemoryCache cache = new InMemoryCache();
    public NettyProxyServer(int port) {
        this.port = port;
        this.nodeHandler = new NodeHandler(this, new LoadBalancerHandler());
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }



    public void start() throws InterruptedException {

        var bootstrap = new ServerBootstrap();

        var channel = bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        System.out.println("Inside nettyproxyserver initizlizer -> handler");
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new ProxyServerHandler(NettyProxyServer.this));

                    }
                })
                .bind(port)
                .sync()
                .channel();


        Scanner scanner = new Scanner(System.in);
        while (!scanner.nextLine().equals("exit")){}

        channel.close();

        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();

    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public NodeHandler getNodeHandler() {
        return nodeHandler;
    }

    public int getPort() {
        return port;
    }
}
