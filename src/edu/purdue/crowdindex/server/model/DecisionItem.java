package edu.purdue.crowdindex.server.model;

/**
 * @author Ahmed
 *
 */
public class DecisionItem implements Comparable<DecisionItem>{
    public int key;
    public double percentage;

    public int getKey() {
        return key;
    }
    public DecisionItem(int key, double percentage) {
        super();
        this.key = key;
        this.percentage = percentage;
    }
    public void setKey(int key) {
        this.key = key;
    }
    public double getPercentage() {
        return percentage;
    }
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
    @Override
    public int compareTo(DecisionItem o) {
        //at equal percentage giving priority to branches than equality at equal percentage
        //at equal percentage giving priority to branches than backtracking
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        if ( this == o ) return EQUAL;
        else if(this.percentage > o.percentage)return AFTER;
        else if(this.percentage < o.percentage)return BEFORE;
        else if(this.percentage == o.percentage){
            if((this.key==-1||this.key == 10000) &&(o.key==-1||o.key == 10000))
                return EQUAL;
            else if ((this.key==-1||this.key == 10000) &&(o.key%2 !=0))
                return AFTER;
            else if ((this.key==-1||this.key == 10000) &&(o.key%2 ==0))
                return BEFORE;
            else if ((this.key%2!=0) &&(o.key==-1||o.key == 10000))
                return BEFORE;
            else if ((this.key%2==0) &&(o.key==-1||o.key == 10000))
                return AFTER;
            else if((this.key%2==0) &&(o.key%2!=0))
                return AFTER;
            else if((this.key%2!=0) &&(o.key%2==0))
                return BEFORE;
            else return EQUAL;

        }
        return 0;
    }



}
