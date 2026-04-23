package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TaskPersistenceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void applicationUsesH2Datasource() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            assertTrue(
                    url.startsWith("jdbc:h2:"),
                    "The task should use an H2 datasource so students can focus on JPA mapping without external database setup."
            );
        }
    }
}
