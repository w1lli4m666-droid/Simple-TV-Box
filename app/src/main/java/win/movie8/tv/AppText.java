package win.movie8.tv;

import android.content.Context;

final class AppText {
    private static Context context;
    static void initialize(Context app) { context = AppLanguage.wrap(app); }
    static String get(int id, Object... args) { return context.getString(id, args); }
}
