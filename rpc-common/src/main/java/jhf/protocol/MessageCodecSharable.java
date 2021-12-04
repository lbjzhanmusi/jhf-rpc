package jhf.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;
import jhf.config.Config;
import jhf.message.Message;
import jhf.protocol.serializer.SerializerAlgortithm;

import java.io.Serializable;
import java.util.List;
@Slf4j
//双向的消息编解码器,自定义协议
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        //1.魔数，4个字节
        out.writeBytes(new byte[]{'y','y','d','s'});
        //2.协议版本，1个字节
        out.writeByte(1);
        //3.序列化版本 0:jdk，1个字节,通过配置类获取枚举类对象之一，在获取这个对象的偏移量
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        //4.指令类型，1个字节
        out.writeByte(msg.getMessageType());
        //5.序号，4个字节
        out.writeInt(msg.getSequenceId());
        //无意义，把消息头对齐填充到16字节
        out.writeByte(0xff);
        //获取msg的字节流
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);
        //6.正文长度，4个字节
        out.writeInt(bytes.length);
        //7.写入内容
        out.writeBytes(bytes);

        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> outList) throws Exception {
        int magicNum=in.readInt();
        byte version=in.readByte();
        byte serializerType=in.readByte();
        byte messageType=in.readByte();
        int sequenceiId=in.readInt();
        in.readByte();
        int length=in.readInt();
        byte[] bytes=new byte[length];
        in.readBytes(bytes,0,length);
        //根据消息头的序列化方法字段，获取相应的反序列化器
        //拿到消息的class对象，和消息内容的字节数组就可以反序列化出消息了
        SerializerAlgortithm[] algortithms = SerializerAlgortithm.values();
        Message message = algortithms[serializerType].deserialize(Message.getMessageClass(messageType), bytes);
//        log.debug("{},{},{},{},{},{}",magicNum,version,serializerType,messageType,sequenceiId,length);
//        log.debug("{}",message);
        outList.add(message);
    }

    public static void main(String[] args) {
        SerializerAlgortithm[] algortithms = SerializerAlgortithm.values();

        System.out.println(algortithms[0]);
        System.out.println(algortithms[1]);
    }
}
