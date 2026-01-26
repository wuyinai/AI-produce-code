package com.wuyinai.wuaipdce.model.dto.app;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 源码文件节点DTO
 */
@Data
public class SourceCodeFileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件路径（相对于根目录）
     */
    private String path;

    /**
     * 是否是文件夹
     */
    private Boolean isDir;

    /**
     * 文件扩展名（如 .html, .css, .js）
     */
    private String ext;

    /**
     * 子文件/文件夹列表（仅文件夹有）
     */
    private List<SourceCodeFileDTO> children;

    /**
     * 文件大小（字节）
     */
    private Long size;
}
