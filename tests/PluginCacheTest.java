package win.movie8.tv;

import java.io.File;
import java.nio.file.Files;

public final class PluginCacheTest {
    public static void main(String[] args)throws Exception{
        File root=Files.createTempDirectory("plugin-cache-test").toFile();File code=new File(root,"code");code.mkdirs();
        File marker=new File(root,"version"),shared=new File(code,"sharedb"),optimized=new File(code,"wencai_spider"),unrelated=new File(code,"keep");
        shared.mkdirs();optimized.mkdirs();unrelated.mkdirs();File payload=new File(shared,"config.dex");payload.createNewFile();new File(unrelated,"history").createNewFile();
        require(PluginCache.prepare(code,marker,"one"),"first migration");require(!shared.exists()&&!optimized.exists(),"only loader caches removed");require(new File(unrelated,"history").exists(),"unrelated file preserved");
        PluginCache.commit(marker,"one");shared.mkdirs();payload.createNewFile();
        require(!PluginCache.prepare(code,marker,"one"),"same plugin reused");require(payload.exists(),"cache retained");
        require(PluginCache.prepare(code,marker,"two"),"new plugin invalidates");require(!shared.exists(),"old payload removed");
        PluginCache.commit(marker,"two");require(!PluginCache.prepare(code,marker,"two"),"new fingerprint saved");
        PluginCache.reset(code);require(new File(unrelated,"history").exists(),"reset stays scoped");
        System.out.println("PluginCacheTest passed");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
