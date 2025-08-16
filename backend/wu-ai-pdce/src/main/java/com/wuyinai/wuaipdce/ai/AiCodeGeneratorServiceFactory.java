package com.wuyinai.wuaipdce.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wuyinai.wuaipdce.ai.tools.ToolManager;
import com.wuyinai.wuaipdce.exception.BusinessException;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;
import com.wuyinai.wuaipdce.service.ChatHistoryService;
import com.wuyinai.wuaipdce.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 服务工厂
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    //将streamingChatModel改为openAiStreamingChatModel

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    /**
     * 一般输出创建 AI 服务不能和 流式输出创建 AI 服务使用同一个 ChatModel
     *
     * @return
     */
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }

    /**
     * AI服务实例缓存
     * 缓存策略：
     *  最大缓存1000个实例
     *  写入后30分钟过期
     *  访问后10分钟过期
     */
    private final Cache<String,AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key,value,cause)->{
                log.debug("AI服务实例被移除，appId：{}，原因：{}",key,cause);
            })
            .build();


    /**
     * 根据 appId 获取 AI 服务(带缓存),这个方法是为了兼容历史逻辑
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorServiceWithCache(long appId) {
        return getAiCodeGeneratorServiceWithCache(appId,CodeGenTypeEnum.HTML);
    }

    /**
     * 根据 appId 获取 AI 服务(带缓存)
     * @param appId,codeGenType
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorServiceWithCache(long appId, CodeGenTypeEnum codeGenType) {
        log.info("从缓存中获取appId：{} 对应的AI服务",appId);
        String cacheKey = buildCacheKey(appId,codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId,codeGenType));
    }
    /**
     * 根据 appId 创建独立的 AI 服务
     * @param appId
     * @return
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        log.info("为appId：{} 创建独立的AI服务",appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)// 最大消息数
                .build();
        chatHistoryService.loadChatHistoryToMemory(appId,chatMemory,20);//从数据库中获取对话，并保存到缓存中
        // 根据代码生成类型选择不同的模型配置
        return switch(codeGenType){
            case VUE_PROJECT ->{
                StreamingChatModel reasoningStreamingChatModelPrototype = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                yield  AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(reasoningStreamingChatModelPrototype)//使用的模型
                    .chatMemoryProvider(memoryId -> chatMemory)//因为使用MomeryId，所以这里需要使用Provider
                    .tools(toolManager.getAllTools())//需要使用工具
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                            toolExecutionRequest,"Error: there is no tool called" + toolExecutionRequest
                                    .name()
                    ))//工具如果不存在时，返回错误信息，防止AI幻觉。
                    .build();
            }
            case HTML,MULTI_FILE -> {
                StreamingChatModel streamingChatModelPrototype = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield  AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModelPrototype)
                    .chatMemory(chatMemory)
                    .build();
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);

        };

    }
    /**
     * 流式输出创建 AI 服务
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return createAiCodeGeneratorService(0L,CodeGenTypeEnum.HTML);
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}


