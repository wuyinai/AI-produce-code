# 数据库初始化

-- 创建库
create database if not exists wuyinai_nocode;

-- 切换库
use wu_ai_pdce_code;

-- 用户表
-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    onlineStatus varchar(20)  default 'offline'         not null comment '在线状态：online/offline',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;
-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

-- 对话历史表
create table chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    messageType varchar(32)                        not null comment 'user/ai',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),                       -- 提升基于应用的查询性能
    INDEX idx_createTime (createTime),             -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (appId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;

-- 协作记录表
create table collaboration_record
(
    id           bigint auto_increment comment '协作记录ID' primary key,
    appId        bigint                             not null comment '关联应用ID',
    creatorId    bigint                             not null comment '创建者ID',
    status       varchar(20)  default 'valid'       not null comment '协作状态：valid/invalid',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0             not null comment '是否删除',
    FOREIGN KEY (appId) REFERENCES app(id),
    FOREIGN KEY (creatorId) REFERENCES user(id),
    INDEX idx_appId (appId),
    INDEX idx_creatorId (creatorId)
) comment '协作记录' collate = utf8mb4_unicode_ci;

-- 协作成员表
create table collaboration_member
(
    id               bigint auto_increment comment '协作成员ID' primary key,
    collaborationId  bigint                             not null comment '关联协作记录ID',
    userId           bigint                             not null comment '用户ID',
    joinTime         datetime     default CURRENT_TIMESTAMP not null comment '加入时间',
    createTime       datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint      default 0             not null comment '是否删除',
    FOREIGN KEY (collaborationId) REFERENCES collaboration_record(id),
    FOREIGN KEY (userId) REFERENCES user(id),
    UNIQUE KEY uk_collaboration_user (collaborationId, userId),
    INDEX idx_collaborationId (collaborationId),
    INDEX idx_userId (userId)
) comment '协作成员' collate = utf8mb4_unicode_ci;

-- 全栈AI智能的数据库设计表
-- 数据库设计项目表
CREATE TABLE IF NOT EXISTS `db_design_project` (
                                                   `id` BIGINT NOT NULL COMMENT '项目ID（雪花算法）',
                                                   `project_name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `db_type` VARCHAR(20) NOT NULL COMMENT '数据库类型（mysql/postgresql/sqlserver/oracle）',
    `design_data` LONGTEXT NOT NULL COMMENT '设计数据JSON（包含表、字段、关系、位置）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
    `user_id` BIGINT NOT NULL COMMENT '创建用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0-否 1-是）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_db_type` (`db_type`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库设计项目表';