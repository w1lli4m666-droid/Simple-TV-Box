package com.github.catvod.crawler;

public final class SpiderDebug {
    private SpiderDebug() { }
    // Required by dynamically loaded TVBox plugins, even when logging is disabled.
    public static void log(String message) { }
    public static void log(Throwable error) { }
}
