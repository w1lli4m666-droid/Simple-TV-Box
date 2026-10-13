package win.movie8.tv;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class PluginContextBridge {
    private PluginContextBridge() { }

    static Method initializer(Class<?> origin, Class<?> contextType, Class<?> applicationType) throws Exception {
        Method method;
        try { method = origin.getDeclaredMethod("init", contextType); }
        catch (NoSuchMethodException missingContext) {
            try { method = origin.getDeclaredMethod("init", applicationType); }
            catch (NoSuchMethodException missingApplication) {
                throw new NoSuchMethodException(origin.getName() + " has no init(Context) or init(Application) entry");
            }
        }
        if (!Modifier.isStatic(method.getModifiers())) throw new Exception("InitOrigin initialization entry is not static");
        method.setAccessible(true);
        return method;
    }

    static Method contextGetter(Class<?> origin, Class<?> contextType) {
        try {
            Method method = origin.getDeclaredMethod("context");
            if (!Modifier.isStatic(method.getModifiers()) || !contextType.isAssignableFrom(method.getReturnType())) return null;
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException absent) { return null; }
    }

    static void verify(Method getter, Object application) throws Exception {
        if (getter == null) return;
        Object actual = getter.invoke(null);
        if (actual != application) throw new Exception("InitOrigin.context() did not retain the supplied Application");
    }

    static boolean alreadyInitialized(Method getter, Object application) throws Exception {
        return getter != null && getter.invoke(null) == application;
    }
}
