package org.uit.utimea.shared.initializr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(0)
public class SqlDataInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        String sqlFileName = "init.sql";
        Resource resource = new ClassPathResource(sqlFileName);

        if (resource.exists()) {
            try (Connection connection = dataSource.getConnection()) {
                log.info("Executing SQL data initialization script: {}", sqlFileName);
                ScriptUtils.executeSqlScript(connection, resource);
                log.info("Successfully executed {}", sqlFileName);
            } catch (SQLException e) {
                log.error("Error executing SQL script: {}", sqlFileName, e);
            }
        } else {
            log.warn("SQL data file '{}' not found in resources. Skipping data initialization.", sqlFileName);
        }
    }
}
