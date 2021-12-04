package jhf.client.loadbalance;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public enum  LoadBalanceAlgortithm implements LoadBalance {
    RoundRobin{
        int i=0;
        @Override
        public String chooseChannel(ConcurrentHashMap<String, Channel> map) {
            ArrayList<String> list = new ArrayList<>();
            for(String ip:map.keySet()){
                list.add(ip);
            }
            return list.get((i++)%list.size());
        }
    }
}
