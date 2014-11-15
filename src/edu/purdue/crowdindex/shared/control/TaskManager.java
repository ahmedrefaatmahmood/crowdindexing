package edu.purdue.crowdindex.shared.control;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import edu.purdue.crowdindex.server.index.Entry;
import edu.purdue.crowdindex.server.index.Node;
public class TaskManager {
    HashMap<String, Task> openedTasks;
    ArrayList<Task> closedTasks;
    ArrayList<Task> tasks ;
    ArrayList< ArrayList<Task>> replicatedTasks; // to assgine tasks to users
    java.sql.Connection con ;
    java.sql.Statement stmt ;
    java.sql.ResultSet rs ;

    HashMap<String, ArrayList<String>> queryResults; //to store final query results
    ArrayList<String> users ;//to store user names
    HashMap<String, ArrayList<String>> resultsPerTask; //to be used with level based approach to store results per task to get an aggregate decision
    public ArrayList<String> getUsers() {
        return users;
    }
    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }
    public HashMap<String, ArrayList<String>> getQueryResults() {
        return queryResults;
    }
    public void setQueryResults(HashMap<String, ArrayList<String>> queryResults) {
        this.queryResults = queryResults;
    }
    int taskCount;
    public TaskManager(Connection con,Statement stmt){
        //initialization of variables
        taskCount=0;
        tasks = new ArrayList<Task>();
        openedTasks = new HashMap<String, Task>();
        closedTasks = new ArrayList<Task>();
        queryResults = new HashMap<String, ArrayList<String>>();
        users = new ArrayList<String>();
        replicatedTasks = new ArrayList<ArrayList<Task>>();
        for(int i =0;i<Constants.MaxReplications;i++)
            replicatedTasks.add(new ArrayList<Task>());
        resultsPerTask = new HashMap<String, ArrayList<String>>();
        this.con = con;
        this.stmt = stmt;

    }
    public HashMap<String, ArrayList<String>> getResultsPerTask() {
        return resultsPerTask;
    }
    public void setResultsPerTask(HashMap<String, ArrayList<String>> resultsPerTask) {
        this.resultsPerTask = resultsPerTask;
    }
    public ArrayList<ArrayList<Task>> getReplicatedTasks() {
        return replicatedTasks;
    }
    public void setReplicatedTasks(ArrayList<ArrayList<Task>> replicatedTasks) {
        this.replicatedTasks = replicatedTasks;
    }
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }
    //such task will not require going back to search the index
    public void createleafTask(Node n , String searchItemRef){
        if(Constants.queryModel.equals(Constants.pathBased)){
            if(!queryResults.containsKey(searchItemRef)){
                queryResults.put(searchItemRef, new ArrayList<String>());
            }
            Task t = new Task();
            t.taskId = taskCount++;
            t.nodeLevel = 0;
            t.prompt = "please select the proper location for this item select the image it self for equal.";
            t.replications = Constants.defaultReplications;
            t.node = n;
            for(int i =0;i<n.m;i++ ){
                t.dataObjects.add(""+n.children[i].key);
            }
            t.queryIds.add(searchItemRef);
            // tasks.add(t);
            for(int i =0;i<Constants.MaxReplications;i++)
                replicatedTasks.get(i).add(t);
        }
    }
    //such task will require going deeper in the index
    public void createNonLeafTask(Node n , String searchItemRef,int level){

        if(!queryResults.containsKey(searchItemRef)){
            queryResults.put(searchItemRef, new ArrayList<String>());
        }
        Task t = new Task();
        t.taskId = taskCount++;
        t.nodeLevel = level;
        t.prompt = "please select the proper location for this item select the image it self for equal.";
        t.replications = Constants.defaultReplications;
        t.node = n;
        for(int i =0;i<n.m;i++ ){
            t.dataObjects.add(""+n.children[i].key);
        }
        t.queryIds.add(searchItemRef);
        //tasks.add(t);
        for(int i =0;i<Constants.MaxReplications;i++)
            replicatedTasks.get(i).add(t);

    }
    //such task will not require going back to search the index
    public void createleafTask(Node n , String searchItemRef,int userId){

        if(!queryResults.containsKey(searchItemRef)){
            queryResults.put(searchItemRef, new ArrayList<String>());
        }
        Task t = new Task();
        t.taskId = taskCount++;
        t.nodeLevel = 0;
        t.prompt = "please select the proper location for this item select the image it self for equal.";
        t.replications = Constants.defaultReplications;
        t.node = n;
        for(int i =0;i<n.m;i++ ){
            t.dataObjects.add(""+n.children[i].key);
        }
        t.queryIds.add(searchItemRef);
        replicatedTasks.get(userId).add(0,t);
        //tasks.add(t);

    }
    //such task will require going deeper in the index
    public void createNonLeafTask(Node n , String searchItemRef,int level,int userId){
        if(Constants.queryModel.equals(Constants.pathBased)){
            if(!queryResults.containsKey(searchItemRef)){
                queryResults.put(searchItemRef, new ArrayList<String>());
            }
            Task t = new Task();
            t.taskId = taskCount++;
            t.nodeLevel = level;
            t.prompt = "please select the proper location for this item select the image it self for equal.";
            t.replications = Constants.defaultReplications;
            t.node = n;
            for(int i =0;i<n.m;i++ ){
                t.dataObjects.add(""+n.children[i].key);
            }
            t.queryIds.add(searchItemRef);
            replicatedTasks.get(userId).add(0,t);
            //tasks.add(t);
        }
    }
    public HashMap<String, Task> getOpenedTasks() {
        return openedTasks;
    }
    public void setOpenedTasks(HashMap<String, Task> openedTasks) {
        this.openedTasks = openedTasks;
    }
    public ArrayList<Task> getClosedTasks() {
        return closedTasks;
    }
    public void setClosedTasks(ArrayList<Task> closedTasks) {
        this.closedTasks = closedTasks;
    }
    public int getTaskCount() {
        return taskCount;
    }
    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public synchronized void  addTask(Task t){
        tasks.add(t);
    }

    public void writeToDisk(String fileName){
        ///TODO wrtie to disck
    }
    public void readFromDisk(String fileName){
        ///TODO wrtie to disck
    }
    public synchronized Task getTaskbyId(int taskId){
        //TODO search tasks by id
        return new Task();
    }
    public boolean addUser(String u){
        if(!users.contains(u)){//users not in system
            users.add(u);
            return true;
        }
        else {//user is already registered
            return false;
        }


    }
    public void crowdSearch(Node x, String searchItemRef, int ht,int userId) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            createleafTask(x,searchItemRef,userId);
        }

        // internal node
        else {
            createNonLeafTask(x,searchItemRef,ht,userId);
        }

    }
    public int getReplicationPerLevel(int level){
        return Constants.MaxReplications;
    }
    public int makeDecsion (ArrayList<String> results){

        //majority voting decsion making
        int A[] = new int [100]; //100 is the max fanout for a node
        for(int i =0;i<100;i++)
            A[i]=0;
        for(String s :results)
            A[new Integer(s)] ++;
        int maxloc=-1,maxvalue =-1;
        for(int i =0;i<100;i++){
            if(A[i]>maxvalue){
                maxvalue = A[i];
                maxloc = i;
            }
        }
        return maxloc;
    }
}

