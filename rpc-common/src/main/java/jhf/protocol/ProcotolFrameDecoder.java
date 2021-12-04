package jhf.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
//处理粘包半包
public class ProcotolFrameDecoder extends LengthFieldBasedFrameDecoder {
    //此处有坑，当回送异常信息的时候，因为异常信息太长了，会导致消息接受出问题，应在服务提供方减少异常信息的回送
    public ProcotolFrameDecoder(){
        this(1024,12,4,0,0);
    }

    public ProcotolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
