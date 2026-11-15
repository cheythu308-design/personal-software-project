package com.chetan.dms.health;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HealthControllerTest {

    private final HealthController controller = new HealthController();

    @Test
    void healthResponseContainsUpStatusAndTimestamp() {
        var response = controller.health();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }
}
