package edu.purdue.crowdindex.server.index;
public class Node {

    public  int m;                             // number of children
    public  Entry[] children ;   // the array of children
    public Entry minEntry;
    public Entry maxEntry;
    public Node(int k,int M) {
        m = k;
        children = new Entry[ M];}             // create a node with k children
}
