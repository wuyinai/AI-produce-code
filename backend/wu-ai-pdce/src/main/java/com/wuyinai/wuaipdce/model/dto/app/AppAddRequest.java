package com.wuyinai.wuaipdce.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt提示词
     */
    private String initPrompt;

    /**
     * 代码生成类型（可选，如果不传则由AI自动选择）
     */
    private String codeGenType;

    private static final long serialVersionUID = 1L;
}
