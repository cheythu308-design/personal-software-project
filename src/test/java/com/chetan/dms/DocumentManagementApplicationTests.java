package com.chetan.dms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DocumentManagementApplicationTests {

    @Test
    void applicationContextStarts() {
        // A successful context load verifies that the application wiring is valid.
    }
}
