package com.wuyinai.wuaipdce.ai;

import com.wuyinai.wuaipdce.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成类型路由服务工厂
 *
 * @author wuyinai
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {


    /**
     * 创建AI代码生成类型路由服务实例
     */
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingServicePrototype() {
        //动态获取多例的路由ChatModel ，支持并发
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype",ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 默认提供一个Bean
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService(){
        return aiCodeGenTypeRoutingServicePrototype();
    }
}
