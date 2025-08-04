package com.wuyinai.wuaipdce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class WuAiPdceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WuAiPdceApplication.class, args);
    }

}
