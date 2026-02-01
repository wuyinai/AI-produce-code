package com.wuyinai.wuaipdce.model.dto.appversion;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AppVersionVO implements Serializable {

    private Long id;

    private Long appId;

    private Integer versionNumber;

    private String versionName;

    private String versionDescription;

    private String triggerType;

    private String triggerMessage;

    private Integer isCurrent;

    private LocalDateTime createTime;

    private Long createUserId;

    private String createUserName;
}
