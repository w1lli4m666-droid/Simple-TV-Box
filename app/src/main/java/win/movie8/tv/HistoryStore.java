package win.movie8.tv;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class HistoryStore {
    private static final String KEY = "history";
    private HistoryStore() { }
    public static void save(Context context, Movie movie, Episode episode, long position) {
        try {
            JSONArray next = new JSONArray(); JSONObject current = new JSONObject();
            current.put("id", movie.id); current.put("name", movie.name); current.put("poster", movie.poster); current.put("remarks", movie.remarks);
            current.put("nid", episode.nid); current.put("episode", episode.name); current.put("position", position); current.put("updated", System.currentTimeMillis()); next.put(current);
            for (JSONObject item : items(context)) if (!(movie.id.equals(item.optString("id")) && episode.nid.equals(item.optString("nid")))) next.put(item);
            while (next.length() > 100) next.remove(next.length() - 1);
            prefs(context).edit().putString(KEY, next.toString()).apply();
        } catch (Exception ignored) { }
    }
    public static List<JSONObject> items(Context context) {
        ArrayList<JSONObject> result = new ArrayList<>();
        try { JSONArray raw = new JSONArray(prefs(context).getString(KEY, "[]")); for (int i=0;i<raw.length();i++) result.add(raw.getJSONObject(i)); } catch (Exception ignored) { }
        return result;
    }
    public static JSONObject latestForMovie(Context context, String movieId) {
        for (JSONObject item : items(context)) if (movieId.equals(item.optString("id"))) return item;
        return null;
    }
    public static void delete(Context context, JSONObject target) {
        try { JSONArray next=new JSONArray(); for(JSONObject item:items(context)) if(!(target.optString("id").equals(item.optString("id")) && target.optString("nid").equals(item.optString("nid")))) next.put(item); prefs(context).edit().putString(KEY,next.toString()).apply(); } catch(Exception ignored) { }
    }
    public static void clear(Context context) { prefs(context).edit().remove(KEY).apply(); }
    private static SharedPreferences prefs(Context context) { return context.getSharedPreferences("movie8", Context.MODE_PRIVATE); }
}
