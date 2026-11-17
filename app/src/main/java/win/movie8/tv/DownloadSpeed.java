package win.movie8.tv;

import java.util.Locale;

final class DownloadSpeed {
    private long bytes,sampledBytes,sampledAt;
    synchronized void add(int count){if(count>0)bytes+=count;}
    synchronized void begin(long now){sampledBytes=bytes;sampledAt=now;}
    synchronized String sample(long now){
        long elapsed=now-sampledAt;
        double rate=elapsed>0?(bytes-sampledBytes)*1000.0/elapsed:0;
        sampledBytes=bytes;sampledAt=now;
        return rate>=1048576?String.format(Locale.US,"%.1f MB/s",rate/1048576)
                :String.format(Locale.US,"%.0f KB/s",rate/1024);
    }
}
