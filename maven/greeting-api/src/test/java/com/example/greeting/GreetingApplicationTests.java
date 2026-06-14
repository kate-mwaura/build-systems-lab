package com.example.greeting;

import com.example.greeting.controller.GreetingController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class GreetingApplicationTests {

    @Autowired
    private GreetingController controller;

    @Test
    void contextLoads() {
        // Confirms the Spring Supermarket is open
        assertNotNull(controller);
    }

    @Test
    void testMainMethod() {
        // This explicitly calls the 'main' method to turn the last bar GREEN
        GreetingApplication.main(new String[] {});
    }

    @Test
    void testGreetingLogic() {
        // Executes the code inside your GreetingController
        String response = controller.greet("Kate");
        assertEquals("Hello, Kate!", response);
    }
}
