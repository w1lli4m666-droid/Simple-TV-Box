package win.movie8.tv;

public final class DownloadSpeedTest {
    public static void main(String[] args){
        DownloadSpeed meter=new DownloadSpeed();meter.begin(1000);
        meter.add(51200);require(meter.sample(1500).equals("100 KB/s"),"half-second interval");
        require(meter.sample(2000).equals("0 KB/s"),"stalled transfer");
        meter.add(2097152);require(meter.sample(3000).equals("2.0 MB/s"),"megabytes");
        meter.add(100000);meter.begin(4000);require(meter.sample(4500).equals("0 KB/s"),"new buffering excludes old bytes");
        meter.add(-1);require(meter.sample(4500).equals("0 KB/s"),"zero interval and invalid count");
        System.out.println("DownloadSpeedTest passed");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
