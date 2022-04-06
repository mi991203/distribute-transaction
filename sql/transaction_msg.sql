/*
 Navicat Premium Data Transfer

 Source Server         : deliver
 Source Server Type    : MySQL
 Source Server Version : 50173
 Source Host           : 192.168.137.40:3306
 Source Schema         : deliver

 Target Server Type    : MySQL
 Target Server Version : 50173
 File Encoding         : 65001

 Date: 06/04/2022 11:20:16
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for transaction_msg
-- ----------------------------
DROP TABLE IF EXISTS `transaction_msg`;
CREATE TABLE `transaction_msg`
(
    `id`          varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci       NOT NULL,
    `service`     varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci   NULL DEFAULT NULL,
    `exchange`    varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci   NULL DEFAULT NULL,
    `type`        varchar(36) CHARACTER SET latin1 COLLATE latin1_swedish_ci   NULL DEFAULT NULL,
    `routing_key` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci   NULL DEFAULT NULL,
    `queue`       varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci   NULL DEFAULT NULL,
    `sequence`    int(11)                                                      NULL DEFAULT NULL,
    `payload`     varchar(1000) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
    `date`        date                                                         NULL DEFAULT NULL,
    UNIQUE INDEX `UNIQUE_ID_SERVICE` (`id`, `service`) USING BTREE
) ENGINE = MyISAM
  CHARACTER SET = latin1
  COLLATE = latin1_swedish_ci
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
