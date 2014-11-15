package edu.purdue.crowdindex.server.index;

import edu.purdue.crowdindex.shared.control.TaskManager;
//this code was adopted from the princton libaray code
//http://algs4.cs.princeton.edu/62btrees/BTree.java.html

public class BTree<Key extends Comparable<Key>, Value> {

    private Node root; // root of the B-tree
    private int HT; // height of the B-tree
    private int N; // number of key-value pairs in the B-tree
    private int M;
    private int order;
    private TaskManager taskManager;

    // helper B-tree node data type

    // constructor
    /**
     * 
     * @param M order of the tree
     */
    public BTree(int o) {
        this.order = o;
        this.M = 2*o+2;
        root = new Node(0, this.M);

    }

    public int getHT() {
        return HT;
    }

    public void setHT(int hT) {
        HT = hT;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public BTree(TaskManager taskManager) {
        root = new Node(0, M);
        this.taskManager = taskManager;
    }

    // return number of key-value pairs in the B-tree
    public int size() {
        return N;
    }

    // return height of B-tree
    public int height() {
        return HT;
    }

    // search for given key, return associated value; return null if no such key
    public Value get(Key key) {

        return search(root, key, HT);

    }

    // search for given key, return associated value; return null if no such key
    public Value getPath(Key key, StringBuilder path) {

        return searchPath(root, key, HT, path);

    }
    // search for given key, return associated value; return null if no such key
    public Value getPathNoEq(Key key, StringBuilder path) {

        return searchPathNoEq(root, key, HT, path);

    }

    // search for given key, return associated value; return null if no such key
    public Value crowdGet(String searchItemRef) {

        return crowdSearch(root, searchItemRef, HT);

    }

    public Value search(Node x, Key key, int ht) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
                if (eq(key, children[j].key))
                    return (Value) children[j].value;
            }
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {
                if (j + 1 == x.m || less(key, children[j + 1].key))
                    return search(children[j].next, key, ht - 1);
            }
        }
        return null;
    }

    private Value searchPath(Node x, Key key, int ht, StringBuilder path) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
                if (eq(key, children[j].key)||less(key, children[j].key)) {
                    path.append(";="+j);
                    return (Value) children[j].value;

                }
            }
            path.append(";="+(x.m-1));
            return (Value) children[x.m-1].value;
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {
                if(eq(key, children[j ].key)){
                    path.append(";="+j);
                    return (Value) (children[j ].key+"");
                }
                if (j + 1 == x.m || less(key, children[j + 1].key)){
                    path.append(";"+j);
                    return searchPath(children[j].next, key, ht - 1,path);
                }
            }
        }
        return null;
    }
    private Value searchPathNoEq(Node x, Key key, int ht, StringBuilder path) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
                if (eq(key, children[j].key)||less(key, children[j].key)) {
                    path.append(";="+j);
                    return (Value) children[j].value;

                }
            }
            path.append(";="+(x.m-1));
            return (Value) children[x.m-1].value;
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {

                if (j + 1 == x.m || less(key, children[j + 1].key)||eq(key, children[j ].key)){
                    path.append(";"+j);
                    return searchPathNoEq(children[j].next, key, ht - 1,path);
                }
            }
        }
        return null;
    }


    public Value crowdSearch(Node x, String searchItemRef, int ht) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            taskManager.createleafTask(x, searchItemRef);
        }

        // internal node
        else {
            taskManager.createNonLeafTask(x, searchItemRef, ht);
        }
        return null;
    }

    // insert key-value pair
    // add code to check for duplicate keys
    public void put(Key key, Value value) {
        Node u = insert(root, key, value, HT);
        N++;
        if (u == null)
            return;

        // need to split root
        Node t = new Node(2, M);
        t.children[0] = new Entry(root.children[0].key, null, root);
        t.children[1] = new Entry(u.children[0].key, null, u);

        t.minEntry = root.minEntry;
        t.maxEntry = u.maxEntry;

        root = t;
        HT++;

    }

    private Node insert(Node h, Key key, Value value, int ht) {
        int j;
        Entry t = new Entry(key, value, null);

        // external node
        if (ht == 0) {
            for (j = 0; j < h.m; j++) {
                if (less(key, h.children[j].key))
                    break;
            }
        }

        // internal node
        else {
            for (j = 0; j < h.m; j++) {
                if ((j + 1 == h.m) || less(key, h.children[j + 1].key)) {
                    Node u = insert(h.children[j++].next, key, value, ht - 1);

                    if (u == null){
                        //adjust the boundries of the node
                        h.minEntry = h.children[0].next.minEntry;
                        h.maxEntry = h.children[h.m-1].next.maxEntry;
                        return null;
                    }
                    t.key = u.children[0].key;
                    t.next = u;
                    break;
                }
            }
        }

        for (int i = h.m; i > j; i--)
            h.children[i] = h.children[i - 1];
        h.children[j] = t;
        h.m++;
        if (h.m < M){
            //adjust boundaries of the node
            if(ht==0){//leaf node
                h.minEntry = h.children[0];
                h.maxEntry = h.children[h.m-1];
            }
            else{//non leaf node
                h.minEntry = h.children[0].next.minEntry;
                h.maxEntry = h.children[h.m-1].next.maxEntry;
            }
            return null;
        }
        else
            return split(h, ht);
    }

    // split node in half
    private Node split(Node h,int ht) {
        Node t = new Node(M / 2, M);
        //    h.m = M / 2;
        h.m =M-( M / 2);

        for (int j = 0; j < M / 2; j++){
            //  t.children[j] = h.children[M / 2 + j];
            t.children[j] = h.children[h.m + j];
        }
        //adjust bouding entries of the node
        if(ht==0)
        {
            h.maxEntry = h.children[h.m-1];
            t.minEntry = t.children[0];
            t.maxEntry = t.children[t.m-1];
        }
        else{
            h.maxEntry = h.children[h.m-1].next.maxEntry;
            t.minEntry = t.children[0].next.minEntry;
            t.maxEntry = t.children[t.m-1].next.maxEntry;
        }
        return t;
    }

    // for debugging
    @Override
    public String toString() {
        return toString(root, HT, "") + "\n";
    }

    private String toString(Node h, int ht, String indent) {
        String s = "";
        Entry[] children = h.children;

        if (ht == 0) {
            for (int j = 0; j < h.m; j++) {
                s += indent + children[j].key + " " + children[j].value + "\n";
            }
        } else {
            for (int j = 0; j < h.m; j++) {
                if (j > 0)
                    s += indent + "(" + children[j].key + ")\n";
                s += toString(children[j].next, ht - 1, indent + "     ");
            }
        }
        return s;
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean eq(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }

    /*************************************************************************
     * test client
     *************************************************************************/

}