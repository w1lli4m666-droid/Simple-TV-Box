package win.movie8.tv;

final class PublicIpCache {
    private String address="",network="";
    private long savedAt;
    synchronized String read(long now,String key){
        return !address.isEmpty()&&network.equals(key)&&now>=savedAt&&now-savedAt<60000?address:"";
    }
    synchronized void store(String value,long now,String key){
        if(!valid(value))throw new IllegalArgumentException("IP service did not return a public IPv4 address");
        address=value;savedAt=now;network=key;
    }
    static boolean valid(String value){
        if(value==null||!value.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}"))return false;
        String[] parts=value.split("\\.");int[] bytes=new int[4];
        for(int i=0;i<4;i++){bytes[i]=Integer.parseInt(parts[i]);if(bytes[i]>255)return false;}
        return bytes[0]!=0&&bytes[0]!=10&&bytes[0]!=127&&bytes[0]<224
            &&!(bytes[0]==169&&bytes[1]==254)&&!(bytes[0]==172&&bytes[1]>=16&&bytes[1]<=31)
            &&!(bytes[0]==192&&bytes[1]==168)&&!(bytes[0]==100&&bytes[1]>=64&&bytes[1]<=127);
    }
}
