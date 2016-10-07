/***********************************************************************************************************************
  File: retrieveExamBefore.sql

  Desc: Creates a test record in the database for integration test

***********************************************************************************************************************/
USE exam;

INSERT into exam (exam_id, session_id, assessment_id, student_id, attempts, client_name, status, subject) values (X'af880054d1d24c24805c0dfdb45a0d24', X'244363EED4D34C02AFAE52FFE1AEAC33', 'assessmentId', 1, 0, 'clientName', 'pending', 'ELA');
INSERT into exam (exam_id, session_id, assessment_id, student_id, attempts, client_name, date_deleted, status, subject) values (X'ab880054d1d24c24805c0dfdb45a0d24', X'244363EED4D34C02AFAE52FFE1AEAC33', 'assessmentId2', 1, 0, 'clientName', current_timestamp, 'pending', 'ELA');
INSERT into exam (exam_id, session_id, assessment_id, student_id, attempts, client_name, date_scored, status, subject) values (X'af880054d1d24c24805c1f0dfdb45980', X'244363EED4D34C02AFAE52FFE1AEAC33', 'assessmentId3', 9999, 0, 'clientName', now(), 'pending', 'ELA');

INSERT into scores (fk_scores_examid_exam, measure_label, value, standard_error, measure_of, is_official, date, subject, use_for_ability, hostname) values (X'af880054d1d24c24805c1f0dfdb45980', 'Measure-Label', '50', 5, 'measure-of', 1, now(), 'ELA', 1, 'host');