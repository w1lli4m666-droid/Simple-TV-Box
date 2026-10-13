-keep class androidx.media3.** { *; }
-keep class win.movie8.tv.NativeBridge { *; }
-keep class com.github.catvod.crawler.** { *; }
# Loaded plugin code references Gson outside the host's shrinker-visible graph.
-keep class com.google.gson.** { *; }
# Downloaded plugins also link directly to the host networking API by name.
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
# Persisted first-page snapshots use these field names across app builds.
-keepclassmembers class win.movie8.tv.Movie { java.lang.String *; }
# Conscrypt adapters reference framework-private classes absent from SDK stubs.
-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
