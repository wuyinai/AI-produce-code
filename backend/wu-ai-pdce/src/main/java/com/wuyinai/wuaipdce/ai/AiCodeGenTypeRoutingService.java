package com.wuyinai.wuaipdce.ai;

import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型路由服务
 * 使用结构化输出直接返回枚举类型
 */

public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户输入，选择生成代码的类型
     */
    @SystemMessage(fromResource = "prompt/code-choose-routing-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
}
