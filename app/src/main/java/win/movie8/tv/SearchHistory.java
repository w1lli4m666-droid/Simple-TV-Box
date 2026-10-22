package win.movie8.tv;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

final class SearchHistory {
    static List<String> items(Context context){
        ArrayList<String> values=new ArrayList<>();
        try{JSONArray data=new JSONArray(context.getSharedPreferences("search-history",0).getString("terms","[]"));for(int i=0;i<Math.min(24,data.length());i++){String term=data.optString(i,"").trim();if(!term.isEmpty())values.add(term);}}catch(Exception ignored){ }
        return values;
    }
    static void record(Context context,String term){save(context,SearchHistoryTerms.record(items(context),term));}
    static void delete(Context context,String term){List<String> values=items(context);values.remove(term);save(context,values);}
    static void clear(Context context){save(context,new ArrayList<>());}
    private static void save(Context context,List<String> terms){context.getSharedPreferences("search-history",0).edit().putString("terms",new JSONArray(terms).toString()).apply();}
}
