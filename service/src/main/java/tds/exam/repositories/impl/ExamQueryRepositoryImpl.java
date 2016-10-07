package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;

@Repository
public class ExamQueryRepositoryImpl implements ExamQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamQueryRepositoryImpl(@Qualifier("queryDataSource") DataSource queryDataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(queryDataSource);
    }

    @Override
    public Optional<Exam> getExamById(UUID id) {
        final SqlParameterSource parameters = new MapSqlParameterSource("id", UuidAdapter.getBytesFromUUID(id));

        String query = "SELECT " +
            "exam_id, " +
            "session_id, " +
            "assessment_id, " +
            "student_id, " +
            "attempts, " +
            "exam.status, " +
            "client_name, " +
            "subject, " +
            "date_started, " +
            "date_deleted, " +
            "date_changed, " +
            "date_completed, " +
            "date_scored, " +
            "created_at, " +
            "esc.description, " +
            "esc.stage \n " +
            "FROM exam.exam exam \n" +
            "JOIN exam_status_codes esc ON esc.status = exam.status \n" +
            "WHERE exam_id = :id";

        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(query, parameters, new ExamRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }

        return examOptional;
    }

    @Override
    public Optional<Exam> getLastAvailableExam(long studentId, String assessmentId, String clientName) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("studentId", studentId);
        queryParameters.put("assessmentId", assessmentId);
        queryParameters.put("clientName", clientName);

        final SqlParameterSource parameters = new MapSqlParameterSource(queryParameters);

        String query = "SELECT " +
            "exam_id, " +
            "session_id, " +
            "assessment_id, " +
            "student_id, " +
            "attempts, " +
            "exam.status, " +
            "client_name, " +
            "subject, " +
            "date_started, " +
            "date_deleted, " +
            "date_changed, " +
            "date_completed, " +
            "date_scored, " +
            "created_at, " +
            "esc.description, " +
            "esc.stage \n " +
            "FROM exam.exam exam \n" +
            "JOIN exam_status_codes esc ON esc.status = exam.status \n" +
            "WHERE date_deleted IS NULL \n" +
            "AND student_id = :studentId \n" +
            "AND assessment_id = :assessmentId \n" +
            "AND client_name = :clientName \n" +
            "ORDER BY exam_id DESC \n" +
            "LIMIT 1";

        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(query, parameters, new ExamRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }

        return examOptional;
    }

    @Override
    public List<Ability> findAbilities(UUID exam, String clientName, String subject, Long studentId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("examId", UuidAdapter.getBytesFromUUID(exam));
        parameters.put("clientName", clientName);
        parameters.put("subject", subject);
        parameters.put("studentId", studentId);

        final String SQL =
            "SELECT\n" +
                "exam.exam_id,\n" +
                "exam.assessment_id,\n" +
                "exam.attempts,\n" +
                "exam.date_scored,\n" +
                "scores.value AS score\n" +
            "FROM\n" +
                "exam, scores\n" +
            "WHERE\n" +
                "exam.client_name = :clientName AND\n" +
                "exam.student_id = :studentId AND\n" +
                "exam.subject = :subject AND\n" +
                "exam.date_deleted IS NULL AND\n" +
                "exam.date_scored IS NOT NULL AND\n" +
                "exam.exam_id <> :examId AND\n" +
                "exam.exam_id = scores.fk_scores_examid_exam AND\n" +
                "scores.use_for_ability = 1 AND\n" +
                "scores.value IS NOT NULL";


        return jdbcTemplate.query(SQL, parameters, new AbilityRowMapper());
    }

    private class AbilityRowMapper implements RowMapper<Ability> {
        @Override
        public Ability mapRow(ResultSet rs, int rowNum) throws SQLException {
            Ability ability = new Ability();
            ability.setExamId(UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")));
            ability.setDateScored(ResultSetMapperUtility.mapTimeStampToInstant(rs, "date_scored"));
            ability.setAttempts((Integer)rs.getObject("attempts"));
            ability.setAssessment(rs.getString("assessment_id"));
            ability.setScore(rs.getObject("score", Float.class));
            return ability;
        }
    }

    private class ExamRowMapper implements RowMapper<Exam> {
        @Override
        public Exam mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Exam.Builder()
                .withId(UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")))
                .withSessionId(UuidAdapter.getUUIDFromBytes(rs.getBytes("session_id")))
                .withAssessmentId(rs.getString("assessment_id"))
                .withStudentId(rs.getLong("student_id"))
                .withAttempts(rs.getInt("attempts"))
                .withClientName(rs.getString("client_name"))
                .withSubject(rs.getString("subject"))
                .withDateStarted(mapTimezoneToInstant(rs, "date_started"))
                .withDateChanged(mapTimezoneToInstant(rs, "date_changed"))
                .withDateDeleted(mapTimezoneToInstant(rs, "date_deleted"))
                .withDateScored(mapTimezoneToInstant(rs, "date_scored"))
                .withDateCompleted(mapTimezoneToInstant(rs, "date_completed"))
                .withCreatedAt(mapTimezoneToInstant(rs, "created_at"))
                .withStatus(new ExamStatusCode.Builder()
                    .withStatus(rs.getString("status"))
                    .withDescription(rs.getString("description"))
                    .withStage(rs.getString("stage"))
                    .build())
                .build();
        }

        private Instant mapTimezoneToInstant(ResultSet rs, String columnLabel) throws SQLException {
            Timestamp t = rs.getTimestamp(columnLabel);
            return t != null ? t.toLocalDateTime().toInstant(ZoneOffset.UTC) : null;
        }
    }
}
