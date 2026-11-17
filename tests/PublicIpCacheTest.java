package win.movie8.tv;

public final class PublicIpCacheTest {
    public static void main(String[] args){
        PublicIpCache cache=new PublicIpCache();
        require(cache.read(10,"wifi").isEmpty(),"empty");cache.store("8.8.8.8",100,"wifi");
        require(cache.read(59999,"wifi").equals("8.8.8.8"),"reuse");
        require(cache.read(60100,"wifi").isEmpty(),"expiry");
        require(cache.read(200,"mobile").isEmpty(),"network change");
        require(cache.read(99,"wifi").isEmpty(),"clock rollback");
        for(String bad:new String[]{"","::1","<html>","256.1.2.3","127.0.0.1","10.0.0.1","192.168.1.1","169.254.0.1","172.16.0.1","100.64.0.1","224.0.0.1"})require(!PublicIpCache.valid(bad),bad);
        require(PublicIpCache.valid("1.1.1.1"),"valid address");
        System.out.println("PublicIpCacheTest passed");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
