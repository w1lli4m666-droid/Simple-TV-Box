package win.movie8.tv;

import org.json.JSONObject;

public final class Movie {
    public final String id, name, poster, remarks, time, year, type, area;

    public Movie(String id, String name, String poster, String remarks, String time, String year, String type, String area) {
        this.id = id; this.name = name; this.poster = poster; this.remarks = remarks;
        this.time = time; this.year = year; this.type = type; this.area=area;
    }

    public static Movie fromJson(JSONObject value) {
        String remarks=value.optString("vodRemarks", value.optString("vod_remarks"));
        if(remarks.isEmpty()) remarks=value.optString("vodVersion");
        if(remarks.isEmpty()) remarks=value.optString("vodSerial");
        return new Movie(
            value.optString("vodId", value.optString("vod_id")),
            value.optString("vodName", value.optString("vod_name")),
            value.optString("vodPic", value.optString("vod_pic")),
            remarks,
            value.optString("vodTimeAdd", value.optString("vod_time")),
            year(value),
            value.optString("vodClass", value.optString("vod_class")),
            value.optString("vodArea", value.optString("vod_area"))
        );
    }
    private static String year(JSONObject value){String result=value.optString("vodYear",value.optString("vod_year"));if(!result.isEmpty())return result;String date=value.optString("vodPubdate");return date.length()>=4?date.substring(0,4):"";}
}
