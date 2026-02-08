package com.wuyinai.wuaipdce.core.handler;


import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;
import com.wuyinai.wuaipdce.service.AppVersionService;
import com.wuyinai.wuaipdce.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器：
 * 1. 传统的 Flux<String> 流（HTML、MULTI_FILE） -> SimpleTextStreamHandler
 * 2. TokenStream 格式的复杂流（VUE_PROJECT） -> JsonMessageStreamHandler
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    @Resource
    @Lazy
    private AppVersionService appVersionService;

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case VUE_PROJECT -> // 使用注入的组件实例
                    jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser)
                            .doOnComplete(() -> autoCreateVersion(appId, loginUser));
            case HTML, MULTI_FILE, MINIPROGRAM -> // 简单文本处理器不需要依赖注入
                    new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser)
                            .doOnComplete(() -> autoCreateVersion(appId, loginUser));
        };
    }

    /**
     * 自动创建新版本
     * @param appId 应用ID
     * @param loginUser 登录用户
     */
    private void autoCreateVersion(long appId, User loginUser) {
        try {
            appVersionService.autoCreateVersion(
                    appId,
                    "AI对话生成",
                    null,
                    loginUser.getId()
            );
        } catch (Exception e) {
            log.error("自动创建版本失败: appId={}, error={}", appId, e.getMessage(), e);
        }
    }
}
