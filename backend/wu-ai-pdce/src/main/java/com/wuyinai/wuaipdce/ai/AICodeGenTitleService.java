package com.wuyinai.wuaipdce.ai;

import com.wuyinai.wuaipdce.ai.model.HtmlCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AICodeGenTitleService {

    @SystemMessage(fromResource = "prompt/codegen-title-system-prompt.txt")
    String generateHtmlCode(String userMessage);
}
