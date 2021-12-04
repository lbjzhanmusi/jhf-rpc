package jhf.zookeeper;

import lombok.extern.slf4j.Slf4j;
import jhf.config.Config;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
@Slf4j
public class ZookeeperServer {
    private static ZooKeeper zooKeeper=null;
    public static void register(){
        //把自己作为服务注册到ZooKeeper上
        log.debug("开始去注册到Zookeeper");
        try {
            zooKeeper = new ZooKeeper(Config.getZookeeperAddress(), 2000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {

                }
            });
            //如果没有这个节点就创建
            Stat exists = zooKeeper.exists("/myRpcServices", false);
            if(exists==null){
                zooKeeper.create("/myRpcServices","null".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            //在这个节点下注册自己
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            byte[] localIp = hostAddress.getBytes();
            zooKeeper.create("/myRpcServices/"+hostAddress+":"+ Config.getPort(),localIp, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
            log.debug("注册成功");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        register();
        Thread.sleep(10000000);
    }
}
