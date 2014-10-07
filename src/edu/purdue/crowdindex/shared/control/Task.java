package edu.purdue.crowdindex.shared.control;
import java.util.ArrayList;

import edu.purdue.crowdindex.server.index.Node;

public class Task {
    int taskId;

    public String prompt;
    public ArrayList<String> queryIds;
    public ArrayList<String> dataObjects;
    public ArrayList<String> results;
    public String status;
    public int nodeLevel; //leaf non leaf task
    public int replications; //how many replication of the same task
    public int solvedSofar;
    public Node node;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Task() {
        queryIds = new ArrayList<String>();
        dataObjects = new ArrayList<String>();
        results = new ArrayList<String>();
        solvedSofar=0;
    }

    public ArrayList<String> getQueryIds() {
        return queryIds;
    }

    public ArrayList<String> getDataObjects() {
        return dataObjects;
    }

    public void setDataObjects(ArrayList<String> dataObjects) {
        this.dataObjects = dataObjects;
    }

    public void setQueryIds(ArrayList<String> queryIds) {
        this.queryIds = queryIds;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

}
