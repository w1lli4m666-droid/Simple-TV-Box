package win.movie8.tv;

import android.content.Context;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

final class CertificateTrust {
    private CertificateTrust() { }

    static X509TrustManager withIsrgRoot(Context context) throws Exception {
        X509TrustManager system = manager(null);
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream input = context.getResources().openRawResource(R.raw.isrg_root_x1);
        try {
            Certificate root = factory.generateCertificate(input);
            store.setCertificateEntry("isrg-root-x1", root);
        } finally {
            input.close();
        }
        X509TrustManager isrg = manager(store);
        return new CombinedTrustManager(system, isrg);
    }

    private static X509TrustManager manager(KeyStore store) throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(store);
        for (TrustManager manager : factory.getTrustManagers()) if (manager instanceof X509TrustManager) return (X509TrustManager) manager;
        throw new CertificateException("No X509 trust manager available");
    }

    private static final class CombinedTrustManager implements X509TrustManager {
        private final X509TrustManager system;
        private final X509TrustManager isrg;
        CombinedTrustManager(X509TrustManager system, X509TrustManager isrg) { this.system = system; this.isrg = isrg; }
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { system.checkClientTrusted(chain, authType); }
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try { system.checkServerTrusted(chain, authType); }
            catch (CertificateException ignored) { isrg.checkServerTrusted(chain, authType); }
        }
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] a = system.getAcceptedIssuers(), b = isrg.getAcceptedIssuers();
            X509Certificate[] result = new X509Certificate[a.length + b.length];
            System.arraycopy(a, 0, result, 0, a.length); System.arraycopy(b, 0, result, a.length, b.length);
            return result;
        }
    }
}
