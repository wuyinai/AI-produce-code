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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_version")
public class AppVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    @Column("app_id")
    private Long appId;

    @Column("version_number")
    private Integer versionNumber;

    @Column("version_name")
    private String versionName;

    @Column("version_description")
    private String versionDescription;

    @Column("code_snapshot")
    private String codeSnapshot;

    @Column("base_version_id")
    private Long baseVersionId;

    @Column("incremental_type")
    private String incrementalType;

    @Column("incremental_data")
    private String incrementalData;

    @Column("file_hashes")
    private String fileHashes;

    @Column("storage_size")
    private Long storageSize;

    @Column("compression_ratio")
    private java.math.BigDecimal compressionRatio;

    @Column("trigger_type")
    private String triggerType;

    @Column("trigger_message")
    private String triggerMessage;

    @Column("is_current")
    private Integer isCurrent;

    @Column("create_time")
    private LocalDateTime createTime;

    @Column("create_user_id")
    private Long createUserId;

    @Column("update_time")
    private LocalDateTime updateTime;

    @Column(value = "is_delete", isLogicDelete = true)
    private Integer isDelete;
}
