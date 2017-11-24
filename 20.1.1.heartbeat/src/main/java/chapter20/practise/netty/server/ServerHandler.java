package chapter20.practise.netty.server;

import chapter20.practise.netty.common.CustomerHeartbeatHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Admin on 2017/11/24.
 */
public class ServerHandler extends CustomerHeartbeatHandler {

    public ServerHandler() {
        super("server");
    }

    @Override
    protected void handleDate(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes() - 5];
        ByteBuf responseBuf = Unpooled.copiedBuffer(byteBuf);
        byteBuf.skipBytes(5);
        byteBuf.readBytes(data);
        String content = new String(data);
        System.out.println(name + " get content: " + content);
        channelHandlerContext.write(responseBuf);
    }

    @Override
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        super.handleReaderIdle(ctx);
        System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }
}
