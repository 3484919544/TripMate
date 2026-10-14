-- ============================================================
-- 智游 TripMate 建表脚本
-- 库 TripMate 由用户预先创建,此处只建表
-- 执行: mysql -u 你的数据库账号 -p TripMate < 01_schema.sql
-- ============================================================

USE `TripMate`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------- 用户表 ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account`     VARCHAR(10)           COMMENT '10位数字账号(系统自动生成)',
  `phone`       VARCHAR(11)           COMMENT '手机号',
  `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
  `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
  `nickname`    VARCHAR(50)           COMMENT '昵称',
  `avatar`      VARCHAR(255)          COMMENT '头像URL',
  `bio`         VARCHAR(200)          COMMENT '个性签名',
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色:USER/ADMIN',
  `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_account` (`account`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------- 行程表 ----------------------------
DROP TABLE IF EXISTS `trip`;
CREATE TABLE `trip` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT         NOT NULL COMMENT '用户 ID',
  `title`       VARCHAR(100)            COMMENT '行程标题',
  `origin`      VARCHAR(50)             COMMENT '出发地',
  `destination` VARCHAR(100)   NOT NULL COMMENT '目的地',
  `days`        INT            NOT NULL COMMENT '天数',
  `budget`      DECIMAL(10, 2)          COMMENT '预算',
  `preference`  VARCHAR(200)            COMMENT '偏好(逗号分隔)',
  `total_cost`  DECIMAL(10, 2)          COMMENT '预计总花费',
  `over_budget` TINYINT(1)     DEFAULT 0 COMMENT '是否超预算',
  `status`      VARCHAR(20)    DEFAULT 'GENERATED' COMMENT '状态',
  `create_time` DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_trip_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '行程表';

-- ---------------------------- 每日安排表 ----------------------------
DROP TABLE IF EXISTS `trip_day`;
CREATE TABLE `trip_day` (
  `id`         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trip_id`    BIGINT         NOT NULL COMMENT '行程 ID',
  `day_index`  INT            NOT NULL COMMENT '第几天',
  `theme`      VARCHAR(100)            COMMENT '主题',
  `morning`    TEXT                   COMMENT '上午安排',
  `afternoon`  TEXT                   COMMENT '下午安排',
  `evening`    TEXT                   COMMENT '晚上安排',
  `cost`       DECIMAL(10, 2)          COMMENT '当日花费',
  `tips`       TEXT                   COMMENT '提示',
  PRIMARY KEY (`id`),
  KEY `idx_trip_day_trip` (`trip_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '每日安排表';

-- ---------------------------- AI 调用日志表 ----------------------------
DROP TABLE IF EXISTS `ai_log`;
CREATE TABLE `ai_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT                COMMENT '用户 ID',
  `prompt`        TEXT                  COMMENT '提示词',
  `model`         VARCHAR(50)           COMMENT '模型',
  `request_time`  DATETIME              COMMENT '请求时间',
  `response_time` DATETIME              COMMENT '响应时间',
  `status`        VARCHAR(20)           COMMENT 'SUCCESS/FAIL',
  `error_msg`     TEXT                  COMMENT '错误信息',
  `tokens`        INT                   DEFAULT 0 COMMENT 'token 数',
  PRIMARY KEY (`id`),
  KEY `idx_ai_log_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 调用日志表';

-- ---------------------------- 反馈表 ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT                COMMENT '用户 ID',
  `trip_id`     BIGINT                COMMENT '行程 ID(可选)',
  `content`     TEXT                  COMMENT '反馈内容',
  `status`      VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态:PENDING 待处理/RESOLVED 已处理',
  `reply`       TEXT                  COMMENT '管理员回复',
  `reply_time`  DATETIME              COMMENT '回复时间',
  `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_feedback_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '反馈表';

SET FOREIGN_KEY_CHECKS = 1;
