package chapter20.practise.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by Admin on 2017/11/24.
 */
public class Server {

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        /**
                         * This method will be called once the {@link Channel} was registered. After the method returns this instance
                         * will be removed from the {@link ChannelPipeline} of the {@link Channel}.
                         *
                         * @param ch the {@link Channel} which was registered.
                         * @throws Exception is thrown if an error occurs. In that case it will be handled by
                         *                   {@link #exceptionCaught(ChannelHandlerContext, Throwable)} which will by default close
                         *                   the {@link Channel}.
                         */
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(20, 0, 0))
                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, -4, 0))
                                    .addLast(new ServerHandler());
                        }
                    });
            Channel channel = bootstrap.bind(18080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
