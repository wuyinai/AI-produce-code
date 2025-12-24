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
 * 协作记录 实体类。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("collaboration_record")
public class CollaborationRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 协作记录ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 关联应用ID
     */
    @Column("appId")
    private Long appId;

    /**
     * 创建者ID
     */
    @Column("creatorId")
    private Long creatorId;

    /**
     * 协作状态：valid/invalid
     */
    private String status;

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