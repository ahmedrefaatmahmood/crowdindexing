package edu.purdue.crowdindex.workersimulation;

import java.util.ArrayList;
/**
 * This is a basic implementation of the priority queue
 * currently we use an array list to store nodes
 * every time to get the node with the minimum score
 * @author Ahmed
 *
 */
public class PriorityQueue {
    public enum Direction{smaller, higher}
    ArrayList<PriorityQueueNode> queue;
    int order;
    /**
     * this is the constructor of the priority queue
     */
    public PriorityQueue(){

        queue= new ArrayList<PriorityQueueNode>();
        order= Parameters.defaultOrder;
    }
    public  PriorityQueue(int order){
        queue= new ArrayList<PriorityQueueNode>();
        this.order= order;
    }
    public void insert(PriorityQueueNode node){
        queue.add(node);
    }
    public PriorityQueueNode getTop( ){
        if(queue.size()==0)
            return null;
        else {
            PriorityQueueNode toReturn = queue.get(0);
            double topScore = toReturn.getScore();
            int returnIndex=0;
            for(int i=1;i<queue.size();i++){
                if(queue.get(i).getScore()>topScore){
                    returnIndex = i;
                    topScore = queue.get(i).getScore();
                }
            }
            toReturn = queue.get(returnIndex);
            queue.remove(returnIndex);
            return toReturn;
        }

    }
    public void updateScore(double updateFactor,double boundray ,Direction direction ){
        for(int i =0;i<queue.size();i++){
            if( direction == Direction.higher&&(Double)queue.get(i).getNode().minEntry.key>=boundray){
                queue.get(i).setScore(queue.get(i).getScore()*updateFactor);
                queue.get(i).setgValue(queue.get(i).getgValue()*updateFactor);
            }
            else if( direction == Direction.smaller&&(Double)queue.get(i).getNode().minEntry.key<=boundray){
                queue.get(i).setScore(queue.get(i).getScore()*updateFactor);
                queue.get(i).setgValue(queue.get(i).getgValue()*updateFactor);
            }
        }
    }
    public void clear(){
        queue.clear();
    }
    public int size (){
        return queue.size();
    }
}
