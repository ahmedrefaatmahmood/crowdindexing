package edu.purdue.crowdindex.workersimulation;

import edu.purdue.crowdindex.server.index.Node;

public class PriorityQueueNode {
    double rank;
    double score ;
    double gValue;
    Node node;
    /**
     * this is the level of the node
     * zero is for leaf level nodes
     */
    int nodeHeight;
    public double getRank() {
        return rank;
    }
    public void setRank(double rank) {
        this.rank = rank;
    }
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public Node getNode() {
        return node;
    }
    public void setNode(Node node) {
        this.node = node;
    }
    public int getNodeHeight() {
        return nodeHeight;
    }
    public void setNodeHeight(int nodeHeight) {
        this.nodeHeight = nodeHeight;
    }

    public double getgValue() {
        return gValue;
    }
    public void setgValue(double gValue) {
        this.gValue = gValue;
    }


}
