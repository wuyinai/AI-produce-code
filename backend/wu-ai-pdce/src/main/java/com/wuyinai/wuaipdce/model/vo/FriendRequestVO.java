package com.wuyinai.wuaipdce.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友请求展示类
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long fromUserId;

    /**
     * 发送者昵称
     */
    private String fromUserName;

    /**
     * 发送者头像
     */
    private String fromUserAvatar;

    /**
     * 发送者账号
     */
    private String fromUserAccount;

    /**
     * 接收者ID
     */
    private Long toUserId;

    /**
     * 请求消息
     */
    private String message;

    /**
     * 请求状态（pending/accepted/rejected）
     */
    private String status;

    /**
     * 请求时间
     */
    private LocalDateTime createTime;

}