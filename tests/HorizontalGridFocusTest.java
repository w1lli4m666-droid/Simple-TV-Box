package win.movie8.tv;

public final class HorizontalGridFocusTest {
    public static void main(String[] args){
        check(0,1,5,1,0);check(0,1,5,-1,0);
        check(0,3,5,1,1);check(1,3,5,-1,0);check(2,3,5,1,2);
        check(4,8,5,1,4);check(5,8,5,-1,5);check(5,8,5,1,6);
        check(7,8,5,1,7);check(1,3,2,1,1);check(2,3,2,-1,2);
        check(-1,3,5,1,-1);check(0,0,5,1,0);
        System.out.println("HorizontalGridFocusTest passed");
    }
    private static void check(int position,int count,int columns,int step,int expected){
        int actual=HorizontalGridFocus.target(position,count,columns,step);
        if(actual!=expected)throw new AssertionError(position+" / "+count+": "+actual+" != "+expected);
    }
}
