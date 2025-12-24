package com.wuyinai.wuaipdce.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协作成员VO
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Data
public class CollaborationMemberVO {
    /**
     * 协作成员ID
     */
    private Long id;

    /**
     * 协作记录ID
     */
    private Long collaborationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名/昵称
     */
    private String userName;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
