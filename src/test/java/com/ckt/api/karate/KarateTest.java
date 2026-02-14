package com.ckt.api.karate;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KarateTest {

    @Test
    public void testRegister() {
        Results results = Runner.builder()
            .path("classpath:features/auth-register.feature")
            .outputCucumberJson(true)
            .parallel(1);
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

    @Test
    public void testLogin() {
        Results results = Runner.builder()
            .path("classpath:features/auth-login.feature")
            .outputCucumberJson(true)
            .parallel(1);
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
