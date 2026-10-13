package win.movie8.tv;

import android.content.Context;
import android.util.Log;
import org.conscrypt.Conscrypt;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.Security;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class WencaiClient {
    private static final String TAG = "Movie8Wencai";
    private static final String BASE = "http://www.ghw9zwp5.com";
    private static final String HTTPS_BASE = "https://www.ghw9zwp5.com";
    private static final String KEY = "cb808529bae6b6be45ecfab29a4889bc";
    private static final String DEVICE_ID = "7c760c16-4a1a-4d5a-99cf-7b9d582385b9";
    private static volatile OkHttpClient HTTP;
    private static final Map<String, String> TYPES = new HashMap<>();

    static {
        TYPES.put("recent", "");
        TYPES.put("movie", "1");
        TYPES.put("series", "2");
        TYPES.put("variety", "3");
        TYPES.put("anime", "4");
    }

    private WencaiClient() { }

    public static synchronized void initialize(Context context) {
        if (HTTP == null) HTTP = createHttpClient(context.getApplicationContext());
        DynamicWencaiSource.initialize(context.getApplicationContext(), HTTP);
    }

    private static OkHttpClient http() {
        if (HTTP == null) throw new IllegalStateException("WencaiClient was not initialized");
        return HTTP;
    }

    private static OkHttpClient createHttpClient(Context context) {
        try {
            if (Security.getProvider("Conscrypt") == null) {
                Security.insertProviderAt(Conscrypt.newProvider(), 1);
            }
            Log.i(TAG, "Conscrypt TLS provider enabled");
        } catch (Throwable error) {
            Log.w(TAG, "Could not enable Conscrypt; using platform TLS", error);
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(16, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        try {
            X509TrustManager trust = CertificateTrust.withIsrgRoot(context);
            SSLContext ssl = SSLContext.getInstance("TLS", "Conscrypt");
            ssl.init(null, new X509TrustManager[]{trust}, null);
            builder.sslSocketFactory(ssl.getSocketFactory(), trust);

        } catch (Exception ignored) { }
        return builder.build();
    }

    public static List<Movie> load(String section) throws Exception {
        return load(section, 1);
    }
    public static List<Movie> load(String section, int page) throws Exception {
        return loadInternal(section, page);
    }
    private static List<Movie> loadInternal(String section, int page) throws Exception {
        try {
            if (DynamicWencaiSource.available()) return DynamicWencaiSource.load(section, page);
        } catch (Exception ignored) { }
        if ("recent".equals(section)) {
            ArrayList<Movie> all = new ArrayList<>();
            for (String type : new String[]{"1", "2", "3", "4"}) all.addAll(list(type, page));
            Collections.sort(all, new Comparator<Movie>() {
                public int compare(Movie a, Movie b) { return b.time.compareTo(a.time); }
            });
            return unique(all, 25);
        }
        return list(TYPES.get(section), page);
    }

    public static List<Movie> search(String keyword) throws Exception {
        try {
            if (DynamicWencaiSource.available()) return DynamicWencaiSource.search(keyword);
        } catch (Exception ignored) { }
        ArrayList<Movie> all = new ArrayList<>();
        for (int page = 1; page <= 5; page++) {
            String canonical = "keyword=" + keyword + "&pageNum=" + page + "&pageSize=25";
            JSONObject data = request("/api/mw-movie/anonymous/video/searchByWord",
                    "keyword=" + enc(keyword) + "&pageNum=" + page + "&pageSize=25", canonical);
            JSONObject result = data.optJSONObject("result");
            if (result == null) result = data.optJSONObject("typeResult");
            if (result == null) result = data;
            all.addAll(toMovies(result.optJSONArray("list")));
        }
        final String query=normalizeTitle(keyword);
        Collections.sort(all, new Comparator<Movie>() {
            public int compare(Movie a, Movie b) {
                int byMatch=Integer.compare(matchRank(a.name,query),matchRank(b.name,query));
                return byMatch!=0?byMatch:b.time.compareTo(a.time);
            }
        });
        return unique(all, 100);
    }

    public static JSONObject detail(String id) throws Exception {
        try {
            if (DynamicWencaiSource.available()) return DynamicWencaiSource.detail(id);
        } catch (Exception ignored) { }
        return request("/api/mw-movie/anonymous/video/detail", "id=" + enc(id), "id=" + id);
    }

    public static String resolve(String id, String nid, String publicIp) throws Exception {
        try {
            if (DynamicWencaiSource.available()) return DynamicWencaiSource.resolve(nid);
        } catch (Exception ignored) { }
        if (publicIp == null || publicIp.isEmpty()) publicIp = publicIpv4();
        String canonical = "clientType=3&id=" + id + "&nid=" + nid;
        JSONObject data;
        try {
            data = request("/api/mw-movie/anonymous/v2/video/episode/url",
                    "clientType=3&id=" + enc(id) + "&nid=" + enc(nid), canonical, publicIp);
        } catch (Exception directError) {
            throw new Exception(AppText.get(R.string.local_playback_failed), directError);
        }
        JSONArray streams = data.optJSONArray("list");
        if (streams == null) throw new Exception(AppText.get(R.string.no_playback_address));
        String fallback = "";
        for (int i = 0; i < streams.length(); i++) {
            JSONObject stream = streams.optJSONObject(i);
            String url = stream == null ? "" : stream.optString("url");
            if (url.contains("whip=" + publicIp) || url.contains("auth_key=")) return url;
            if (fallback.isEmpty()) fallback = url;
        }
        if (fallback.isEmpty()) throw new Exception(AppText.get(R.string.no_playable_address));
        return fallback;
    }

    public static String publicIpv4() throws Exception {
        Exception last = null;
        for (String endpoint : new String[]{"https://api.ipify.org/", "http://v4.ident.me/"}) {
            try {

                Request request = new Request.Builder().url(endpoint).header("user-agent", "okhttp/3.15").build();
                try (Response response = http().newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("IP service returned HTTP " + response.code());
                    String address = responseText(response).trim();

                    return address;
                }
            } catch (Exception error) {
                last = error;

            }
        }
        throw new Exception(AppText.get(R.string.public_ip_failed), last);
    }

    private static List<Movie> list(String type, int page) throws Exception {
        String query = "type1=" + type + "&pageNum=" + page + "&area=&year=";
        JSONObject data = request("/api/mw-movie/anonymous/video/list", query,
                "area=&pageNum=" + page + "&type1=" + type + "&year=");
        return toMovies(data.optJSONArray("list"));
    }

    private static JSONObject request(String path, String query, String canonical) throws Exception {
        return request(path, query, canonical, "");
    }

    private static JSONObject request(String path, String query, String canonical, String clientIp) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = NativeBridge.available() ? NativeBridge.sign(canonical, KEY, timestamp)
                : javaSign(canonical, KEY, timestamp);
        Exception last = null;
        for (String base : new String[]{HTTPS_BASE, BASE}) {
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    Log.d(TAG, "request start protocol=" + protocol(base) + " attempt=" + (attempt + 1) + " path=" + path);
                    Request.Builder request = new Request.Builder().url(base + path + "?" + query)
                            .header("accept", "application/json")
                            .header("deviceid", DEVICE_ID)
                            .header("sign", signature)
                            .header("t", timestamp)
                            .header("user-agent", clientIp.isEmpty() ? "okhttp/3.15" : "Mozilla/5.0 (Linux; Android 5.0)");
                    if (!clientIp.isEmpty()) request.header("x-forwarded-for", clientIp);
                    try (Response raw = http().newCall(request.build()).execute()) {
                        String body = responseText(raw);
                        if (!raw.isSuccessful()) throw new IOException("HTTP " + raw.code() + ": " + body);
                        JSONObject response = new JSONObject(body);
                        if (response.optInt("code") != 200) {
                            throw new Exception(response.optString("msg", AppText.get(R.string.source_request_failed)));
                        }
                        Log.d(TAG, "request success protocol=" + protocol(base)
                                + " attempt=" + (attempt + 1) + " path=" + path);
                        return response.optJSONObject("data");
                    }
                } catch (Exception error) {
                    last = error;
                    Log.e(TAG, "request failed protocol=" + protocol(base) + " attempt=" + (attempt + 1)
                            + " path=" + path + " error=" + describe(error));
                }
            }
        }
        throw new Exception(AppText.get(R.string.direct_request_failed), last);
    }

    private static List<Movie> toMovies(JSONArray list) {
        ArrayList<Movie> result = new ArrayList<>();
        if (list == null) return result;
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.optJSONObject(i);
            if (item != null) result.add(Movie.fromJson(item));
        }
        return result;
    }

    private static List<Movie> unique(List<Movie> input, int limit) {
        ArrayList<Movie> result = new ArrayList<>();
        HashMap<String, Boolean> ids = new HashMap<>();
        for (Movie movie : input) {
            if (!movie.id.isEmpty() && !ids.containsKey(movie.id)) {
                ids.put(movie.id, true);
                result.add(movie);
                if (result.size() >= limit) break;
            }
        }
        return result;
    }

    private static String responseText(Response response) throws IOException {
        ResponseBody body = response.body();
        return body == null ? "" : body.string();
    }

    private static String protocol(String base) { return base.substring(0, base.indexOf(':')); }
    private static String enc(String value) throws Exception { return java.net.URLEncoder.encode(value, "UTF-8"); }

    private static String describe(Throwable error) {
        StringBuilder result = new StringBuilder();
        Throwable current = error;
        for (int depth = 0; current != null && depth < 6; depth++) {
            if (depth > 0) result.append(" <- ");
            result.append(current.getClass().getName()).append(": ").append(current.getMessage());
            current = current.getCause();
        }
        return result.toString();
    }

    private static String javaSign(String canonical, String key, String time) throws Exception {
        String input = canonical + (canonical.isEmpty() ? "" : "&") + "key=" + key + "&t=" + time;
        String md5 = hex(MessageDigest.getInstance("MD5").digest(input.getBytes("UTF-8")));
        return hex(MessageDigest.getInstance("SHA-1").digest(md5.getBytes("UTF-8")));
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) result.append(String.format("%02x", value & 255));
        return result.toString();
    }
    private static int matchRank(String title,String query){String value=normalizeTitle(title);if(value.equals(query))return 0;if(value.startsWith(query))return 1;if(value.contains(query))return 2;return 3;}
    private static String normalizeTitle(String value){return value==null?"":value.replaceAll("[\\s·・:：,，.。!！?？（）()【】\\[\\]_-]","").toLowerCase();}
}
