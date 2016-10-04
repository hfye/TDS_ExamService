/***********************************************************************************************************************
  File: V1475261335__exam_add_subject_scored_column.sql

  Desc: Adds a date_scored and subject field for exam

***********************************************************************************************************************/
USE exam;

ALTER TABLE exam
  ADD COLUMN date_scored TIMESTAMP(3) DEFAULT NULL,
  ADD COLUMN subject VARCHAR(20) NOT NULL;