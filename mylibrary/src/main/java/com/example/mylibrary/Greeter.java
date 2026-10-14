package com.example.mylibrary;

/** Tiny API used to prove that the published AAR/JAR really works. */
public final class Greeter {

    private Greeter() {
    }

    public static String greet(String name) {
        String who = (name == null || name.trim().isEmpty()) ? "world" : name.trim();
        return "Hello, " + who + "!";
    }
}
