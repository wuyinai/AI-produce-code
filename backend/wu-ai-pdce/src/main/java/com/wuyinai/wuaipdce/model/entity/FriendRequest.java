package com.wuyinai.wuaipdce.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友请求 实体类。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("friend_request")
public class FriendRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id，雪花算法
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 请求发送者ID
     */
    @Column("from_user_id")
    private Long fromUserId;

    /**
     * 请求接收者ID
     */
    @Column("to_user_id")
    private Long toUserId;

    /**
     * 请求状态（pending/accepted/rejected）
     */
    private String status;

    /**
     * 请求消息
     */
    private String message;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column("is_delete")
    private Integer isDelete;

}