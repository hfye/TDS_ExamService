package tds.exam.configuration.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration for DataSources.  Queries
 */
@Configuration
public class JdbcTemplateConfiguration {

    @Bean(name = "commandJdbcTemplate")
    public NamedParameterJdbcTemplate commandsDataSource(@Autowired DataSource commandDataSource) {
        return new NamedParameterJdbcTemplate(commandDataSource);
    }

    @Bean(name = "queryJdbcTemplate")
    public NamedParameterJdbcTemplate queriesDataSource(@Autowired DataSource queryDataSource) {
        return new NamedParameterJdbcTemplate(queryDataSource);
    }
}
