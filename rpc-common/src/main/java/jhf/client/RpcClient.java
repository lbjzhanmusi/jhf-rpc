package jhf.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import jhf.client.loadbalance.LoadBalance;
import jhf.config.Config;
import jhf.handler.RpcResponseMessageHandler;
import jhf.message.RpcRequestMessage;
import jhf.protocol.MessageCodecSharable;
import jhf.protocol.MessageIdPromiseMap;
import jhf.protocol.ProcotolFrameDecoder;
import jhf.utils.SequenceIdGenerator;
import jhf.zookeeper.ZookeeperClient;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClient {
    //需要顾及线程安全问题，防止多次初始化
    private static ConcurrentHashMap<String,Channel> map=new ConcurrentHashMap<>();
    private static LoadBalance loadBalance= Config.getLoadBalanceAlgortithm();
    public static Channel getChannel(){
        //从Zookeeper拉取服务列表
        if(map.size()==0)ZookeeperClient.getServerList();
        String ip = loadBalance.chooseChannel(map);
        return map.get(ip);
    }
    public static void updateChannel(List<String> address){
        for(String s:address){
            if(map.get(s)==null){
                init(s);
            }
        }
    }
    private static void init(String address) {
        //获取ip和port
        String[] split = address.split(":");
        String ip=split[0];
        int port=Integer.parseInt(split[1]);

        //
        NioEventLoopGroup group = new NioEventLoopGroup();
        RpcResponseMessageHandler RPC_RESPONSE=new RpcResponseMessageHandler();
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
//        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        try {
            Channel channel = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //处理粘包半包
                            ch.pipeline().addLast(new ProcotolFrameDecoder());
                            //打印下日志
//                            ch.pipeline().addLast(LOGGING_HANDLER);
                            //自定义协议出栈入栈处理器
                            ch.pipeline().addLast(MESSAGE_CODEC);
                            //RPC回应处理器
                            ch.pipeline().addLast(RPC_RESPONSE);
                        }
                    })
                    .connect(ip, port)
                    .sync()
                    .channel();
            //连接建立后把channel放到map中
            map.put(address,channel);
            channel.closeFuture().addListener((future)->{
                group.shutdownGracefully();
                //连接关闭时，移除channel，防止内存泄漏
                map.remove(address);
            });
        } catch (InterruptedException e) {
            log.error("RPC客户端初始化错误");
        }
    }
    public static <T> T getProxyService(Class<T> serviceClass){
        Class[] interfaces = new Class[]{serviceClass};
        Object object= Proxy.newProxyInstance(serviceClass.getClassLoader(),interfaces,((proxy, method, args) -> {
            RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(
                    SequenceIdGenerator.nextId(),
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );
            Channel channel = getChannel();
            channel.writeAndFlush(rpcRequestMessage);
            log.debug("写出数据了！");
            //调用这个方法的线程是用户线程，而发送数据和接受数据的线程是Netty的NIO线程，把（messageId，promise）放到map中，接受数据的时候根据messageId来设置结果
            DefaultPromise<Object> promise=new DefaultPromise<>(channel.eventLoop());
            MessageIdPromiseMap.setPromise(rpcRequestMessage.getSequenceId(),promise);
            //同步等待Netty收到response
            log.debug("开始等待数据！");
            promise.await();
            log.debug("收到数据了！");
            if(promise.isSuccess()){
                return promise.get();
            }else {
                throw new RuntimeException("服务方出问题了");
            }
        }));
        return (T)object;
    }
}
