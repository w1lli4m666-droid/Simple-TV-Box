package win.movie8.tv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SearchHistoryTermsTest {
    public static void main(String[] args){
        List<String> result=SearchHistoryTerms.record(Arrays.asList("Star","Other","STAR")," star ");
        if(!result.equals(Arrays.asList("star","Other")))throw new AssertionError(result.toString());
        ArrayList<String> many=new ArrayList<>();for(int i=0;i<100;i++)many.add("term"+i);
        if(SearchHistoryTerms.record(many,"latest").size()!=24)throw new AssertionError("limit");
        if(!SearchHistoryTerms.record(Arrays.asList(" ","a")," ").equals(Arrays.asList("a")))throw new AssertionError("blank");
        System.out.println("SearchHistoryTermsTest passed");
    }
}
