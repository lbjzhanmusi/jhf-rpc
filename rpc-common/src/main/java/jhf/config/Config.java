package jhf.config;

import lombok.extern.slf4j.Slf4j;
import jhf.client.loadbalance.LoadBalance;
import jhf.client.loadbalance.LoadBalanceAlgortithm;
import jhf.protocol.serializer.Serializer;
import jhf.protocol.serializer.SerializerAlgortithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
@Slf4j
public class Config {
    private static Properties properties;
    private static final SerializerAlgortithm DEFAULT_SERIALIZER_ALGORTITHM=SerializerAlgortithm.valueOf("Java");
    private static final String DEFAULT_PORT="8080";
    private static final LoadBalance DEFAULT_LOAD_BALANCE=LoadBalanceAlgortithm.valueOf("RoundRobin");
    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static SerializerAlgortithm getSerializerAlgorithm(){
        String value=properties.getProperty("jhf.rpc.client.serializer.algorithm");
        //默认采用JDK的序列化算法
        if(value==null){
            return DEFAULT_SERIALIZER_ALGORTITHM;
        }
        Serializer algortithm = SerializerAlgortithm.valueOf(value);
        if(algortithm==null){
            throw new RuntimeException("不支持该序列化算法:"+value);
        }
        return SerializerAlgortithm.valueOf(value);
    }
    public static String getPort(){
        String value=properties.getProperty("jhf.rpc.server.port");
        if(value==null){
            return DEFAULT_PORT;
        }
        return value;
    }
    public static String getZookeeperAddress(){
        String value=properties.getProperty("jhf.zookeeper.address");
        if(value==null){
            throw new RuntimeException("请在application.properties的jhf.zookeeperAddress字段中写入Zookeeper的地址");
        }
        return value;
    }

    public static LoadBalance getLoadBalanceAlgortithm() {
        String value=properties.getProperty("jhf.rpc.client.loadbalance.algorithm");
        //默认采用JDK的序列化算法
        if(value==null){
            return DEFAULT_LOAD_BALANCE;
        }
        LoadBalanceAlgortithm algortithm = LoadBalanceAlgortithm.valueOf(value);
        if(algortithm==null){
            throw new RuntimeException("不支持该负载均衡算法:"+value);
        }else{
            return LoadBalanceAlgortithm.valueOf(value);
        }
    }
}
