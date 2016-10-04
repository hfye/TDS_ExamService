/***********************************************************************************************************************
  File: V1475261281__exam_create_table_scores.sql

  Desc: Creates the scores database table

***********************************************************************************************************************/
USE exam;

CREATE TABLE IF NOT EXISTS scores(
  fk_scores_examid_exam VARBINARY(16) NOT NULL,
  measure_label VARCHAR(100) NOT NULL,
  value VARCHAR(100) DEFAULT NULL,
  standard_error FLOAT DEFAULT NULL,
  measure_of VARCHAR(150) NOT NULL,
  is_official BIT(1) NOT NULL DEFAULT b'1',
  date DATETIME(3) NOT NULL,
  subject VARCHAR(100) DEFAULT NULL,
  use_for_ability BIT(1) NOT NULL DEFAULT b'0',
  hostname VARCHAR(25) DEFAULT NULL,
  PRIMARY KEY (fk_scores_examid_exam, measure_of, measure_label),
  CONSTRAINT fk_examscores FOREIGN KEY (fk_scores_examid_exam) REFERENCES exam (id) ON DELETE CASCADE ON UPDATE NO ACTION
);
