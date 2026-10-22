package win.movie8.tv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

final class SuggestionTitles {
    static List<String> select(List<String> names,String query,int limit){
        String key=normalize(query);if(key.isEmpty()||limit<=0)return new ArrayList<>();
        LinkedHashMap<String,String> unique=new LinkedHashMap<>();
        for(String name:names){if(name==null)continue;String normalized=normalize(name);if(normalized.contains(key)&&!unique.containsKey(normalized))unique.put(normalized,name.trim());}
        ArrayList<String> values=new ArrayList<>(unique.values());
        Collections.sort(values,(a,b)->Integer.compare(rank(a,key),rank(b,key)));
        return new ArrayList<>(values.subList(0,Math.min(limit,values.size())));
    }
    private static int rank(String name,String key){String normalized=normalize(name);return normalized.equals(key)?0:normalized.startsWith(key)?1:2;}
    private static String normalize(String text){return text==null?"":text.replaceAll("[\\s·・:：,，.。!！?？（）()【】\\[\\]_-]","").toLowerCase(Locale.ROOT);}
}
