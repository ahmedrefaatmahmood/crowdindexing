package edu.purdue.crowdindex.server.index;
// internal nodes: only use key and next
// external nodes: only use key and value
public class Entry {
    public Comparable key;
    public  Object value;
    public  Node next;     // helper field to iterate over array entries
    public Entry(Comparable key, Object value, Node next) {
        this.key   = key;
        this.value = value;
        this.next  = next;
    }
}
