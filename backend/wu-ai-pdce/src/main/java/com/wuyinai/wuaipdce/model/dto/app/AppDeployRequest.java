package com.wuyinai.wuaipdce.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {
    /**
     * 应用id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}
