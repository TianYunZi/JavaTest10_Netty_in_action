package chapter20.practise.netty.client;

import chapter20.practise.netty.common.CustomerHeartbeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Admin on 2017/11/24.
 */
public class Client {

    private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
    private Channel channel;
    private Bootstrap bootstrap;

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        client.start();
        client.sendData();
    }

    public void sendData() throws InterruptedException {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 10000; i++) {
            if (channel != null && channel.isActive()) {
                String content = "client msg " + i;
                ByteBuf buf = channel.alloc().buffer(5 + content.getBytes().length);
                buf.writeInt(5 + content.getBytes().length);
                buf.writeByte(CustomerHeartbeatHandler.CUSTOM_MSG);
                buf.writeBytes(content.getBytes());
                channel.writeAndFlush(buf);
            }
            //Thread.sleep(random.nextInt(20000));
        }
    }

    public void start() {
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
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
                    ch.pipeline().addLast(new IdleStateHandler(0, 0, 5))
                            .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, -4, 0))
                            .addLast(new ClientHandler(Client.this));
                }
            });
            doConnect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }

        ChannelFuture future = bootstrap.connect("127.0.0.1", 18080);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channel = future.channel();
                    System.out.println("Connect to server successfully!");
                } else {
                    System.out.println("Failed to connect to server, try connect after 10s");
                    future.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 10, TimeUnit.SECONDS);
                }
            }
        });
    }
}
