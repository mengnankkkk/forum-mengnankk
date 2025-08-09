CREATE DATABASE IF NOT EXISTS forum DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE forum;

-- 插入初始数据
INSERT INTO `role` (`role_name`, `role_code`, `description`) VALUES
                                                                 ('管理员', 'ADMIN', '系统管理员'),
                                                                 ('版主', 'MODERATOR', '版块管理员'),
                                                                 ('普通用户', 'USER', '普通用户');

INSERT INTO `forum_category` (`category_name`, `description`, `sort_order`) VALUES
                                                                                ('技术讨论', 'Java、Python、前端等技术交流', 1),
                                                                                ('生活随笔', '日常生活分享', 2),
                                                                                ('新手求助', '新手问题求助', 3),
                                                                                ('公告通知', '系统公告和通知', 4);