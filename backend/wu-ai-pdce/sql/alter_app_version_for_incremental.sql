-- 修改 app_version 表，添加增量存储支持
USE wu_ai_pdce_code;

-- 添加增量存储相关字段
ALTER TABLE app_version
ADD COLUMN base_version_id BIGINT DEFAULT NULL COMMENT '基准版本ID（用于增量存储）' AFTER code_snapshot,
ADD COLUMN incremental_type VARCHAR(20) DEFAULT 'full' COMMENT '存储类型：full-完整快照，incremental-增量差异' AFTER base_version_id,
ADD COLUMN incremental_data LONGTEXT DEFAULT NULL COMMENT '增量数据（JSON格式，存储差异信息）' AFTER incremental_type,
ADD COLUMN file_hashes LONGTEXT DEFAULT NULL COMMENT '文件哈希值（JSON格式，用于快速判断文件变化）' AFTER incremental_data,
ADD COLUMN storage_size BIGINT DEFAULT 0 COMMENT '存储大小（字节）' AFTER file_hashes,
ADD COLUMN compression_ratio DECIMAL(5,2) DEFAULT 0.00 COMMENT '压缩率（相对于完整快照的压缩比例）' AFTER storage_size;

-- 添加索引
CREATE INDEX idx_base_version_id ON app_version(base_version_id);
CREATE INDEX idx_app_version_number ON app_version(app_id, version_number);

-- 添加注释
ALTER TABLE app_version
MODIFY COLUMN code_snapshot LONGTEXT COMMENT '代码快照（完整快照时使用，增量存储时为NULL）';
