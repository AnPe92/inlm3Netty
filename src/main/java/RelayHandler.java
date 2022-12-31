import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.Channel;
public class RelayHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Node node;
    private final Channel channel;

    public RelayHandler(Node node, Channel channel) {
        this.node = node;
        this.channel = channel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        channel.writeAndFlush(byteBuf.copy());
        node.addRequest();
        System.out.println("in channelread relayhandler");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        node.addConnection(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        node.removeConnection(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
