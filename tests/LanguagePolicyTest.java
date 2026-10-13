package win.movie8.tv;

import java.util.Locale;

public final class LanguagePolicyTest {
    public static void main(String[] args) {
        check(Locale.SIMPLIFIED_CHINESE, true);
        check(new Locale("zh", "SG"), true);
        check(Locale.forLanguageTag("zh-Hans"), true);
        check(Locale.forLanguageTag("zh-Hans-HK"), true);
        check(Locale.forLanguageTag("zh-Hant-CN"), false);
        check(Locale.TRADITIONAL_CHINESE, false);
        check(new Locale("zh", "HK"), false);
        check(Locale.ENGLISH, false);
        check(Locale.JAPANESE, false);
        check(Locale.FRENCH, false);
        check(null, false);
        System.out.println("LanguagePolicyTest passed");
    }
    private static void check(Locale locale, boolean expected) {
        if (LanguagePolicy.isSimplifiedChinese(locale) != expected) throw new AssertionError(String.valueOf(locale));
    }
}
