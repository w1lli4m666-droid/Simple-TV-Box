package win.movie8.tv;

import android.os.Build;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.net.Socket;
import java.io.IOException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public final class TlsCompat {
    private static SSLSocketFactory factory;
    private TlsCompat() { }
    public static HttpURLConnection open(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("connection", "keep-alive");
        if (connection instanceof HttpsURLConnection && Build.VERSION.SDK_INT <= 22) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(factory());
        }
        return connection;
    }
    private static synchronized SSLSocketFactory factory() throws IOException {
        if (factory != null) return factory;
        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, null, null);
            factory = new Tls12Factory(context.getSocketFactory());
            return factory;
        } catch (GeneralSecurityException error) {
            throw new IOException(AppText.get(R.string.tls_init_failed), error);
        }
    }
    private static final class Tls12Factory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;
        Tls12Factory(SSLSocketFactory delegate) { this.delegate = delegate; }
        private Socket enable(Socket socket) { if (socket instanceof SSLSocket) ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2"}); return socket; }
        public String[] getDefaultCipherSuites() { return delegate.getDefaultCipherSuites(); }
        public String[] getSupportedCipherSuites() { return delegate.getSupportedCipherSuites(); }
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException { return enable(delegate.createSocket(s,host,port,autoClose)); }
        public Socket createSocket(String host, int port) throws IOException { return enable(delegate.createSocket(host,port)); }
        public Socket createSocket(String host, int port, java.net.InetAddress local, int localPort) throws IOException { return enable(delegate.createSocket(host,port,local,localPort)); }
        public Socket createSocket(java.net.InetAddress host, int port) throws IOException { return enable(delegate.createSocket(host,port)); }
        public Socket createSocket(java.net.InetAddress host, int port, java.net.InetAddress local, int localPort) throws IOException { return enable(delegate.createSocket(host,port,local,localPort)); }
    }
}
