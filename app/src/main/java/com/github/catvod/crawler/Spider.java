package com.github.catvod.crawler;

import android.content.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Dns;
import okhttp3.OkHttpClient;

/** Host API used by dynamically downloaded TVBox spiders. */
public abstract class Spider {
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static Dns safeDns() { return Dns.SYSTEM; }
    public OkHttpClient client() { return CLIENT; }
    public void init(Context context) { }
    public void init(Context context, String extend) { init(context); }
    public void destroy() { }
    public String homeContent(boolean filter) { return ""; }
    public String homeVideoContent() { return ""; }
    public String categoryContent(String tid, String page, boolean filter, HashMap<String, String> extend) { return ""; }
    public String detailContent(List<String> ids) { return ""; }
    public String searchContent(String keyword, boolean quick) { return ""; }
    public String searchContent(String keyword, boolean quick, String page) { return searchContent(keyword, quick); }
    public String playerContent(String flag, String id, List<String> vipFlags) { return ""; }
    public boolean isVideoFormat(String url) { return false; }
    public boolean manualVideoCheck() { return false; }
    public String action(String action) { return ""; }
    public Object[] proxyLocal(Map<String, String> params) { return null; }
}
