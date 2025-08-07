package com.wuyinai.wuaipdce.ai;

import com.wuyinai.wuaipdce.ai.model.HtmlCodeResult;
import com.wuyinai.wuaipdce.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {
    /**
     * 一般输出生成HTML代码
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 一般输出生成老三样分离HTML代码
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-system-prompt.txt")
    MultiFileCodeResult generateMultiHtmlCode(String userMessage);

    /**
     * 流式输出生成HTML代码
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStreaming(String userMessage);

    /**
     * 流式输出生成老三样分离HTML代码
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-system-prompt.txt")
    Flux<String> generateMultiHtmlCodeStreaming(String userMessage);
}
