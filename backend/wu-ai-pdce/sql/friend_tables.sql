-- 创建好友关系表
CREATE TABLE `friend` (
  `id` BIGINT NOT NULL COMMENT '主键，雪花算法',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `friend_id` BIGINT NOT NULL COMMENT '好友ID',
  `status` VARCHAR(20) NOT NULL COMMENT '好友状态（friend/pending）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_friend` (`user_id`, `friend_id`),
  KEY `idx_friend_user` (`friend_id`, `user_id`),
  CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- 创建好友请求表
CREATE TABLE `friend_request` (
  `id` BIGINT NOT NULL COMMENT '主键，雪花算法',
  `from_user_id` BIGINT NOT NULL COMMENT '请求发送者ID',
  `to_user_id` BIGINT NOT NULL COMMENT '请求接收者ID',
  `status` VARCHAR(20) NOT NULL COMMENT '请求状态（pending/accepted/rejected）',
  `message` VARCHAR(200) DEFAULT NULL COMMENT '请求消息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_to_user_status` (`to_user_id`, `status`),
  KEY `idx_from_to` (`from_user_id`, `to_user_id`),
  CONSTRAINT `fk_friend_request_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_friend_request_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友请求表';
