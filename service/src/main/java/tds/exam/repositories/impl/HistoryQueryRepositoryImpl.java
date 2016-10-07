package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.exam.repositories.HistoryQueryRepository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class for retrieving data from the exam history table.
 *
 * Created by emunoz on 10/6/16.
 */
@Repository
public class HistoryQueryRepositoryImpl implements HistoryQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public HistoryQueryRepositoryImpl(@Qualifier("queryDataSource") DataSource queryDataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(queryDataSource);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Optional<Float> findAbilityFromHistoryForSubjectAndStudent(String clientName, String subject, Long studentId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientName", clientName);
        parameters.put("subject", subject);
        parameters.put("studentId", studentId);

        final String SQL =
                "SELECT\n" +
                        "MAX(initial_ability)\n" +
                        "FROM\n" +
                        "exam.history\n" +
                        "WHERE\n" +
                        "client_name = :clientName AND\n" +
                        "student_id = :studentId AND\n" +
                        "subject = :subject AND\n" +
                        "initial_ability IS NOT NULL;";

        Optional<Float> abilityOptional;
        try {
            abilityOptional = Optional.ofNullable(jdbcTemplate.queryForObject(SQL, parameters, Float.class));
        } catch (EmptyResultDataAccessException e) {
            abilityOptional = Optional.empty();
        }

        return abilityOptional;
    }
}
