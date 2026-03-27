package com.example.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    @Test
    void testAddition() {
        int result = 2 + 3;
        assertEquals(5, result, "2 + 3 should equal 5");
    }

    @Test
    void testString() {
        String greeting = "Hello, DevSecOps!";
        assertEquals("Hello, DevSecOps!", greeting);
    }
}
