package com.wuyinai.wuaipdce.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存源码文件的请求
 */
@Data
public class SourceCodeSaveRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件内容
     */
    private String content;
}
