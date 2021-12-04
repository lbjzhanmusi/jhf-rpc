package jhf.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import jhf.config.Config;
import jhf.handler.RpcRequestMessageHandler;
import jhf.protocol.MessageCodecSharable;
import jhf.protocol.ProcotolFrameDecoder;
import jhf.zookeeper.ZookeeperServer;

@Slf4j
public class RpcServer {
    //不需要顾及线程安全问题，生产者启动时初始化一次
    public static void init(){
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcRequestMessageHandler RPC_REQUEST = new RpcRequestMessageHandler();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //用于解决粘包半包的handler，不能提出来，因为这是线程不安全的
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    //10s没有请求就关闭连接
//                    ch.pipeline().addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                    //打印调试日志的东西
//                    ch.pipeline().addLast(LOGGING_HANDLER);
                    //自定义协议的解析器
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    //服务提供端对RPC请求的处理器
                    ch.pipeline().addLast(RPC_REQUEST);
                }
            });
            Channel channel = serverBootstrap.bind(Integer.parseInt(Config.getPort())).sync().channel();
            ZookeeperServer.register();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        init();
    }
}
