package win.movie8.tv;

/** Optional JNI signing library; the Java implementation remains available. */
public final class NativeBridge {
    private static boolean loaded;
    static {
        try { System.loadLibrary("movie8_core"); loaded = true; } catch (UnsatisfiedLinkError ignored) { }
    }
    private NativeBridge() { }
    public static native String sign(String canonical, String key, String timestamp);
    public static boolean available() { return loaded; }
}
