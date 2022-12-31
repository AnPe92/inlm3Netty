import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class ProxyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private Channel channel;
    private final NettyProxyServer server;



    public ProxyServerHandler(NettyProxyServer server) {
        this.server = server;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        var bootstrap = new Bootstrap();
        var node = server.getNodeHandler().next();

        this.channel = bootstrap
                .group(server.getWorkerGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        var pipeline = socketChannel.pipeline();
                        pipeline.addLast(new RelayHandler(node, ctx.channel()));


                    }
                })
                .connect("localhost", node.getPort())
                .channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
            channel.writeAndFlush(byteBuf.copy());

    }
}
