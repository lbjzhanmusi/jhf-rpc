package jhf.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jhf.message.RpcResponseMessage;
import jhf.protocol.MessageIdPromiseMap;

public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        //在并发map里找到对应的消息序号的promise设置成功或失败，调用方法的用户线程已经阻塞在promise.await()那里，等着Netty线程处理结果
        MessageIdPromiseMap.getResponse(msg);
    }
}
