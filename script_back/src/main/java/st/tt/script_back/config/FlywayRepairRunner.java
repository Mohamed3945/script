package st.tt.script_back.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Repairs the recorded Flyway checksum for V19 when the migration file is restored to the current source state.
 * <p>
 * This runner is disabled by default and only executes when the {@code repair-flyway} profile is active.
 */
@Component
@Profile("repair-flyway")
public class FlywayRepairRunner implements CommandLineRunner {

    private static final int V19_CHECKSUM = 88794278;

    private final JdbcTemplate jdbcTemplate;

    public FlywayRepairRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        int updated = jdbcTemplate.update(
                "UPDATE flyway_schema_history SET checksum = ? WHERE version = ?",
                V19_CHECKSUM,
                "19");
        System.out.println("Flyway repair applied to version 19 checksum, rows updated: " + updated);
    }
}