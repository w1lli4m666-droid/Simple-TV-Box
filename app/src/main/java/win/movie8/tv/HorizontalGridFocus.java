package win.movie8.tv;

final class HorizontalGridFocus {
    static int target(int position,int count,int columns,int step){
        int next=position+step;
        if(position<0||position>=count||columns<1||next<0||next>=count)return position;
        return next/columns==position/columns?next:position;
    }
}
