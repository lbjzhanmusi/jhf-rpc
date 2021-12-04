package jhf.zookeeper;

import lombok.extern.slf4j.Slf4j;
import jhf.client.RpcClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;
@Slf4j
public class ZookeeperClient {
    private static String connectString="192.168.40.130:2181";
    private static int sessionTimeout=2000;
    private static ZooKeeper zk=null;
    public static void getServerList(){
        if(zk==null) {
            try {
                getConnect();
            } catch (IOException e) {
                throw new RuntimeException("连接Zookeeper出问题了");
            }
        }
        try {
            List<String>  children = zk.getChildren("/myRpcServices", true);
            log.debug("服务列表第一次拉去，或监听到更新：{}",children);
            RpcClient.updateChannel(children);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void getConnect() throws IOException {
        zk=new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                getServerList();
            }
        });
    }
}
