package win.movie8.tv;

import android.content.Context;
import android.app.Application;
import android.util.Base64;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;

final class DynamicWencaiSource {
    private static final String SUBSCRIPTION = "http://www.xn--sss604efuw.net/tv";
    private static final Pattern IMAGE_PAYLOAD = Pattern.compile("[A-Za-z0-9]{8}\\*\\*");
    private static final String EPISODE_SEPARATOR = "\u0001";
    private static final Object LOCK = new Object();
    private static Context app;
    private static OkHttpClient http;
    private static Object spider;
    private static Method homeContent, homeVideoContent, categoryContent, detailContent, searchContent, playerContent;
    private static boolean attempted;
    private static Exception initializationFailure;
    private static final HashMap<String, String> categoryIds = new HashMap<>();

    private DynamicWencaiSource() { }

    static void initialize(Context context, OkHttpClient client) {
        app = context.getApplicationContext();
        http = client;
    }

    static boolean available() {
        try { ensureReady(); return true; }
        catch (Exception | LinkageError error) { return false; }
    }

    static List<Movie> load(String section, int page) throws Exception {
        ensureReady();
        if ("recent".equals(section)) {
            ArrayList<Movie> combined = new ArrayList<>();
            for (String item : new String[]{"movie", "series", "variety", "anime"}) combined.addAll(category(item, page));
            Collections.sort(combined, (a, b) -> b.time.compareTo(a.time));
            return unique(combined, 25);
        }
        return category(section, page);
    }

    static List<Movie> search(String keyword) throws Exception {
        ensureReady();
        ArrayList<Movie> result = new ArrayList<>();
        for (int page = 1; page <= 5; page++) {
            String json = (String) searchContent.invoke(spider, keyword, false, String.valueOf(page));
            List<Movie> pageItems = movies(json);
            result.addAll(pageItems);
            if (pageItems.isEmpty()) break;
        }
        return unique(result, 125);
    }

    static JSONObject detail(String id) throws Exception {
        ensureReady();
        String json = (String) detailContent.invoke(spider, Collections.singletonList(id));
        JSONObject root = new JSONObject(json);
        JSONArray list = root.optJSONArray("list");
        if (list == null || list.length() == 0) throw new Exception(AppText.get(R.string.missing_details));
        JSONObject item = list.getJSONObject(0);
        JSONObject result = new JSONObject();
        result.put("vodContent", item.optString("vod_content", item.optString("vod_remarks")));
        JSONArray episodes = new JSONArray();
        String[] flags = item.optString("vod_play_from").split("\\$\\$\\$");
        String[] sources = item.optString("vod_play_url").split("\\$\\$\\$");
        for (int source = 0; source < sources.length; source++) {
            String flag = source < flags.length ? flags[source] : "";
            String[] values = sources[source].split("#");
            for (String value : values) {
                int marker = value.indexOf('$');
                String name = marker > 0 ? value.substring(0, marker) : value;
                String playId = marker > 0 ? value.substring(marker + 1) : value;
                if (playId.isEmpty()) continue;
                JSONObject episode = new JSONObject();
                episode.put("name", name.isEmpty() ? String.valueOf(episodes.length() + 1) : name);
                episode.put("nid", flag + EPISODE_SEPARATOR + playId);
                episodes.put(episode);
            }
            if (episodes.length() > 0) break;
        }
        result.put("episodeList", episodes);
        return result;
    }

    static String resolve(String encodedEpisode) throws Exception {
        ensureReady();
        int marker = encodedEpisode.indexOf(EPISODE_SEPARATOR);
        String flag = marker >= 0 ? encodedEpisode.substring(0, marker) : "";
        String playId = marker >= 0 ? encodedEpisode.substring(marker + EPISODE_SEPARATOR.length()) : encodedEpisode;
        String json = (String) playerContent.invoke(spider, flag, playId, Collections.emptyList());
        JSONObject player = new JSONObject(json);
        String url = player.optString("url");
        if (url.isEmpty()) throw new Exception(player.optString("msg", AppText.get(R.string.missing_video_url)));
        return url;
    }

    private static List<Movie> category(String section, int page) throws Exception {
        String tid = categoryIds.get(section);
        if (tid == null || tid.isEmpty()) throw new Exception(AppText.get(R.string.missing_category, section));
        String json = (String) categoryContent.invoke(spider, tid, String.valueOf(page), true, new HashMap<String, String>());
        return movies(json);
    }

    private static List<Movie> movies(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray list = root.optJSONArray("list");
        ArrayList<Movie> result = new ArrayList<>();
        if (list != null) for (int i = 0; i < list.length(); i++) {
            JSONObject value = list.optJSONObject(i);
            if (value != null) result.add(Movie.fromJson(value));
        }
        return result;
    }

    private static void ensureReady() throws Exception {
        synchronized (LOCK) {
            if (spider != null) return;
            if (initializationFailure != null) throw initializationFailure;
            Exception refreshError = null;
            if (!attempted) {
                attempted = true;
                try { refreshPlugin(); }
                catch (Exception error) { refreshError = error; }
            }
            File cached = pluginFile();
            try {
                if (!cached.isFile()) throw new Exception(AppText.get(R.string.missing_plugin_cache), refreshError);
                loadPlugin(cached);
            } catch (Exception | LinkageError error) {
                initializationFailure = new Exception(AppText.get(R.string.plugin_init_failed), error);
                throw initializationFailure;
            }
        }
    }

    private static void refreshPlugin() throws Exception {
        byte[] subscription = get(SUBSCRIPTION);
        String config = decodeSubscription(subscription);
        JSONObject root = new JSONObject(config);
        String descriptor = root.optString("spider");
        if (descriptor.isEmpty()) throw new Exception(AppText.get(R.string.missing_spider));
        String[] parts = descriptor.split(";md5;", 2);
        String url = parts[0].trim();
        String expected = parts.length > 1 ? parts[1].trim() : "";
        File cached = pluginFile();
        boolean hit = cached.isFile() && !expected.isEmpty() && expected.equalsIgnoreCase(md5(cached));
        if (hit) return;
        byte[] plugin = get(url);
        if (!expected.isEmpty() && !expected.equalsIgnoreCase(md5(plugin))) throw new Exception(AppText.get(R.string.plugin_checksum_failed));
        File dir = cached.getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs()) throw new Exception(AppText.get(R.string.create_plugin_cache_failed));
        File pending = new File(dir, "wencai.pending");
        try (FileOutputStream output = new FileOutputStream(pending)) { output.write(plugin); }
        if (cached.exists() && !cached.delete()) throw new Exception(AppText.get(R.string.replace_plugin_failed));
        if (!pending.renameTo(cached)) throw new Exception(AppText.get(R.string.save_plugin_failed));
    }

    private static String decodeSubscription(byte[] bytes) throws Exception {
        String raw = new String(bytes, Charset.forName("ISO-8859-1"));
        String trimmed = raw.trim();
        if (trimmed.startsWith("{")) return trimmed;
        Matcher marker = IMAGE_PAYLOAD.matcher(raw);
        if (!marker.find()) throw new Exception(AppText.get(R.string.unknown_subscription));
        String payload = raw.substring(marker.end()).trim();
        return new String(Base64.decode(payload, Base64.DEFAULT), "UTF-8");
    }

    private static void loadPlugin(File file) throws Exception {
        File optimized = new File(app.getCodeCacheDir(), "wencai_spider");
        if (!file.setReadOnly() && file.canWrite()) throw new Exception(AppText.get(R.string.readonly_plugin_failed));
        if (!optimized.exists() && !optimized.mkdirs()) throw new Exception(AppText.get(R.string.create_optimized_cache_failed));
        DexClassLoader loader = new DexClassLoader(file.getAbsolutePath(), optimized.getAbsolutePath(), null, app.getClassLoader());
        Class<?> init = loader.loadClass("com.github.catvod.spider.Init");
        init.getMethod("init", Context.class).invoke(null, app);
        Object value = init.getMethod("loader").invoke(null);
        if (!(value instanceof ClassLoader)) throw new Exception("Init.loader() did not return an internal class loader");
        ClassLoader inner = (ClassLoader) value;
        Class<?> origin = inner.loadClass("com.github.catvod.spider.InitOrigin");
        Method getter = PluginContextBridge.contextGetter(origin, Context.class);
        if (!PluginContextBridge.alreadyInitialized(getter, app)) {
            Method originInit = PluginContextBridge.initializer(origin, Context.class, Application.class);
            originInit.invoke(null, app);
        }
        PluginContextBridge.verify(getter, app);
        Class<?> type = loader.loadClass("com.github.catvod.spider.JpysGuard");
        Object candidate = type.newInstance();
        type.getMethod("init", Context.class, String.class).invoke(candidate, app, "");
        Method[] methods = new Method[]{
            type.getMethod("homeContent", boolean.class),
            type.getMethod("homeVideoContent"),
            type.getMethod("categoryContent", String.class, String.class, boolean.class, HashMap.class),
            type.getMethod("detailContent", List.class),
            type.getMethod("searchContent", String.class, boolean.class, String.class),
            type.getMethod("playerContent", String.class, String.class, List.class)
        };
        String categories = (String) methods[0].invoke(candidate, true);
        HashMap<String, String> ids = readCategories(categories);
        // Publish only after every initialization stage succeeds.
        homeContent = methods[0]; homeVideoContent = methods[1]; categoryContent = methods[2];
        detailContent = methods[3]; searchContent = methods[4]; playerContent = methods[5];
        categoryIds.clear(); categoryIds.putAll(ids);
        spider = candidate;
    }

    private static HashMap<String, String> readCategories(String json) throws Exception {
        HashMap<String, String> ids = new HashMap<>();
        JSONArray classes = new JSONObject(json).optJSONArray("class");
        if (classes == null) throw new Exception(AppText.get(R.string.missing_categories));
        for (int i = 0; i < classes.length(); i++) {
            JSONObject item = classes.optJSONObject(i);
            if (item == null) continue;
            String name = item.optString("type_name");
            String id = item.optString("type_id");
            if (name.contains("电影")) ids.put("movie", id);
            else if (name.contains("剧集") || name.contains("电视剧")) ids.put("series", id);
            else if (name.contains("综艺")) ids.put("variety", id);
            else if (name.contains("动漫") || name.contains("动画")) ids.put("anime", id);
        }
        if (ids.isEmpty()) throw new Exception(AppText.get(R.string.unknown_categories));
        return ids;
    }

    private static byte[] get(String url) throws Exception {
        Request request = new Request.Builder().url(url).header("user-agent", "okhttp/3.15").build();
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new Exception("HTTP " + response.code() + " from " + url);
            ResponseBody body = response.body();
            if (body == null) throw new Exception(AppText.get(R.string.empty_response, url));
            return body.bytes();
        }
    }

    private static File pluginFile() { return new File(new File(app.getFilesDir(), "spiders"), "wencai.jar"); }
    private static String md5(File file) throws Exception { try (FileInputStream input = new FileInputStream(file)) { MessageDigest digest = MessageDigest.getInstance("MD5"); byte[] buffer = new byte[8192]; int count; while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count); return hex(digest.digest()); } }
    private static String md5(byte[] bytes) throws Exception { return hex(MessageDigest.getInstance("MD5").digest(bytes)); }
    private static String hex(byte[] bytes) { StringBuilder value = new StringBuilder(); for (byte item : bytes) value.append(String.format("%02x", item & 255)); return value.toString(); }
    private static List<Movie> unique(List<Movie> input, int limit) { ArrayList<Movie> result = new ArrayList<>(); HashMap<String, Boolean> ids = new HashMap<>(); for (Movie movie : input) if (!movie.id.isEmpty() && !ids.containsKey(movie.id)) { ids.put(movie.id, true); result.add(movie); if (result.size() >= limit) break; } return result; }
}
