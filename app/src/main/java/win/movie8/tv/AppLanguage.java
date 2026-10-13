package win.movie8.tv;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

final class AppLanguage {
    static Context wrap(Context context) {
        Configuration config = new Configuration(context.getResources().getConfiguration());
        Locale device = android.os.Build.VERSION.SDK_INT >= 24 ? config.getLocales().get(0) : config.locale;
        config.setLocale(LanguagePolicy.isSimplifiedChinese(device) ? Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH);
        return context.createConfigurationContext(config);
    }
}
