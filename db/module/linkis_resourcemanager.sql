DROP TABLE IF EXISTS `linkis_em_resource_meta_data`;
CREATE TABLE `linkis_em_resource_meta_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `em_application_name` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `em_instance` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `total_resource` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  `protected_resource` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  `resource_policy` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `used_resource` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  `left_resource` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  `locked_resource` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  `register_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_resource_lock`;
CREATE TABLE `linkis_resource_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `em_application_name` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `em_instance` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `lock_unique` (`user`,`em_application_name`,`em_instance`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_user_resource_meta_data`;
CREATE TABLE `linkis_user_resource_meta_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `ticket_id` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `em_application_name` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `em_instance` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `engine_application_name` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `engine_instance` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `user_locked_resource` varchar(5000) COLLATE utf8_bin DEFAULT NULL,
  `user_used_resource` varchar(5000) COLLATE utf8_bin DEFAULT NULL,
  `resource_type` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `locked_time` bigint(20) DEFAULT NULL,
  `used_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_em_meta_data`;
CREATE TABLE `linkis_em_meta_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `em_name` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `resource_request_policy` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;