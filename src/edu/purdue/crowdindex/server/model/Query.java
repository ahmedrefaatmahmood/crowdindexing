package edu.purdue.crowdindex.server.model;

public class Query {
    int queryId;
    String dataItem;
    int indexParam;
    String queryModel;
    String expectedPath;
    int dataset;
    int solved;
    String retreivedResult;
    int solvedsofar;
    public int getSolved() {
        return solved;
    }
    public void setSolved(int solved) {
        this.solved = solved;
    }
    public String getRetreivedResult() {
        return retreivedResult;
    }
    public void setRetreivedResult(String retreivedResult) {
        this.retreivedResult = retreivedResult;
    }
    public int getSolvedsofar() {
        return solvedsofar;
    }
    public void setSolvedsofar(int solvedsofar) {
        this.solvedsofar = solvedsofar;
    }
    public int getDataset() {
        return dataset;
    }
    public void setDataset(int dataset) {
        this.dataset = dataset;
    }
    public int getQueryId() {
        return queryId;
    }
    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }
    public String getDataItem() {
        return dataItem;
    }
    public void setDataItem(String dataItem) {
        this.dataItem = dataItem;
    }
    public int getIndexParam() {
        return indexParam;
    }
    public void setIndexParam(int indexParam) {
        this.indexParam = indexParam;
    }
    public String getQueryModel() {
        return queryModel;
    }
    public void setQueryModel(String queryModel) {
        this.queryModel = queryModel;
    }
    public String getExpectedPath() {
        return expectedPath;
    }
    public void setExpectedPath(String expectedPath) {
        this.expectedPath = expectedPath;
    }
}
