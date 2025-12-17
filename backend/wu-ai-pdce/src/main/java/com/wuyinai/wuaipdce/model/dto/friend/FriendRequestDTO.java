package com.wuyinai.wuaipdce.model.dto.friend;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发送好友请求DTO
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Data
public class FriendRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接收者ID
     */
    private Long toUserId;

    /**
     * 请求消息
     */
    private String message;

}