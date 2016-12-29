/***********************************************************************************************************************
  File: V1480723427__exam_create_table_field_test_item_group.sql

  Desc: Creates the field_test_item_group table

***********************************************************************************************************************/

USE exam;

DROP TABLE  IF EXISTS field_test_item_group_event;
DROP TABLE IF EXISTS field_test_item_group;

CREATE TABLE field_test_item_group (
  id BIGINT NOT NULL AUTO_INCREMENT,
  field_test_key VARCHAR(200) NOT NULL,
  exam_id VARBINARY(16) NOT NULL,
  `position` INT(11) NOT NULL,
  num_items INT(11) DEFAULT NULL,
  assessment_key VARCHAR(250) DEFAULT NULL,
  group_id VARCHAR(50) NOT NULL,
  group_key VARCHAR(60) NOT NULL,
  block_id VARCHAR(10) NOT NULL,
  segment_id VARCHAR(100) DEFAULT NULL,
  segment_position INT(11) NOT NULL,
  session_id VARBINARY(16) DEFAULT NULL,
  language VARCHAR(50) DEFAULT NULL,
  interval_size INT(11) DEFAULT NULL,
  interval_start INT(11) DEFAULT NULL,
  num_intervals INT(11) DEFAULT NULL,
  date_assigned DATETIME(3) DEFAULT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  INDEX ix_created_at (created_at),
  PRIMARY KEY (id),
  KEY ix_ftitem_cluster (exam_id, group_id),
  KEY ix_ftexamitemgroup_pk (field_test_key, segment_position, language, group_key),
  CONSTRAINT fk_field_test_item_group_exam_id_exam FOREIGN KEY (exam_id) REFERENCES exam (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE field_test_item_group_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  field_test_item_group_id BIGINT(20) NOT NULL,
  deleted BIT(1) DEFAULT NULL,
  position_administered INT(11) DEFAULT NULL,
  date_administered DATETIME(3) DEFAULT NULL,
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  PRIMARY KEY(id),
  INDEX ix_created_at (created_at),
  FOREIGN KEY (field_test_item_group_id) REFERENCES field_test_item_group(id)
);