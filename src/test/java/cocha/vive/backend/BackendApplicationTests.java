package cocha.vive.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires a running PostgreSQL instance — context load smoke test has no value when all other tests cover the context indirectly")
@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
