package win.movie8.tv;

import java.util.ArrayList;
import java.util.List;

final class SearchHistoryTerms {
    static List<String> record(List<String> previous,String query){
        ArrayList<String> values=new ArrayList<>();String term=query.trim();if(!term.isEmpty())values.add(term);
        for(String item:previous){if(item==null||item.trim().isEmpty())continue;boolean exists=false;for(String value:values)if(value.equalsIgnoreCase(item.trim())){exists=true;break;}if(!exists)values.add(item.trim());if(values.size()>=24)break;}
        return values;
    }
}
