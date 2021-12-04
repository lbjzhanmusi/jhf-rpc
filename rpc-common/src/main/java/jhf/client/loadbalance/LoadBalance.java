package jhf.client.loadbalance;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public interface LoadBalance {
    String chooseChannel(ConcurrentHashMap<String,Channel> map);

}
