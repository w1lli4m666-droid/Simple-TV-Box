package win.movie8.tv;

public final class EpisodeIdTest {
    public static void main(String[] args){
        String encoded="source\u0001147233@1327287";
        require(EpisodeId.raw(encoded).equals("1327287"),"plugin to API");
        require(EpisodeId.raw("147233@1327287").equals("1327287"),"bare play ID");
        require(EpisodeId.plugin("147233","1327287").equals("147233@1327287"),"API to plugin");
        require(EpisodeId.plugin("147233",encoded).equals(encoded),"preserve source flag");
        require(EpisodeId.same("1327287",encoded),"same episode across routes");
        require(!EpisodeId.same("1327286",encoded),"different episodes");
        require(!EpisodeId.same(null,""),"missing IDs");
        System.out.println("EpisodeIdTest passed");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
