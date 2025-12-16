package com.wuyinai.wuaipdce.model.dto.app;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 保存直接修改的请求
 */
@Data
public class AppSaveDirectEditRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 修改的文件列表
     */
    private List<EditedFile> files;

    /**
     * 修改的文件信息
     */
    @Data
    public static class EditedFile implements Serializable {
        /**
         * 文件路径
         */
        private String filePath;

        /**
         * 文件内容
         */
        private String content;
    }
}