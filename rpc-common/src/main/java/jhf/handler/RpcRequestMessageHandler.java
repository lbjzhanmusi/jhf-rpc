package jhf.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import jhf.message.RpcRequestMessage;
import jhf.message.RpcResponseMessage;
import jhf.utils.ServicesFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        log.debug("开始处理RPC请求！");
        //应答的序号应该与请求序号一致，客户端才能区别
        RpcResponseMessage response=new RpcResponseMessage();
        response.setSequenceId(msg.getSequenceId());
        try {
            Object service = ServicesFactory.getService(Class.forName(msg.getInterfaceName()));
            Method method=service.getClass().getMethod(msg.getMethodName(),msg.getParameterTypes());;
            Object invoke = method.invoke(service,msg.getParameterValue());
            response.setReturnValue(invoke);
        }catch (Exception e){
            response.setExceptionValue(e);
        }
        //回送结果
        ctx.writeAndFlush(response);
        log.debug("RPC请求处理完毕并已经回送结果");
    }

    //服务端测试能否正确处理消息
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RpcRequestMessage msg = new RpcRequestMessage(1,
                "jhf.HelloService",
                "saySomething",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        );
        Object service = ServicesFactory.getService(Class.forName(msg.getInterfaceName()));
        Method method=service.getClass().getMethod(msg.getMethodName(),msg.getParameterTypes());;
        Object invoke = method.invoke(service,msg.getParameterValue());
        System.out.println(invoke);
    }
}
