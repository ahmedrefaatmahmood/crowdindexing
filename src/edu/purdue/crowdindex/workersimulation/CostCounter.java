package edu.purdue.crowdindex.workersimulation;

public class CostCounter {
    int value;

    public CostCounter() {

        this.value = 0;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
    public void increment(){
        this.value++;
    }
}
