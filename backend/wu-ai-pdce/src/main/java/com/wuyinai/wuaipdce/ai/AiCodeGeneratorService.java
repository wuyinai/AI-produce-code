package com.wuyinai.wuaipdce.ai;

import com.wuyinai.wuaipdce.ai.model.HtmlCodeResult;
import com.wuyinai.wuaipdce.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {
    /**
     * 一般输出生成HTML代码
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 一般输出生成老三样分离HTML代码
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-system-prompt.txt")
    MultiFileCodeResult generateMultiHtmlCode(String userMessage);

    /**
     * 流式输出生成HTML代码
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStreaming(String userMessage);

    /**
     * 流式输出生成老三样分离HTML代码
     *
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-system-prompt.txt")
    Flux<String> generateMultiHtmlCodeStreaming(String userMessage);

    /**
     * 生成Vue项目代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成过程的流逝相应
     */
    @SystemMessage(fromResource = "prompt/code-vue-project-prompt.txt")
    Flux<String> generateVueCodeStreaming(@MemoryId long memoryId, @UserMessage String userMessage);



    /**
     * 方案1：在每次对话添加一个id
     * @param memoryId
     * @param userMessage
     * @return
     */
//    HtmlCodeResult generateHtmlCode(@MemoryId int memoryId, @UserMessage String userMessage);

}
