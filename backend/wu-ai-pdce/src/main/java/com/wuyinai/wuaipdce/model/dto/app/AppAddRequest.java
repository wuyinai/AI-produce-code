package com.wuyinai.wuaipdce.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt提示词
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}
