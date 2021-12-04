package jhf.impl;

import jhf.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String saySomething(String s) {
        return s;
    }
}
