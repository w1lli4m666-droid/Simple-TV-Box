package win.movie8.tv;

import java.util.Locale;

final class LanguagePolicy {
    static boolean isSimplifiedChinese(Locale locale) {
        if (locale == null || !"zh".equals(locale.getLanguage())) return false;
        if ("Hant".equals(locale.getScript())) return false;
        if ("Hans".equals(locale.getScript())) return true;
        return "CN".equals(locale.getCountry()) || "SG".equals(locale.getCountry());
    }
}
