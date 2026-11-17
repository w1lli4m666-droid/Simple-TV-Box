package win.movie8.tv;

final class EpisodeId {
    static String raw(String value){
        if(value==null)return "";
        int separator=value.indexOf('\u0001');String play=separator<0?value:value.substring(separator+1);
        int marker=play.lastIndexOf('@');return marker<0?play:play.substring(marker+1);
    }
    static String plugin(String movie,String value){
        return value.indexOf('\u0001')>=0||value.indexOf('@')>=0?value:movie+"@"+value;
    }
    static boolean same(String left,String right){String id=raw(left);return !id.isEmpty()&&id.equals(raw(right));}
}
