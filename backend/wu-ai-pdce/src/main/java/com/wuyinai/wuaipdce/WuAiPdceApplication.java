package com.wuyinai.wuaipdce;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.wuyinai.wuaipdce.mapper")
public class WuAiPdceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WuAiPdceApplication.class, args);
    }

}
