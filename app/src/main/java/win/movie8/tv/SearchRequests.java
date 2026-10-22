package win.movie8.tv;

final class SearchRequests {
    private int generation;
    int next(){return ++generation;}
    boolean accepts(int request){return generation==request;}
}
