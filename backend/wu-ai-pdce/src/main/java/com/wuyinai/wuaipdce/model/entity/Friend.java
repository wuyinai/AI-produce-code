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
 * 好友关系 实体类。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("friend")
public class Friend implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id，雪花算法
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户ID
     */
    @Column("user_id")
    private Long userId;

    /**
     * 好友ID
     */
    @Column("friend_id")
    private Long friendId;

    /**
     * 好友状态（friend/pending）
     */
    private String status;

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