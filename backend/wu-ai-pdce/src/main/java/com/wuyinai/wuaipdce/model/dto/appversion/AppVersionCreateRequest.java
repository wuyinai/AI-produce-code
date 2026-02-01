package com.wuyinai.wuaipdce.model.dto.appversion;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppVersionCreateRequest implements Serializable {

    private Long appId;

    private String triggerType;

    private String triggerMessage;

    private String versionDescription;
}
