// ABOUTME: Test utility for creating database connections without Spring context
// ABOUTME: Mirrors application.properties configuration for integration testing
package it.xpug.pbw.datasource;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class TestDataSource {

    /**
     * Creates a DataSource configured for the dedicated test database
     * @return DataSource for the plantsdb_test database
     */
    public static DataSource createDataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/plantsdb_test");
        dataSource.setUser("pbwuser");
        dataSource.setPassword("pbwpass");
        return dataSource;
    }

    /**
     * Creates a JdbcTemplate from the test DataSource
     * @return JdbcTemplate ready for repository testing
     */
    public static JdbcTemplate createJdbcTemplate() {
        return new JdbcTemplate(createDataSource());
    }
}
