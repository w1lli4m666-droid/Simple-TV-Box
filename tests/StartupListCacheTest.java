package win.movie8.tv;

import java.io.File;
import java.nio.file.Files;

public final class StartupListCacheTest {
    public static void main(String[] args) throws Exception {
        File dir = Files.createTempDirectory("purebox-startup-cache").toFile();
        File file = new File(dir, "recent.bin");
        long now = 100000000L;
        check(StartupListCache.read(file, now) == null, "missing");
        StartupListCache.save(file, "[{\"name\":\"电影\"}]", now);
        check("[{\"name\":\"电影\"}]".equals(StartupListCache.read(file, now + 1)), "UTF8 round trip");
        check(StartupListCache.read(file, now - 1) == null, "future timestamp");
        check(StartupListCache.read(file, now + 86400001L) == null, "expired");
        StartupListCache.save(file, "[]", now + 2);
        check("[]".equals(StartupListCache.read(file, now + 2)), "replacement");
        Files.write(file.toPath(), new byte[16]);
        check(StartupListCache.read(file, now) == null, "wrong version");
        StartupListCache.save(file, "[]", now);
        byte[] truncated = Files.readAllBytes(file.toPath());
        Files.write(file.toPath(), java.util.Arrays.copyOf(truncated, truncated.length - 1));
        check(StartupListCache.read(file, now) == null, "truncated payload");
        Files.write(file.toPath(), new byte[524305]);
        check(StartupListCache.read(file, now) == null, "oversize");
        Files.delete(file.toPath());
        Files.delete(dir.toPath());
        System.out.println("StartupListCacheTest passed");
    }
    private static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
    }
}
