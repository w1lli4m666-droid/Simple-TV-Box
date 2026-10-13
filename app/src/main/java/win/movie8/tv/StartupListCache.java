package win.movie8.tv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;

final class StartupListCache {
    private static final int VERSION = 1;
    private static final int MAX_BYTES = 512 * 1024;
    private static final long MAX_AGE_MS = 24 * 60 * 60 * 1000L;

    static String read(File file, long now) throws Exception {
        if (!file.isFile() || file.length() > MAX_BYTES + 16) return null;
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            if (in.readInt() != VERSION) return null;
            long saved = in.readLong();
            if (saved > now || now - saved > MAX_AGE_MS) return null;
            int size = in.readInt();
            if (size <= 0 || size > MAX_BYTES || file.length() != size + 16L) return null;
            byte[] bytes = new byte[size];
            in.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    static void save(File file, String json, long now) throws Exception {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        if (bytes.length == 0 || bytes.length > MAX_BYTES) return;
        File pending = new File(file.getParentFile(), file.getName() + ".pending");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(pending))) {
            out.writeInt(VERSION);
            out.writeLong(now);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
        if (pending.renameTo(file)) return;
        // Android replaces atomically; Windows needs the disposable old snapshot removed first.
        if (!file.isFile() || !file.delete() || !pending.renameTo(file))
            throw new java.io.IOException("Cannot replace startup list cache");
    }
}
