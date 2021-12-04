package jhf;

import lombok.extern.slf4j.Slf4j;
import jhf.client.RpcClient;

@Slf4j
public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        HelloService helloService= RpcClient.getProxyService(HelloService.class);
        //两次调用测试负载均衡
        System.out.println("远程调用的结果是"+helloService.saySomething("jhf"));
        System.out.println("远程调用的结果是"+helloService.saySomething("jhf"));
    }
}
