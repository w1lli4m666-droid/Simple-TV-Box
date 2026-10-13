package win.movie8.tv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(16 * 1024) {
        protected int sizeOf(String key, Bitmap value) { return value.getByteCount() / 1024; }
    };
    private static final ExecutorService WORKERS = Executors.newFixedThreadPool(3);
    private ImageLoader() { }
    public static void load(final ImageView view, final String url) {
        view.setTag(url); view.setImageDrawable(null);
        if (url == null || url.isEmpty()) return;
        Bitmap cached = CACHE.get(url); if (cached != null) { view.setImageBitmap(cached); return; }
        WORKERS.execute(new Runnable() { public void run() {
            HttpURLConnection c = null;
            try {
                c = TlsCompat.open(new URL(url));
                c.setConnectTimeout(8000); c.setReadTimeout(12000); c.setRequestProperty("user-agent", "Mozilla/5.0 (Linux; Android 5.0) SimpleTVBox/1.1");
                Bitmap image;
                try (InputStream in = c.getInputStream()) { image = BitmapFactory.decodeStream(in); }
                if (image == null) throw new java.io.IOException("Poster decode returned null");
                CACHE.put(url, image);
                view.post(new Runnable() { public void run() {
                    boolean visible = url.equals(view.getTag());
                    if (visible) view.setImageBitmap(image);
                } });
            } catch (Exception ignored) { }
            finally { if (c != null) c.disconnect(); }
        }});
    }
}
