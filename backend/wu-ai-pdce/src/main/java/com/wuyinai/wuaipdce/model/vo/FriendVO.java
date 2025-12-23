package com.wuyinai.wuaipdce.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友信息展示类
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 好友ID
     */
    private Long id;

    /**
     * 好友昵称
     */
    private String userName;

    /**
     * 好友头像
     */
    private String userAvatar;

    /**
     * 好友账号
     */
    private String userAccount;

    /**
     * 好友简介
     */
    private String userProfile;

    /**
     * 好友状态
     */
    private String status;

    /**
     * 成为好友时间
     */
    private LocalDateTime createTime;

    /**
     * 在线状态：online/offline
     */
    private String onlineStatus;

}