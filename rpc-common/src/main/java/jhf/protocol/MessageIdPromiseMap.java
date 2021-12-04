package jhf.protocol;

import io.netty.util.concurrent.Promise;
import jhf.message.RpcResponseMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageIdPromiseMap{
    private static Map<Integer,Promise<Object>> map=new ConcurrentHashMap<>();
    public static boolean getResponse(RpcResponseMessage msg){
        Promise promise = map.remove(msg.getSequenceId());
        if(msg.getExceptionValue()==null){
            promise.setSuccess(msg.getReturnValue());
            return true;
        }else{
            promise.setFailure(msg.getExceptionValue());
            return false;
        }
    }
    public static void setPromise(Integer seqId,Promise<Object> promise){
        map.put(seqId, promise);
    }
}
