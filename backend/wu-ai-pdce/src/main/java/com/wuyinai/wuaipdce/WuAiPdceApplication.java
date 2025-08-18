package com.wuyinai.wuaipdce;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableCaching//开启缓存
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.wuyinai.wuaipdce.mapper")

public class WuAiPdceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WuAiPdceApplication.class, args);
    }

}
