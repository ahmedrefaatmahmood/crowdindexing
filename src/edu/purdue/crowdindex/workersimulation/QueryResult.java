package edu.purdue.crowdindex.workersimulation;

import edu.purdue.crowdindex.server.index.Node;

public class QueryResult {

    public QueryResult() {
        node = null;
        value = null;
        key = null;
    }
    private Node node;
    private Double value;
    private Double key;
    private int indexWithinNode;
    private boolean smallerBackTracking;
    public boolean isSmallerBackTracking() {
        return smallerBackTracking;
    }
    public void setSmallerBackTracking(boolean smallerBackTracking) {
        this.smallerBackTracking = smallerBackTracking;
    }
    public boolean isHigherBackTracking() {
        return higherBackTracking;
    }
    public void setHigherBackTracking(boolean higherBackTracking) {
        this.higherBackTracking = higherBackTracking;
    }
    private boolean higherBackTracking;

    public QueryResult(Double key, Double value,int indexWithInNode) {
        super();
        this.value = value;
        this.key = key;
        this.indexWithinNode  = indexWithInNode;
        higherBackTracking = false;
        smallerBackTracking = false;
    }
    public QueryResult(Node node,int indexWithInNode) {
        super();
        this.node = node;
        this.indexWithinNode  = indexWithInNode;
        higherBackTracking = false;
        smallerBackTracking = false;
    }
    public Node getNode() {
        return node;
    }
    public void setNode(Node node) {
        this.node = node;
    }
    public Double getValue() {
        return value;
    }
    public void setValue(Double value) {
        this.value = value;
    }
    public Double getKey() {
        return key;
    }
    public void setKey(Double key) {
        this.key = key;
    }
    public int getIndexWithinNode() {
        return indexWithinNode;
    }
    public void setIndexWithinNode(int indexWithinNode) {
        this.indexWithinNode = indexWithinNode;
    }

}
