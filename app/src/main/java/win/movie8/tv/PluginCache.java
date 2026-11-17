package win.movie8.tv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

final class PluginCache {
    private static final String VERSION="1:";

    static boolean prepare(File root,File marker,String fingerprint)throws IOException{
        if((VERSION+fingerprint).equals(read(marker)))return false;
        reset(root);return true;
    }
    static void reset(File root)throws IOException{
        File base=root.getCanonicalFile();
        for(String name:new String[]{"sharedb","wencai_spider"})remove(new File(base,name),base);
    }
    private static void remove(File file,File root)throws IOException{
        if(!file.exists())return;
        if(!file.getCanonicalFile().equals(file.getAbsoluteFile())||!file.getCanonicalPath().startsWith(root.getPath()+File.separator))throw new IOException("Plugin cache escapes code cache directory");
        if(file.isDirectory()){
            File[] children=file.listFiles();if(children==null)throw new IOException("Cannot list plugin cache");
            for(File child:children)remove(child,root);
        }
        if(!file.delete())throw new IOException("Cannot remove plugin cache: "+file.getName());
    }
    static void commit(File marker,String fingerprint)throws IOException{
        File pending=new File(marker.getParentFile(),marker.getName()+".pending");
        try(FileOutputStream out=new FileOutputStream(pending)){out.write((VERSION+fingerprint).getBytes("UTF-8"));}
        if(!pending.renameTo(marker)){
            if(!marker.isFile()||!marker.delete()||!pending.renameTo(marker))throw new IOException("Cannot save plugin cache fingerprint");
        }
    }
    private static String read(File file)throws IOException{
        if(!file.isFile())return "";
        if(file.length()>128)return "";
        try(FileInputStream in=new FileInputStream(file)){
            byte[] bytes=new byte[128];int n=in.read(bytes);return n<0?"":new String(bytes,0,n,"UTF-8");
        }
    }
}
