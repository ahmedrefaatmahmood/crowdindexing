package edu.purdue.crowdindex.workersimulation;

public class Query {
    public Query(double queryKey) {
        super();
        this.queryKey = queryKey;
    }

    double queryKey;

    public double getQueryKey() {
        return queryKey;
    }

    public void setQueryKey(double queryKey) {
        this.queryKey = queryKey;
    }
}
