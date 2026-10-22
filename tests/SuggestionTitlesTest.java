package win.movie8.tv;

import java.util.Arrays;
import java.util.List;

public final class SuggestionTitlesTest {
    public static void main(String[] args){
        List<String> result=SuggestionTitles.select(Arrays.asList("Other","The Star","Star Wars","star"," STAR ",null),"star",5);
        if(!result.equals(Arrays.asList("star","Star Wars","The Star")))throw new AssertionError(result.toString());
        if(SuggestionTitles.select(Arrays.asList("A","AB","ABC"),"a",2).size()!=2)throw new AssertionError("limit");
        if(!SuggestionTitles.select(Arrays.asList("A")," ",5).isEmpty())throw new AssertionError("blank");
        System.out.println("SuggestionTitlesTest passed");
    }
}
