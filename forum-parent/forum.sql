-- 用户表
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `username` varchar(50) NOT NULL UNIQUE COMMENT '用户名',
                        `password` varchar(255) NOT NULL COMMENT '密码',
                        `email` varchar(100) NOT NULL UNIQUE COMMENT '邮箱',
                        `phone` varchar(20) COMMENT '手机号',
                        `nickname` varchar(50) COMMENT '昵称',
                        `avatar` varchar(255) COMMENT '头像URL',
                        `gender` tinyint DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
                        `birthday` date COMMENT '生日',
                        `signature` varchar(200) COMMENT '个人签名',
                        `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
                        `last_login_time` datetime COMMENT '最后登录时间',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `deleted` tinyint DEFAULT 0 COMMENT '删除标志',
                        PRIMARY KEY (`id`),
                        KEY `idx_username` (`username`),
                        KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE `role` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `role_name` varchar(50) NOT NULL COMMENT '角色名称',
                        `role_code` varchar(50) NOT NULL COMMENT '角色编码',
                        `description` varchar(200) COMMENT '角色描述',
                        `status` tinyint DEFAULT 1 COMMENT '状态',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `deleted` tinyint DEFAULT 0,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE `user_role` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `user_id` bigint NOT NULL,
                             `role_id` bigint NOT NULL,
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             KEY `idx_user_id` (`user_id`),
                             KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 版块表
CREATE TABLE `forum_category` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `parent_id` bigint DEFAULT 0 COMMENT '父级ID',
                                  `category_name` varchar(100) NOT NULL COMMENT '版块名称',
                                  `description` text COMMENT '版块描述',
                                  `icon` varchar(255) COMMENT '版块图标',
                                  `sort_order` int DEFAULT 0 COMMENT '排序',
                                  `post_count` int DEFAULT 0 COMMENT '帖子数量',
                                  `topic_count` int DEFAULT 0 COMMENT '主题数量',
                                  `status` tinyint DEFAULT 1 COMMENT '状态',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  `deleted` tinyint DEFAULT 0,
                                  PRIMARY KEY (`id`),
                                  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版块表';

-- 帖子表
CREATE TABLE `post` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `category_id` bigint NOT NULL COMMENT '版块ID',
                        `user_id` bigint NOT NULL COMMENT '用户ID',
                        `title` varchar(200) NOT NULL COMMENT '标题',
                        `content` longtext NOT NULL COMMENT '内容',
                        `post_type` tinyint DEFAULT 1 COMMENT '帖子类型：1-普通，2-精华，3-置顶',
                        `status` tinyint DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已删除',
                        `view_count` int DEFAULT 0 COMMENT '浏览数',
                        `like_count` int DEFAULT 0 COMMENT '点赞数',
                        `comment_count` int DEFAULT 0 COMMENT '评论数',
                        `collect_count` int DEFAULT 0 COMMENT '收藏数',
                        `is_anonymous` tinyint DEFAULT 0 COMMENT '是否匿名',
                        `tags` varchar(500) COMMENT '标签',
                        `images` text COMMENT '图片URLs',
                        `last_comment_time` datetime COMMENT '最后评论时间',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `deleted` tinyint DEFAULT 0,
                        PRIMARY KEY (`id`),
                        KEY `idx_category_id` (`category_id`),
                        KEY `idx_user_id` (`user_id`),
                        KEY `idx_create_time` (`create_time`),
                        KEY `idx_last_comment_time` (`last_comment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 评论表
CREATE TABLE `comment` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `post_id` bigint NOT NULL COMMENT '帖子ID',
                           `parent_id` bigint DEFAULT 0 COMMENT '父评论ID',
                           `user_id` bigint NOT NULL COMMENT '用户ID',
                           `content` text NOT NULL COMMENT '评论内容',
                           `like_count` int DEFAULT 0 COMMENT '点赞数',
                           `reply_count` int DEFAULT 0 COMMENT '回复数',
                           `status` tinyint DEFAULT 1 COMMENT '状态',
                           `is_anonymous` tinyint DEFAULT 0 COMMENT '是否匿名',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           `deleted` tinyint DEFAULT 0,
                           PRIMARY KEY (`id`),
                           KEY `idx_post_id` (`post_id`),
                           KEY `idx_parent_id` (`parent_id`),
                           KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 点赞表
CREATE TABLE `like_record` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `user_id` bigint NOT NULL COMMENT '用户ID',
                               `target_id` bigint NOT NULL COMMENT '目标ID',
                               `target_type` tinyint NOT NULL COMMENT '目标类型：1-帖子，2-评论',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 收藏表
CREATE TABLE `collect_record` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `user_id` bigint NOT NULL COMMENT '用户ID',
                                  `post_id` bigint NOT NULL COMMENT '帖子ID',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏记录表';

-- 关注表
CREATE TABLE `follow` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `follower_id` bigint NOT NULL COMMENT '关注者ID',
                          `following_id` bigint NOT NULL COMMENT '被关注者ID',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 通知表
CREATE TABLE `notification` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL COMMENT '接收用户ID',
                                `sender_id` bigint COMMENT '发送用户ID',
                                `type` tinyint NOT NULL COMMENT '通知类型：1-点赞，2-评论，3-关注，4-系统',
                                `title` varchar(200) COMMENT '通知标题',
                                `content` text COMMENT '通知内容',
                                `target_id` bigint COMMENT '目标ID',
                                `is_read` tinyint DEFAULT 0 COMMENT '是否已读',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                `deleted` tinyint DEFAULT 0,
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 文件表
CREATE TABLE `file_info` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `file_name` varchar(255) NOT NULL COMMENT '文件名',
                             `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
                             `file_path` varchar(500) NOT NULL COMMENT '文件路径',
                             `file_size` bigint NOT NULL COMMENT '文件大小',
                             `file_type` varchar(50) NOT NULL COMMENT '文件类型',
                             `md5` varchar(32) COMMENT 'MD5值',
                             `upload_user_id` bigint COMMENT '上传用户ID',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `deleted` tinyint DEFAULT 0,
                             PRIMARY KEY (`id`),
                             KEY `idx_md5` (`md5`),
                             KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';