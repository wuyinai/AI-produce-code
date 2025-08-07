package com.wuyinai.wuaipdce.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
/**
 * @Description: html代码生成结果
 */
@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("生成代码的描述")
    private String description;
}

