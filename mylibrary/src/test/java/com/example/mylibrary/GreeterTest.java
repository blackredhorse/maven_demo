package com.example.mylibrary;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GreeterTest {

    @Test
    public void greetsTheGivenName() {
        assertEquals("Hello, Android!", Greeter.greet("Android"));
    }

    @Test
    public void fallsBackToWorld() {
        assertEquals("Hello, world!", Greeter.greet("  "));
    }
}
