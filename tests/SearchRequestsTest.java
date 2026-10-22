package win.movie8.tv;

public final class SearchRequestsTest {
    public static void main(String[] args){
        SearchRequests requests=new SearchRequests();
        int slow=requests.next();int fast=requests.next();
        if(requests.accepts(slow)||!requests.accepts(fast))throw new AssertionError("Stale search accepted");
        requests.next();if(requests.accepts(fast))throw new AssertionError("Canceled search accepted");
        System.out.println("SearchRequestsTest passed");
    }
}
