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
 * 协作成员 实体类。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("collaboration_member")
public class CollaborationMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 协作成员ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 关联协作记录ID
     */
    @Column("collaborationId")
    private Long collaborationId;

    /**
     * 用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 加入时间
     */
    @Column("joinTime")
    private LocalDateTime joinTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}