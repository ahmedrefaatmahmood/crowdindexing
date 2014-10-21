package edu.purdue.crowdindex.server;

import edu.purdue.crowdindex.shared.control.Constants;


public class ServerSideQueryAnswer {
    private Double key;
    private Integer indexWithinNode;
    public ServerSideQueryAnswer() {
        key = null;
        indexWithinNode =null;
    }

    public ServerSideQueryAnswer(Double key,int indexWithInNode) {
        super();
        this.key = key;
        this.indexWithinNode  = indexWithInNode;

    }
    public ServerSideQueryAnswer(int indexWithInNode) {
        super();
        this.indexWithinNode  = indexWithInNode;

    }


    public boolean isSmallerBackTracking() {
        return indexWithinNode.equals(Integer.parseInt( Constants.LESS_THAN_SUBTREE));
    }

    public boolean isHigherBackTracking() {
        return indexWithinNode.equals(Integer.parseInt( Constants.GREATER_THAN_SUBTREE));
    }
    public boolean isSmallerBoundKey() {
        return indexWithinNode.equals(Integer.parseInt( Constants.MIN_SUBTREE_KEY));
    }

    public boolean isHigherBoundKey() {
        return indexWithinNode.equals(Integer.parseInt( Constants.MAX_SUBTREE_KEY));
    }
    public boolean isKey() {
        return Math.abs(indexWithinNode)%2==1&&indexWithinNode>=1&&indexWithinNode<Integer.parseInt(Constants.MAX_SUBTREE_KEY);
    }
    public boolean isBranch() {
        return Math.abs(indexWithinNode)%2==0&&indexWithinNode>=0&&indexWithinNode<Integer.parseInt(Constants.MAX_SUBTREE_KEY);
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

