package win.movie8.tv;

import android.content.Context;
import com.google.gson.Gson;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class ListPreviewCache {
    private static File directory;
    private static final Gson GSON=new Gson();
    static void initialize(Context context){directory=context.getFilesDir();}
    static synchronized List<Movie> read(String section){
        if(directory==null||!validSection(section))return Collections.emptyList();
        try{
            String json=StartupListCache.read(file(section),System.currentTimeMillis());if(json==null)return Collections.emptyList();
            Movie[] movies=GSON.fromJson(json,Movie[].class);if(movies==null||movies.length==0||movies.length>200)return Collections.emptyList();
            for(Movie movie:movies)if(movie==null||movie.id==null||movie.id.isEmpty()||movie.name==null||movie.poster==null||movie.remarks==null||movie.time==null||movie.year==null||movie.type==null||movie.area==null)return Collections.emptyList();
            return Arrays.asList(movies);
        }catch(Exception ignored){return Collections.emptyList();}
    }
    static synchronized void save(String section,List<Movie> movies){
        if(directory==null||!validSection(section)||movies.isEmpty())return;
        try{StartupListCache.save(file(section),GSON.toJson(movies),System.currentTimeMillis());}catch(Exception ignored){ }
    }
    private static File file(String section){return new File(directory,section+"-startup-cache.bin");}
    private static boolean validSection(String value){return "recent".equals(value)||"movie".equals(value)||"series".equals(value)||"variety".equals(value)||"anime".equals(value);}
}
