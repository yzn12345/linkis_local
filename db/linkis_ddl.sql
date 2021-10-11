SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `linkis_ps_configuration_config_key`;
CREATE TABLE `linkis_ps_configuration_config_key`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(50) DEFAULT NULL COMMENT 'Set key, e.g. spark.executor.instances',
  `description` varchar(200) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `default_value` varchar(200) DEFAULT NULL COMMENT 'Adopted when user does not set key',
  `validate_type` varchar(50) DEFAULT NULL COMMENT 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules',
  `validate_range` varchar(50) DEFAULT NULL COMMENT 'Validate range',
  `engine_conn_type` varchar(50) DEFAULT NULL COMMENT 'engine type,such as spark,hive etc',
  `is_hidden` tinyint(1) DEFAULT NULL COMMENT 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end',
  `is_advanced` tinyint(1) DEFAULT NULL COMMENT 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so',
  `level` tinyint(1) DEFAULT NULL COMMENT 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets',
  `treeName` varchar(20) DEFAULT NULL COMMENT 'Reserved field, representing the subdirectory of engineType',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_configuration_key_engine_relation`;
CREATE TABLE `linkis_ps_configuration_key_engine_relation`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key_id` bigint(20) NOT NULL COMMENT 'config key id',
  `engine_type_label_id` bigint(20) NOT NULL COMMENT 'engine label id',
  PRIMARY KEY (`id`),
  UNIQUE INDEX(`config_key_id`, `engine_type_label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_configuration_config_value`;
CREATE TABLE linkis_ps_configuration_config_value(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key_id` bigint(20),
  `config_value` varchar(200),
  `config_label_id`int(20),
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX(`config_key_id`, `config_label_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_ps_configuration_category`;
CREATE TABLE `linkis_ps_configuration_category` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) NOT NULL,
  `level` int(20) NOT NULL,
  `description` varchar(200),
  `tag` varchar(200),
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX(`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- New linkis job
--

DROP  TABLE IF EXISTS `linkis_ps_job_history_group_history`;
CREATE TABLE `linkis_ps_job_history_group_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key, auto increment',
  `job_req_id` varchar(64) DEFAULT NULL COMMENT 'job execId',
  `submit_user` varchar(50) DEFAULT NULL COMMENT 'who submitted this Job',
  `execute_user` varchar(50) DEFAULT NULL COMMENT 'who actually executed this Job',
  `source` text DEFAULT NULL COMMENT 'job source',
  `labels` text DEFAULT NULL COMMENT 'job labels',
  `params` text DEFAULT NULL COMMENT 'job labels',
  `progress` varchar(32) DEFAULT NULL COMMENT 'Job execution progress',
  `status` varchar(50) DEFAULT NULL COMMENT 'Script execution status, must be one of the following: Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout',
  `log_path` varchar(200) DEFAULT NULL COMMENT 'File path of the job log',
  `error_code` int DEFAULT NULL COMMENT 'Error code. Generated when the execution of the script fails',
  `error_desc` varchar(1000) DEFAULT NULL COMMENT 'Execution description. Generated when the execution of script fails',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Update time',
  `instances` varchar(250) DEFAULT NULL COMMENT 'Entrance instances',
  `metrics` text DEFAULT NULL COMMENT   'Job Metrics',
  `engine_type` varchar(32) DEFAULT NULL COMMENT 'Engine type',
  `execution_code` text DEFAULT NULL COMMENT 'Job origin code or code path',
  PRIMARY KEY (`id`),
  KEY `created_time` (`created_time`),
  KEY `submit_user` (`submit_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP  TABLE IF EXISTS `linkis_ps_job_history_detail`;
CREATE TABLE `linkis_ps_job_history_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key, auto increment',
  `job_history_id` bigint(20) NOT NULL COMMENT 'ID of JobHistory',
  `result_location` varchar(500) DEFAULT NULL COMMENT 'File path of the resultsets',
  `execution_content` text DEFAULT NULL COMMENT 'The script code or other execution content executed by this Job',
  `result_array_size` int(4) DEFAULT 0 COMMENT 'size of result array',
  `job_group_info` text DEFAULT NULL COMMENT 'Job group info/path',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Update time',
  `status` varchar(32) DEFAULT NULL COMMENT 'status',
  `priority` int(4) DEFAULT 0 COMMENT 'order of subjob',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for linkis_ps_udf_manager
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_manager`;
CREATE TABLE `linkis_ps_udf_manager` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_ps_udf_shared_group
-- An entry would be added when a user share a function to other user group
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_shared_group`;
CREATE TABLE `linkis_ps_udf_shared_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `udf_id` bigint(20) NOT NULL,
  `shared_group` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `linkis_ps_udf_shared_user`;
CREATE TABLE `linkis_ps_udf_shared_user`
(
   `id` bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
   `udf_id` bigint(20) NOT NULL,
   `user_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for linkis_ps_udf_shared_group
-- An entry would be added when a user share a function to another user
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_shared_group`;
CREATE TABLE `linkis_ps_udf_shared_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `udf_id` bigint(20) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_ps_udf_tree
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_tree`;
CREATE TABLE `linkis_ps_udf_tree` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent` bigint(20) NOT NULL,
  `name` varchar(100) DEFAULT NULL COMMENT 'Category name of the function. It would be displayed in the front-end',
  `user_name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `category` varchar(50) DEFAULT NULL COMMENT 'Used to distinguish between udf and function',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_ps_udf_user_load_info
-- Used to store the function a user selects in the front-end
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_user_load_info`;
CREATE TABLE `linkis_ps_udf_user_load_info` (
  `udf_id` int(11) NOT NULL,
  `user_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_ps_udf
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf`;
CREATE TABLE `linkis_ps_udf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_user` varchar(50) NOT NULL,
  `udf_name` varchar(255) NOT NULL,
  `udf_type` int(11) DEFAULT '0',
  `path` varchar(255) DEFAULT NULL COMMENT 'Path of the referenced function',
  `register_format` varchar(255) DEFAULT NULL,
  `use_format` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_expire` bit(1) DEFAULT NULL,
  `is_shared` bit(1) DEFAULT NULL,
  `tree_id` bigint(20) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for linkis_ps_variable_key_user
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_variable_key_user`;
CREATE TABLE `linkis_ps_variable_key_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) DEFAULT NULL COMMENT 'Reserved word',
  `key_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL COMMENT 'Value of the global variable',
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_id_2` (`application_id`,`key_id`,`user_name`),
  KEY `key_id` (`key_id`),
  KEY `application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for linkis_ps_variable_key
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_variable_key`;
CREATE TABLE `linkis_ps_variable_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(50) DEFAULT NULL COMMENT 'Key of the global variable',
  `description` varchar(200) DEFAULT NULL COMMENT 'Reserved word',
  `name` varchar(50) DEFAULT NULL COMMENT 'Reserved word',
  `application_id` bigint(20) DEFAULT NULL COMMENT 'Reserved word',
  `default_value` varchar(200) DEFAULT NULL COMMENT 'Reserved word',
  `value_type` varchar(50) DEFAULT NULL COMMENT 'Reserved word',
  `value_regex` varchar(100) DEFAULT NULL COMMENT 'Reserved word',
  PRIMARY KEY (`id`),
  KEY `application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_datasource_access
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_access`;
CREATE TABLE `linkis_ps_datasource_access` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL,
  `visitor` varchar(16) COLLATE utf8_bin NOT NULL,
  `fields` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `application_id` int(4) NOT NULL,
  `access_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_field
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_field`;
CREATE TABLE `linkis_ps_datasource_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL,
  `name` varchar(64) COLLATE utf8_bin NOT NULL,
  `alias` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `type` varchar(64) COLLATE utf8_bin NOT NULL,
  `comment` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `express` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `rule` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `is_partition_field` tinyint(1) NOT NULL,
  `is_primary` tinyint(1) NOT NULL,
  `length` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_import
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_import`;
CREATE TABLE `linkis_ps_datasource_import` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL,
  `import_type` int(4) NOT NULL,
  `args` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_lineage
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_lineage`;
CREATE TABLE `linkis_ps_datasource_lineage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) DEFAULT NULL,
  `source_table` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_table
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_table`;
CREATE TABLE `linkis_ps_datasource_table` (
  `id` bigint(255) NOT NULL AUTO_INCREMENT,
  `database` varchar(64) COLLATE utf8_bin NOT NULL,
  `name` varchar(64) COLLATE utf8_bin NOT NULL,
  `alias` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(16) COLLATE utf8_bin NOT NULL,
  `comment` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `product_name` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `project_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `usage` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `lifecycle` int(4) NOT NULL,
  `use_way` int(4) NOT NULL,
  `is_import` tinyint(1) NOT NULL,
  `model_level` int(4) NOT NULL,
  `is_external_use` tinyint(1) NOT NULL,
  `is_partition_table` tinyint(1) NOT NULL,
  `is_available` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `database` (`database`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_table_info
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_table_info`;
CREATE TABLE `linkis_ps_datasource_table_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL,
  `table_last_update_time` datetime NOT NULL,
  `row_num` bigint(20) NOT NULL,
  `file_num` int(11) NOT NULL,
  `table_size` varchar(32) COLLATE utf8_bin NOT NULL,
  `partitions_num` int(11) NOT NULL,
  `update_time` datetime NOT NULL,
  `field_num` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;




-- ----------------------------
-- Table structure for linkis_ps_cs_context_map
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_map`;
CREATE TABLE `linkis_ps_cs_context_map` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(128) DEFAULT NULL,
  `context_scope` varchar(32) DEFAULT NULL,
  `context_type` varchar(32) DEFAULT NULL,
  `props` text,
  `value` mediumtext,
  `context_id` int(11) DEFAULT NULL,
  `keywords` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key` (`key`,`context_id`,`context_type`),
  KEY `keywords` (`keywords`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_cs_context_map_listener
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_map_listener`;
CREATE TABLE `linkis_ps_cs_context_map_listener` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `listener_source` varchar(255) DEFAULT NULL,
  `key_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_cs_context_history
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_history`;
CREATE TABLE `linkis_ps_cs_context_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `context_id` int(11) DEFAULT NULL,
  `source` text,
  `context_type` varchar(32) DEFAULT NULL,
  `history_json` text,
  `keyword` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `keyword` (`keyword`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_cs_context_id
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_id`;
CREATE TABLE `linkis_ps_cs_context_id` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(32) DEFAULT NULL,
  `application` varchar(32) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `expire_type` varchar(32) DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `instance` varchar(128) DEFAULT NULL,
  `backup_instance` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `instance` (`instance`(128)),
  KEY `backup_instance` (`backup_instance`(191)),
  KEY `instance_2` (`instance`(128),`backup_instance`(128))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_cs_context_listener
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_listener`;
CREATE TABLE `linkis_ps_cs_context_listener` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `listener_source` varchar(255) DEFAULT NULL,
  `context_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


drop table if exists `linkis_ps_bml_resources`;
CREATE TABLE if not exists `linkis_ps_bml_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'resource uuid',
  `is_private` TINYINT(1) DEFAULT 0 COMMENT 'Whether the resource is private, 0 means private, 1 means public',
  `resource_header` TINYINT(1) DEFAULT 0 COMMENT 'Classification, 0 means unclassified, 1 means classified',
	`downloaded_file_name` varchar(200) DEFAULT NULL COMMENT 'File name when downloading',
	`sys` varchar(100) NOT NULL COMMENT 'Owning system',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
	`owner` varchar(200) NOT NULL COMMENT 'Resource owner',
	`is_expire` TINYINT(1) DEFAULT 0 COMMENT 'Whether expired, 0 means not expired, 1 means expired',
	`expire_type` varchar(50) DEFAULT null COMMENT 'Expiration type, date refers to the expiration on the specified date, TIME refers to the time',
	`expire_time` varchar(50) DEFAULT null COMMENT 'Expiration time, one day by default',
	`max_version` int(20) DEFAULT 10 COMMENT 'The default is 10, which means to keep the latest 10 versions',
	`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
	`updator` varchar(50) DEFAULT NULL COMMENT 'updator',
	`enable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Status, 1: normal, 0: frozen',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;


drop table if exists `linkis_ps_bml_resources_version`;
CREATE TABLE if not exists `linkis_ps_bml_resources_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'Resource uuid',
  `file_md5` varchar(32) NOT NULL COMMENT 'Md5 summary of the file',
  `version` varchar(20) NOT NULL COMMENT 'Resource version (v plus five digits)',
	`size` int(10) NOT NULL COMMENT 'File size',
	`start_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	`end_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	`resource` varchar(2000) NOT NULL COMMENT 'Resource content (file information including path and file name)',
	`description` varchar(2000) DEFAULT NULL COMMENT 'description',
	`start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Started time',
	`end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Stoped time',
	`client_ip` varchar(200) NOT NULL COMMENT 'Client ip',
	`updator` varchar(50) DEFAULT NULL COMMENT 'updator',
	`enable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Status, 1: normal, 0: frozen',
	unique key `resource_id_version`(`resource_id`, `version`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



drop table if exists `linkis_ps_bml_resources_permission`;
CREATE TABLE if not exists `linkis_ps_bml_resources_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'Resource uuid',
  `permission` varchar(10) NOT NULL COMMENT 'permission',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
	`system` varchar(50) default "dss" COMMENT 'creator',
	`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'updated time',
	`updator` varchar(50) NOT NULL COMMENT 'updator',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



drop table if exists `linkis_ps_resources_download_history`;
CREATE TABLE if not exists `linkis_ps_resources_download_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
	`start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'start time',
	`end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'stop time',
	`client_ip` varchar(200) NOT NULL COMMENT 'client ip',
	`state` TINYINT(1) NOT NULL COMMENT 'Download status, 0 download successful, 1 download failed',
	 `resource_id` varchar(50) not null,
	 `version` varchar(20) not null,
	`downloader` varchar(50) NOT NULL COMMENT 'Downloader',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




-- 创建资源任务表,包括上传,更新,下载
drop table if exists `linkis_ps_bml_resources_task`;
CREATE TABLE if not exists `linkis_ps_bml_resources_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` varchar(50) DEFAULT NULL COMMENT 'resource uuid',
  `version` varchar(20) DEFAULT NULL COMMENT 'Resource version number of the current operation',
  `operation` varchar(20) NOT NULL COMMENT 'Operation type. upload = 0, update = 1',
  `state` varchar(20) NOT NULL DEFAULT 'Schduled' COMMENT 'Current status of the task:Schduled, Running, Succeed, Failed,Cancelled',
  `submit_user` varchar(20) NOT NULL DEFAULT '' COMMENT 'Job submission user name',
  `system` varchar(20) DEFAULT 'dss' COMMENT 'Subsystem name: wtss',
  `instance` varchar(128) NOT NULL COMMENT 'Material library example',
  `client_ip` varchar(50) DEFAULT NULL COMMENT 'Request IP',
  `extra_params` text COMMENT 'Additional key information. Such as the resource IDs and versions that are deleted in batches, and all versions under the resource are deleted',
  `err_msg` varchar(2000) DEFAULT NULL COMMENT 'Task failure information.e.getMessage',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Starting time',
  `end_time` datetime DEFAULT NULL COMMENT 'End Time',
  `last_update_time` datetime NOT NULL COMMENT 'Last update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



drop table if exists linkis_ps_bml_project;
create table if not exists linkis_ps_bml_project(
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL,
  `system` varchar(64) not null default "dss",
  `source` varchar(1024) default null,
  `description` varchar(1024) default null,
  `creator` varchar(128) not null,
  `enabled` tinyint default 1,
  `create_time` datetime DEFAULT now(),
  unique key(`name`),
PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;



drop table if exists linkis_ps_bml_project_user;
create table if not exists linkis_ps_bml_project_user(
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `project_id` int(10) NOT NULL,
  `username` varchar(64) DEFAULT NULL,
  `priv` int(10) not null default 7,
  `creator` varchar(128) not null,
  `create_time` datetime DEFAULT now(),
  `expire_time` datetime default null,
  unique key user_project(`username`, `project_id`),
PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;


drop table if exists linkis_ps_bml_project_resource;
create table if not exists linkis_ps_bml_project_resource(
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `project_id` int(10) NOT NULL,
  `resource_id` varchar(128) DEFAULT NULL,
PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;


DROP TABLE IF EXISTS `linkis_ps_instance_label`;
CREATE TABLE `linkis_ps_instance_label` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_key` varchar(32) COLLATE utf8_bin NOT NULL COMMENT 'string key',
  `label_value` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'string value',
  `label_feature` varchar(16) COLLATE utf8_bin NOT NULL COMMENT 'store the feature of label, but it may be redundant',
  `label_value_size` int(20) NOT NULL COMMENT 'size of key -> value map',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `label_key_value` (`label_key`,`label_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_instance_label_value_relation`;
CREATE TABLE `linkis_ps_instance_label_value_relation` (
  `label_value_key` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'value key',
  `label_value_content` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'value content',
  `label_id` int(20) DEFAULT NULL COMMENT 'id reference linkis_ps_instance_label -> id',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create unix timestamp',
  UNIQUE KEY `label_value_key_label_id` (`label_value_key`,`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_ps_instance_label_relation`;
CREATE TABLE `linkis_ps_instance_label_relation` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL COMMENT 'id reference linkis_ps_instance_label -> id',
  `service_instance` varchar(64) NOT NULL COLLATE utf8_bin COMMENT 'structure like ${host|machine}:${port}',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create unix timestamp',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_instance_info`;
CREATE TABLE `linkis_ps_instance_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'structure like ${host|machine}:${port}',
  `name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'equal application name in registry',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create unix timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `instance` (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_ps_error_code`;
CREATE TABLE `linkis_ps_error_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `error_code` varchar(50) NOT NULL,
  `error_desc` varchar(1024) NOT NULL,
  `error_regex` varchar(1024) DEFAULT NULL,
  `error_type` int(3) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_service_instance`;

CREATE TABLE `linkis_cg_manager_service_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `owner` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `mark` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `instance` (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_linkis_resources`;

CREATE TABLE `linkis_cg_manager_linkis_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `max_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `min_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `used_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `left_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `expected_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `locked_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `resourceType` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketId` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_lock`;

CREATE TABLE `linkis_cg_manager_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time_out` longtext COLLATE utf8_bin,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `lock_object` (`lock_object`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_rm_external_resource_provider`;
CREATE TABLE `linkis_cg_rm_external_resource_provider` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `resource_type` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `labels` varchar(32) DEFAULT NULL,
  `config` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `linkis_cg_manager_engine_em`;
CREATE TABLE `linkis_cg_manager_engine_em` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `engine_instance` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `em_instance` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label`;

CREATE TABLE `linkis_cg_manager_label` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_key` varchar(32) COLLATE utf8_bin NOT NULL,
  `label_value` varchar(255) COLLATE utf8_bin NOT NULL,
  `label_feature` varchar(16) COLLATE utf8_bin NOT NULL,
  `label_value_size` int(20) NOT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `label_key_value` (`label_key`,`label_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label_value_relation`;

CREATE TABLE `linkis_cg_manager_label_value_relation` (
  `label_value_key` varchar(255) COLLATE utf8_bin NOT NULL,
  `label_value_content` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `label_value_key_label_id` (`label_value_key`,`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label_resource`;
CREATE TABLE `linkis_cg_manager_label_resource` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `resource_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_cg_manager_label_service_instance`;
CREATE TABLE `linkis_cg_manager_label_service_instance` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `service_instance` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_cg_manager_label_user`;
CREATE TABLE `linkis_cg_manager_label_user` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_cg_manager_metrics_history`;

CREATE TABLE `linkis_cg_manager_metrics_history` (
  `instance_status` int(20) DEFAULT NULL,
  `overload` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `heartbeat_msg` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `healthy_status` int(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketID` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `serviceName` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `instance` varchar(255) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_service_instance_metrics`;

CREATE TABLE `linkis_cg_manager_service_instance_metrics` (
  `instance` varchar(128) COLLATE utf8_bin NOT NULL,
  `instance_status` int(11) DEFAULT NULL,
  `overload` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `heartbeat_msg` text COLLATE utf8_bin DEFAULT NULL,
  `healthy_status` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_engine_conn_plugin_bml_resources`;
CREATE TABLE `linkis_cg_engine_conn_plugin_bml_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `engine_conn_type` varchar(100) NOT NULL COMMENT 'Engine type',
  `version` varchar(100) COMMENT 'version',
  `file_name` varchar(255) COMMENT 'file name',
  `file_size` bigint(20)  DEFAULT 0 NOT NULL COMMENT 'file size',
  `last_modified` bigint(20)  COMMENT 'File update time',
  `bml_resource_id` varchar(100) NOT NULL COMMENT 'Owning system',
  `bml_resource_version` varchar(200) NOT NULL COMMENT 'Resource owner',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'updated time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;




