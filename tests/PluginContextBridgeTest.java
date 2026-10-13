package win.movie8.tv;

import java.lang.reflect.Method;

public final class PluginContextBridgeTest {
    static class Context { }
    static class Application extends Context { }
    static class Origin {
        private static Context app;
        private static void init(Context value) { app = value; }
        private static Context context() { return app; }
    }
    static class ApplicationOrigin {
        static Application app;
        static void init(Application value) { app = value; }
        static Application context() { return app; }
    }
    static class BrokenOrigin {
        static void init(Context value) { }
        static Context context() { return null; }
    }
    static class NoGetterOrigin { static void init(Context value) { } }
    static class NonStaticOrigin { void init(Context value) { } }

    public static void main(String[] args) throws Exception {
        Application app = new Application();
        Method getter = PluginContextBridge.contextGetter(Origin.class, Context.class);
        boolean failed = false;
        try { PluginContextBridge.verify(getter, app); } catch (Exception expected) { failed = true; }
        require(failed, "null context detected");
        initialize(Origin.class, app);
        require(PluginContextBridge.alreadyInitialized(getter, app), "existing Application detected");
        require(!PluginContextBridge.alreadyInitialized(getter, new Application()), "different Application is not accepted");
        require(!PluginContextBridge.alreadyInitialized(null, app), "missing getter is not assumed ready");
        initialize(ApplicationOrigin.class, app);
        initialize(NoGetterOrigin.class, app);
        failed = false;
        try { initialize(BrokenOrigin.class, app); } catch (Exception expected) { failed = true; }
        require(failed, "broken initializer detected");
        failed = false;
        try { initialize(NonStaticOrigin.class, app); } catch (Exception expected) { failed = true; }
        require(failed, "nonstatic entry rejected");
        failed = false;
        try { initialize(Object.class, app); } catch (NoSuchMethodException expected) { failed = true; }
        require(failed, "missing entry rejected");
        System.out.println("PASS: Context/Application entries, private access, null verification, unsupported entries");
    }
    private static void initialize(Class<?> type, Application app) throws Exception {
        PluginContextBridge.initializer(type, Context.class, Application.class).invoke(null, app);
        PluginContextBridge.verify(PluginContextBridge.contextGetter(type, Context.class), app);
    }
    private static void require(boolean value, String name) { if (!value) throw new AssertionError(name); }
}
