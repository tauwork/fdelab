CREATE DATABASE IF NOT EXISTS fdelab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fdelab;

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(64)  NOT NULL,
    role_code   VARCHAR(32)  NOT NULL DEFAULT 'EDITOR',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS site_article (
    id          BIGINT        NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    summary     VARCHAR(500)  NULL,
    content     TEXT          NULL,
    cover_url   VARCHAR(500)  NULL,
    published   TINYINT       NOT NULL DEFAULT 1 COMMENT '1发布 0草稿',
    deleted     TINYINT       NOT NULL DEFAULT 0,
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_user (id, username, password, nickname, role_code, status, deleted)
SELECT 1, 'admin', '$2b$10$RhJVzjyVrVk27co1ZT0S9u3mev/fFkodeb9fjg7sJSLs4dzqTGh/C', '管理员', 'ADMIN', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO sys_user (id, username, password, nickname, role_code, status, deleted)
SELECT 2, 'editor', '$2b$10$Nd876IBejcskGnfUuX9WCe2N4QCKcLTwwQ.36fZmgQ9wuWwOLComO', '编辑', 'EDITOR', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'editor');

INSERT INTO site_article (id, title, summary, content, published, deleted)
SELECT 101,
       '欢迎来到 FDELab',
       '官网、小程序和后台从这里开始共用同一套数据。',
       'FDELab 目前是业务骨架。展示站读取已发布的动态，管理后台负责登录和用户，小程序走同一组公开接口。后续业务表和接口按模块往上加即可。',
       1,
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM site_article WHERE id = 101);

INSERT INTO site_article (id, title, summary, content, published, deleted)
SELECT 102,
       '三端怎么分工',
       '小程序面对用户，官网负责展示，后台负责管理。',
       'fdelab-miniapp 是微信小程序，fdelab-site-web 是展示网站，fdelab-admin-web 是管理后台。它们都访问 fdelab-server。需要登录的接口走 Sa-Token，公开内容放在 /api/public 下。',
       1,
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM site_article WHERE id = 102);
