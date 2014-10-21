package edu.purdue.crowdindex.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.purdue.crowdindex.client.GreetingService;
import edu.purdue.crowdindex.server.index.BTree;
import edu.purdue.crowdindex.server.index.Node;
import edu.purdue.crowdindex.server.model.DecisionItem;
import edu.purdue.crowdindex.server.model.Query;
import edu.purdue.crowdindex.shared.control.Constants;
import edu.purdue.crowdindex.workersimulation.TraversalStrategy.MaxFreqType;

/**
 * The server side implementation of the RPC service. task types
 * breadth,depth,informed,astar,test,unlimited?
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {
    boolean indexBuild;
    ArrayList<BTree<Integer, String>> st;
    // TaskManager manger;
    Connection con1;
    Statement stmt1;
    ResultSet rs1;
    static int testReplication = 2;

    // AR static int testReplication = 10;
    // AR static int testQueryNum= 5;
    static int testQueryNum = 2;
    static int testFanout = 5;
    static int dataSet1TestTree = 5;
    static int dataSet2TestTree = 6;
    static int minRep = 1; // it should be 5 or so

    public GreetingServiceImpl() {

        // connectToDB();
        indexBuild = false;
        // manger = new TaskManager(con, stmt);
        st = new ArrayList<BTree<Integer, String>>();

        buildBtrees();
        readConfig();

        indexBuild = true;

    }

    void writeToFile(String s) {
        FileWriter fstream;
        try {
            fstream = new FileWriter("AR_Logging.txt");

            BufferedWriter out2 = new BufferedWriter(fstream);
            // PrintWriter out2 = new PrintWriter(new BufferedWriter(new
            // FileWriter(, true)));

            // out2.println(s);
            out2.write(s);

            out2.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
        }

    }

    void readConfig() { // read the configuration from the database if reset is
        // set all tasks should be cleared and read queries and
        // distribute queries among users
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            int reset = 0;
            int createTest = 0;
            con = getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select  reset, genTstTasks from crowdindex.config;");
            // //rs.close();
            while (rs.next()) {

                reset = rs.getInt("reset");
                createTest = rs.getInt("genTstTasks");

                // System.out.println("   "+firstName+" "+password);
            }
            // rs.close();
            // stmt.close();
            if (createTest == 1) {
                buildTestCases();
                st = new ArrayList<BTree<Integer, String>>();

                buildBtrees();
            }
            if (reset == 1) {
                reset();
                // stmt.execute("update crowdindex.config set reset = 0 ;");
            }
        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());

            // connectToDB();

        } finally {
            closeEveryThing(con, stmt, rs);
        }

    }

    void reset() {
        deleteTasks();
        readQueries();

    }

    void deleteTasks() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            stmt = con.createStatement();
            String statment = "delete from crowdindex.task;" + "delete from crowdindex.taskgroup;"
                    + "delete from crowdindex.backtrack;"
                    + "delete from crowdindex.expectedDistanceError;"
                    + "delete from crowdindex.taskgroupusers ;";// +
            // "update crowdindex.query set solved = 0,retreivedpath=null,responsetime = null,retrievedresult=null,solvedsofar=0,depthpaths=null,totalDecisionDepth=null ;";
            stmt.execute(statment);

        } catch (Exception e) {
            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            // connectToDB();
        } finally {
            closeEveryThing(con, stmt, rs);
        }
    }

    void readQueries() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            stmt = con.createStatement();
            ArrayList<String> expectedPathList = new ArrayList<String>();
            ArrayList<Query> queryList = new ArrayList<Query>();

            rs = stmt
                    .executeQuery("select  id,dataitem, indexparam,querymodel,expectedpath,dataset  from crowdindex.query "
                            + "where solved = 0 and querymodel <> 'insert '");
            while (rs.next()) {
                Query q = new Query();
                q.setQueryId(rs.getInt("id"));
                q.setDataItem(rs.getString("dataitem"));
                q.setIndexParam(rs.getInt("indexparam"));
                q.setQueryModel(rs.getString("querymodel"));
                q.setExpectedPath(rs.getString("expectedpath"));
                q.setDataset(rs.getInt("dataset"));
                findExpectedQueryPath(q, expectedPathList);
                queryList.add(q);
            }
            rs = stmt
                    .executeQuery("select  id,dataitem, indexparam,querymodel,expectedpath,dataset  from crowdindex.query"
                            + " where solved = 0 and querymodel = 'insert ' group by dataset ");
            while (rs.next()) {
                Query q = new Query();
                q.setQueryId(rs.getInt("id"));
                q.setDataItem(rs.getString("dataitem"));
                q.setIndexParam(rs.getInt("indexparam"));
                q.setQueryModel(rs.getString("querymodel"));
                q.setExpectedPath(rs.getString("expectedpath"));
                q.setDataset(rs.getInt("dataset"));
                findExpectedQueryPath(q, expectedPathList);
                queryList.add(q);
            }
            for (String path : expectedPathList) {
                String[] S = path.split(",");
                try {
                    stmt = con.createStatement();
                    stmt.execute("update crowdindex.query set expectedpath =  '" + S[1]
                            + "' where  id =" + S[0] + "  ;");
                    // stmt.close();
                } catch (SQLException e) {

                    e.printStackTrace();
                    writeToFile(e.getMessage());
                    writeToFile(e.getStackTrace().toString());
                    connectToDB();
                }
            }
            generateTasks(con, stmt, rs, queryList);
        } catch (Exception e) {
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            e.printStackTrace();
            // connectToDB();
        } finally {
            closeEveryThing(con, stmt, rs);
        }

    }

    void generateTasks(Connection con, Statement stmt, ResultSet rs, ArrayList<Query> queryList)
            throws Exception {
        stmt = con.createStatement();
        for (Query q : queryList) {
            int roothieght = st.get(q.getIndexParam()).height();
            int replications = getReplicationForQueryLevel(con, stmt, rs, q.getQueryId(),
                    roothieght);
            // ArrayList<Integer> users = getTaskUserList();

            if (q.getQueryModel().equals("breadth") || q.getQueryModel().equals("informed")) {
                // we need to create a taskgroup
                // insert tasks for users

                int taskgroupid = createTaskGroup(con, stmt, rs, q, roothieght, replications);
                String dataItems = getDataItems(q.getIndexParam(), "root", q.getQueryModel());
                for (int i = 0; i < replications; i++) {
                    createTaskBreadth(con, stmt, rs, q, roothieght, replications, taskgroupid,
                            dataItems, "root", q.getQueryModel());
                }
            } else if (q.getQueryModel().equals("depth")) {
                // get replications per level for query
                // select users with least number of open tasks
                // insert tasks for users
                int taskgroupid = createTaskGroup(con, stmt, rs, q, roothieght, replications);
                String dataItems = getDataItems(q.getIndexParam(), "root", q.getQueryModel());
                for (int i = 0; i < replications; i++) {
                    createTaskdepth(con, stmt, rs, q, roothieght, replications, dataItems, "root",
                            "" + taskgroupid);
                }
            } else if (q.getQueryModel().equals("test")) {
                String path = "root" + q.getExpectedPath();
                int level = 0;
                path = path.substring(0, path.indexOf(";="));
                while (true) {
                    int taskgroupid = createTaskGroup(con, stmt, rs, q, level, replications);
                    String dataItems = getDataItems(q.getIndexParam(), path, q.getQueryModel());
                    for (int k = 0; k < replications; k++) {
                        createTaskBreadth(con, stmt, rs, q, level, replications, taskgroupid,
                                dataItems, path, "test");
                    }
                    level++;
                    if (path.contains(";"))
                        path = path.substring(0, path.lastIndexOf(";"));
                    else
                        break;
                }
            } else if (q.getQueryModel().equals("insert")) {
                int taskgroupid = createTaskGroup(con, stmt, rs, q, roothieght, replications);
                String dataItems = getDataItems(q.getIndexParam(), "root", q.getQueryModel());
                String equivalentdataitems = getEquivalentDataItems(q.getIndexParam(), dataItems);
                for (int i = 0; i < replications; i++) {
                    createTaskBreadth(con, stmt, rs, q, roothieght, replications, taskgroupid,
                            equivalentdataitems, "root", q.getQueryModel());
                }
            } else if (q.getQueryModel().equals("astar")) {
                int taskgroupid = createTaskGroup(con, stmt, rs, q, roothieght, replications);
                String dataItems = getDataItems(q.getIndexParam(), "root", q.getQueryModel());
                for (int i = 0; i < replications; i++) {
                    createTaskAstar(con, stmt, rs, q, roothieght, replications, taskgroupid,
                            dataItems, "root", q.getQueryModel(), 1.0);
                }
            }
        }

    }

    String getEquivalentDataItems(int indexId, String dataItems) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String result = "";
        try {
            con = getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select  itemid  from crowdindex.insertionorder  "
                    + "where treeid = " + indexId + " and insertorder IN ("
                    + dataItems.substring(1) + ")");
            while (rs.next()) {

                result = result + "," + rs.getInt("itemid");
            }
        } catch (Exception e) {
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            e.printStackTrace();
            // connectToDB();
        } finally {
            closeEveryThing(con, stmt, rs);
        }

        return result;

    }

    // task for a certain user
    void createTaskdepth(Connection con, Statement stmt, ResultSet rs, Query q, int level,
            int replication, int userid, String dataitems, String nodepath, String taskgroup)
                    throws SQLException {
        int id = 0;

        stmt = con.createStatement();
        rs = stmt.executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.task; ");
        while (rs.next()) {
            id = rs.getInt("maxid");
        }

        stmt = con.createStatement();
        String statment = "insert into crowdindex.task (id,level,query,replications,nodepath,expected_result,user,indexparam,queryModel,dataItems,creationTime,taskstatus,queryItem,dataset,taskgroup) "
                + "values ("
                + id
                + ","
                + level
                + ","
                + q.getQueryId()
                + ","
                + replication
                + ",'"
                + nodepath
                + "','"
                + q.getExpectedPath()
                + "',"
                + userid
                + ","
                + q.getIndexParam()
                + ",'depth','"
                + dataitems
                + "',CURRENT_TIMESTAMP,0,'"
                + q.getDataItem()
                + "',"
                + q.getDataset() + "," + taskgroup + "); ";
        stmt.execute(statment);

    }

    void createTaskdepth(Connection con, Statement stmt, ResultSet rs, Query q, int level,
            int replication, String dataitems, String nodepath, String taskgroup) throws Exception {
        int id = 0;

        // Connection con = null;
        // Statement stmt=null;
        // ResultSet rs=null;
        // try {
        // con = getConnection();
        stmt = con.createStatement();

        rs = stmt.executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.task; ");
        while (rs.next()) {
            id = rs.getInt("maxid");
        }
        // rs.close();
        // stmt.close();
        stmt = con.createStatement();
        String statment = "insert into crowdindex.task (id,level,query,replications,nodepath,expected_result,indexparam,queryModel,dataItems,creationTime,taskstatus,queryItem,dataset,taskgroup) "
                + "values ("
                + id
                + ","
                + level
                + ","
                + q.getQueryId()
                + ","
                + replication
                + ",'"
                + nodepath
                + "','"
                + q.getExpectedPath()
                + "',"
                + q.getIndexParam()
                + ",'depth','"
                + dataitems
                + "',CURRENT_TIMESTAMP,0,'"
                + q.getDataItem()
                + "',"
                + q.getDataset()
                + ","
                + taskgroup + "); ";
        stmt.execute(statment);
        // stmt.close();

        // } catch (Exception e) {
        //
        // e.printStackTrace();
        // // connectToDB();
        // }
        // finally{
        // closeEveryThing(con, stmt, rs);
        // }
    }

    void createTaskBreadth(Connection con, Statement stmt, ResultSet rs, Query q, int level,
            int replication, int taskgroupid, int userid, String dataitems, String nodepath,
            String taskModel) throws Exception {
        int id = 0;
        // / Connection con = null;
        // Statement stmt=null;
        // ResultSet rs=null;

        // con = getConnection();
        stmt = con.createStatement();

        rs = stmt.executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.task; ");
        while (rs.next()) {
            id = rs.getInt("maxid");
        }
        // rs.close();
        // stmt.close();
        String statment = "insert into crowdindex.task (id,level,query,replications,nodepath,expected_result,user,indexparam,taskgroup,queryModel,dataItems,creationTime,taskstatus,queryItem,dataset) "
                + "values ("
                + id
                + ","
                + level
                + ","
                + q.getQueryId()
                + ","
                + replication
                + ",'"
                + nodepath
                + "','"
                + q.getExpectedPath()
                + "',"
                + userid
                + ","
                + q.getIndexParam()
                + ","
                + taskgroupid
                + ",'"
                + taskModel
                + "','"
                + dataitems
                + "',CURRENT_TIMESTAMP,0,'"
                + q.getDataItem() + "'," + q.getDataset() + " ); ";

        stmt = con.createStatement();
        stmt.execute(statment);
        // stmt.close();

    }

    void createTaskBreadth(Connection con, Statement stmt, ResultSet rs, Query q, int level,
            int replication, int taskgroupid, String dataitems, String nodepath, String taskModel)
                    throws Exception {
        int id = 0;
        // Connection con = null;
        // Statement stmt=null;
        // ResultSet rs=null;
        // try {
        // con = getConnection();
        stmt = con.createStatement();

        rs = stmt.executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.task; ");
        while (rs.next()) {
            id = rs.getInt("maxid");
        }
        // rs.close();
        // stmt.close();
        String statment = "insert into crowdindex.task (id,level,query,replications,nodepath,expected_result,indexparam,taskgroup,queryModel,dataItems,creationTime,taskstatus,queryItem,dataset) "
                + "values ("
                + id
                + ","
                + level
                + ","
                + q.getQueryId()
                + ","
                + replication
                + ",'"
                + nodepath
                + "','"
                + q.getExpectedPath()
                + "',"
                + q.getIndexParam()
                + ","
                + taskgroupid
                + ",'"
                + taskModel
                + "','"
                + dataitems
                + "',CURRENT_TIMESTAMP,0,'"
                + q.getDataItem()
                + "'," + q.getDataset() + " ); ";

        stmt = con.createStatement();
        stmt.execute(statment);
        // stmt.close();

        // } catch (Exception e) {
        //
        // e.printStackTrace();
        // // connectToDB();
        //
        // }
        // finally{
        // closeEveryThing(con, stmt, rs);
        // }
    }

    void createTaskAstar(Connection con, Statement stmt, ResultSet rs, Query q, int level,
            int replication, int taskgroupid, String dataitems, String nodepath, String taskModel,
            Double gvalue) throws Exception {
        int id = 0;
        // Connection con = null;
        // Statement stmt=null;
        // ResultSet rs=null;
        // try {
        // con = getConnection();
        stmt = con.createStatement();

        rs = stmt.executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.task; ");
        while (rs.next()) {
            id = rs.getInt("maxid");
        }
        // rs.close();
        // stmt.close();
        String statment = "insert into crowdindex.task (id,level,query,replications,nodepath,expected_result,indexparam,taskgroup,queryModel,dataItems,creationTime,taskstatus,queryItem,dataset,gAstar) "
                + "values ("
                + id
                + ","
                + level
                + ","
                + q.getQueryId()
                + ","
                + replication
                + ",'"
                + nodepath
                + "','"
                + q.getExpectedPath()
                + "',"
                + q.getIndexParam()
                + ","
                + taskgroupid
                + ",'"
                + taskModel
                + "','"
                + dataitems
                + "',CURRENT_TIMESTAMP,0,'"
                + q.getDataItem()
                + "'," + q.getDataset() + "" + "," + gvalue + " ); ";

        stmt = con.createStatement();
        stmt.execute(statment);
        // stmt.close();

        // } catch (Exception e) {
        //
        // e.printStackTrace();
        // // connectToDB();
        //
        // }
        // finally{
        // closeEveryThing(con, stmt, rs);
        // }
    }

    String getDataItems(int treeindex, String nodepath, String taskType) {
        String result = "";
        if ("root".equals(nodepath)) {

            for (int i = 0; i < st.get(treeindex).getRoot().m; i++) {
                result = result + "," + st.get(treeindex).getRoot().children[i].key;
            }
            if (taskType.equals("astar")) {
                result = result + "," + st.get(treeindex).getRoot().maxEntry.key;
            }
        } else {
            nodepath = nodepath.substring(nodepath.indexOf("root") + 5);
            return getDataItems(st.get(treeindex).getRoot(), nodepath, taskType);
        }
        return result;
    }

    String getDataItems(Node n, String nodepath, String taskType) {
        String result = "";
        if (nodepath.contains(";")) {
            String firstPart = nodepath.substring(0, nodepath.indexOf(";"));
            String remainingPart = nodepath.substring(nodepath.indexOf(";") + 1);
            return getDataItems(n.children[new Integer(firstPart)].next, remainingPart, taskType);
        } else if (nodepath.startsWith("=")) {
            String index = nodepath.substring(1);
            return "" + n.children[new Integer(index)].key;
        } else if (nodepath != null && !"".equals(nodepath)) {
            return getDataItems(n.children[new Integer(new Integer(nodepath))].next, "", taskType);
        } else {

            for (int i = 0; i < n.m; i++) {
                result = result + "," + n.children[i].key;
            }
            if (taskType.equals("astar")) {
                result = result + "," + n.maxEntry.key;
            }
        }
        return result;
    }

    int createTaskGroup(Connection con, Statement stmt, ResultSet rs, Query q, int level,
            int replication) throws Exception {
        int id = 0;
        // Connection con = null;
        // Statement stmt=null;
        // ResultSet rs=null;
        // try {
        // con = getConnection();

        stmt = con.createStatement();
        rs = stmt.executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.taskgroup; ");
        while (rs.next()) {
            id = rs.getInt("maxid");
        }
        // rs.close();
        // stmt.close();
        stmt = con.createStatement();
        String statment = "insert into crowdindex.taskgroup (id,queryid,level,replicationnum,expectedresult,creatationtime,respondsofar) values ("
                + id
                + ","
                + q.getQueryId()
                + ","
                + level
                + ","
                + replication
                + ",'"
                + q.getExpectedPath() + "',CURRENT_TIMESTAMP,0 ); ";
        stmt.execute(statment);
        // stmt.close();

        // } catch (Exception e) {
        //
        // e.printStackTrace();
        // // connectToDB();
        //
        // }
        // finally{
        // closeEveryThing(con, stmt, rs);
        // }
        return id;
    }

    Query getQueryById(String queryId) {
        Query q = new Query();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            stmt = con.createStatement();

            // ArrayList<E>
            String statment = "select  id,dataitem,indexparam,querymodel,expectedpath,dataset,solved,solvedsofar,retrievedresult from crowdindex.query where id = "
                    + queryId + ";";
            rs = stmt.executeQuery(statment);
            while (rs.next()) {

                q.setQueryId(rs.getInt("id"));
                q.setDataItem(rs.getString("dataitem"));
                q.setIndexParam(rs.getInt("indexparam"));
                q.setQueryModel(rs.getString("querymodel"));
                q.setExpectedPath(rs.getString("expectedpath"));
                if (q.getExpectedPath() == null)
                    q.setExpectedPath("");
                q.setDataset(rs.getInt("dataset"));
                q.setSolved(rs.getInt("solved"));
                q.setSolvedsofar(rs.getInt("solvedsofar"));
                q.setRetreivedResult(rs.getString("retrievedresult"));
                if (q.getRetreivedResult() == null)
                    q.setRetreivedResult("");

                // System.out.println("   "+firstName+" "+password);
            }

        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            // connectToDB();

        } finally {
            closeEveryThing(con, stmt, rs);
        }
        return q;

    }

    ArrayList<Integer> getTaskUserList() {
        // will return a list of current users sorted by number of open tasks
        ArrayList<Integer> userIdList = new ArrayList<Integer>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        // String reply="error" ;
        try {
            con = getConnection();
            stmt = con.createStatement();

            rs = stmt.executeQuery("select  id from crowdindex.users order by opentasks ");
            while (rs.next()) {
                int id = rs.getInt("id");
                userIdList.add(id);
                // System.out.println("   "+firstName+" "+password);
            }
            // rs.close();
            // stmt.close();
        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            // connectToDB();

        } finally {
            closeEveryThing(con, stmt, rs);
        }
        return userIdList;

    }

    int getReplicationForQueryLevel(Connection con, Statement stmt, ResultSet rs, int queryId,
            int level) throws Exception {
        int result = 5;// defualt replications

        con = getConnection();
        stmt = con.createStatement();
        String statment = "select  replication from crowdindex.replicationperlevel where query = "
                + queryId + " and ( level ='all' or level = " + level + ");";
        rs = stmt.executeQuery(statment);
        while (rs.next()) {
            result = rs.getInt("replication");

            // System.out.println("   "+firstName+" "+password);
        }

        return result;

    }

    void findExpectedQueryPath(Query q, ArrayList<String> expectedPathsList) {
        StringBuilder path = new StringBuilder();
        String value;
        int indexparam = q.getIndexParam();
        if (!"test".equals(q.getQueryModel()))
            value = st.get(indexparam).getPath(new Integer(q.getDataItem()), path);
        else
            value = st.get(indexparam).getPathNoEq(new Integer(q.getDataItem()), path);
        q.setExpectedPath(path.toString());
        System.out.println("value = " + value);
        System.out.println("path = " + path.toString());
        expectedPathsList.add("" + q.getQueryId() + "," + path.toString());

    }

    void buildBtrees() {
        st.add(new BTree<Integer, String>(4));// dummy insertion at index 0
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        int i = 1;
        try {
            con = getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select id,fanout,dataset,descrip from crowdindex.indexparam;");
            while (rs.next()) {
                int fanout = new Integer(rs.getString("fanout"));
                int dataset = rs.getInt("dataset");
                String description = rs.getString("descrip");
                BTree<Integer, String> tree = new BTree<Integer, String>(fanout);
                builtTreeFromDataSet(tree, dataset, description, i);
                st.add(tree);
                i++;
                // System.out.println("   "+firstName+" "+password);
            }
            // rs.close();
            // stmt.close();

        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            // connectToDB();

        } finally {
            closeEveryThing(con, stmt, rs);
        }

    }

    void builtTreeFromDataSet(BTree<Integer, String> tree, int dataset, String description,
            int treeIndex) {
        if ("query".equals(description)) {
            if (dataset == 1) {// squares data set
                for (int i = 1; i <= 149; i++)
                    // for (int i = 1; i <= 5; i++)
                    tree.put(new Integer(i), "squareimages" + "/(" + i + ").jpg");
            } else {
                for (int i = 96; i <= 1296; i++)
                    tree.put(new Integer(i), "usedcarimages2" + "/(" + i + ").jpg");
            }
        } else if ("insert".equals(description)) {
            tree.put(1, "1");
        }
    }

    void connectToDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            con1 = DriverManager.getConnection(
                    // "jdbc:mysql://localhost:3306/crowdindex?allowMultiQueries=true",
                    // "root", "1234");
                    "jdbc:mysql://localhost:3306/crowdindex", "root", "1234");
            stmt1 = con1.createStatement();

        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());

        }

    }

    public Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/crowdindex?allowMultiQueries=true", "root", "1234");
        // return DriverManager
        // .getConnection(
        // "jdbc:mysql://localhost:10159/crowdindex?allowMultiQueries=true",
        // "root", "1234");

    }

    void closeEveryThing(Connection con, Statement stmt, ResultSet res) {
        if (res != null)
            try {
                res.close();
            } catch (SQLException logOrIgnore) {
                writeToFile(logOrIgnore.getMessage());
                writeToFile(logOrIgnore.getStackTrace().toString());
            }
        if (stmt != null)
            try {
                stmt.close();
            } catch (SQLException logOrIgnore) {
                writeToFile(logOrIgnore.getMessage());
                writeToFile(logOrIgnore.getStackTrace().toString());
            }
        if (con != null)
            try {
                con.close();
            } catch (SQLException logOrIgnore) {
                writeToFile(logOrIgnore.getMessage());
                writeToFile(logOrIgnore.getStackTrace().toString());
            }

    }

    @Override
    public String getTask(String input) throws IllegalArgumentException {
        // this function requests a task from the server
        // the task is send to the client as a string
        // the format of task is
        // taskid;queryitem;taskitem1,taskitem2,taskitem3,....
        // the task is removed from the unopned tasks to closed tasks
        // Verify that the input is valid.

        String[] s = input.split(";");
        String userId = s[0];
        String preivousTaskId = s[1];
        String previousTaskType = s[2];
        String previousQUeryId = s[3];

        String newTaskId = "";
        String newqueryId = "";
        String newqueryItem = "";
        String newTaskType = "";
        String newDataItems = "";
        String newDataSet = "";
        String newTaskGroup = "";

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        // String reply="error" ;
        String toSend = "";
        try {
            con = getConnection();
            con.setAutoCommit(false);

            if ("depth".equals(previousTaskType)) {
                // we need to get the next task of the same query

                stmt = con.createStatement();
                rs = stmt
                        .executeQuery("select  id,query,queryItem,dataItems,queryModel,dataset,taskgroup from crowdindex.task where user = "
                                + userId
                                + " and query = "
                                + previousQUeryId
                                + " and( taskstatus =0 or taskstatus =1) order by query, id;");

                while (rs.next()) {

                    newTaskId = "" + rs.getInt("id");
                    newqueryId = "" + rs.getInt("query");
                    newqueryItem = rs.getString("queryItem");
                    newDataItems = rs.getString("dataItems");
                    newTaskType = rs.getString("queryModel");
                    newDataSet = "" + rs.getInt("dataset");
                    newTaskGroup = "" + rs.getInt("taskgroup");
                    break;
                    // System.out.println("   "+firstName+" "+password);
                }
                // rs.close();
                // stmt.close();
                if (newTaskId == null || "".equals(newTaskId) || "null".equals(newTaskId)) {
                    stmt = con.createStatement();
                    rs = stmt
                            .executeQuery("select  id,query,queryItem,dataItems,queryModel,dataset,taskgroup from crowdindex.task where user = "
                                    + userId
                                    + "  and ( taskstatus =0 or taskstatus =1) order by query, id;");
                    // //rs.close();
                    while (rs.next()) {

                        newTaskId = "" + rs.getInt("id");
                        newqueryId = "" + rs.getInt("query");
                        newqueryItem = rs.getString("queryItem");
                        newDataItems = rs.getString("dataItems");
                        newTaskType = rs.getString("queryModel");
                        newDataSet = "" + rs.getInt("dataset");
                        newTaskGroup = "" + rs.getInt("taskgroup");
                        break;
                        // System.out.println("   "+firstName+" "+password);
                    }
                    // rs.close();
                    // stmt.close();
                }
                if (newTaskId == null || "".equals(newTaskId) || "null".equals(newTaskId)) {
                    stmt = con.createStatement();
                    rs = stmt
                            .executeQuery("select  id,query,queryItem,dataItems,queryModel,dataset,taskgroup from crowdindex.task t where user is null  and taskstatus =0  and "
                                    + userId
                                    + " not in(select user from crowdindex.taskgroupusers u where t.taskgroup = u.taskgroup) order by query,id;");
                    // //rs.close();
                    while (rs.next()) {

                        newTaskId = "" + rs.getInt("id");
                        newqueryId = "" + rs.getInt("query");
                        newqueryItem = rs.getString("queryItem");
                        newDataItems = rs.getString("dataItems");
                        newTaskType = rs.getString("queryModel");
                        newDataSet = "" + rs.getInt("dataset");
                        newTaskGroup = "" + rs.getInt("taskgroup");
                        break;
                        // System.out.println("   "+firstName+" "+password);
                    }
                    // rs.close();
                    // stmt.close();
                }
                toSend = newTaskId + ";" + newqueryId + ";" + newqueryItem + ";" + newDataItems
                        + ";" + newTaskType + ";" + newDataSet;
                if ("2".equals(newDataSet)) {
                    toSend = toSend + ";" + getDataSetItemDetails(newqueryItem);
                    String[] ss = newDataItems.split(",");
                    for (String sss : ss) {
                        if (sss != null && !"".equals(sss))
                            toSend = toSend + "@" + getDataSetItemDetails(sss);
                    }
                }
                if ("".equals(newTaskId))
                    toSend = "No  tasks for now";
                else {
                    stmt = con.createStatement();
                    String statment = "REPLACE INTO crowdindex.taskgroupusers SET user =" + userId
                            + ", taskgroup = " + newTaskGroup + ";";
                    stmt.execute(statment);
                    // stmt.close();
                    stmt = con.createStatement();
                    statment = " update crowdindex.task set taskstatus = 1, user=" + userId
                            + " where id = " + newTaskId + ";";
                    stmt.execute(statment);
                    // stmt.close();
                }

            } else {

                stmt = con.createStatement();
                rs = stmt
                        .executeQuery("select  id,query,queryItem,dataItems,queryModel,dataset,taskgroup from crowdindex.task where user = "
                                + userId
                                + "  and ( taskstatus =0 or taskstatus =1)  order by query,id;");

                while (rs.next()) {

                    newTaskId = "" + rs.getInt("id");
                    newqueryId = "" + rs.getInt("query");
                    newqueryItem = rs.getString("queryItem");
                    newDataItems = rs.getString("dataItems");
                    newTaskType = rs.getString("queryModel");
                    newDataSet = "" + rs.getInt("dataset");
                    newTaskGroup = "" + rs.getInt("taskgroup");
                    break;
                    // System.out.println("   "+firstName+" "+password);
                }

                if (newTaskId == null || "".equals(newTaskId) || "null".equals(newTaskId)) {
                    stmt = con.createStatement();
                    rs = stmt
                            .executeQuery("select  id,query,queryItem,dataItems,queryModel,dataset,taskgroup from crowdindex.task t where user is null  and taskstatus =0  and "
                                    + userId
                                    + " not in(select user from crowdindex.taskgroupusers u where t.taskgroup = u.taskgroup) order by query, id;");
                    // //rs.close();
                    while (rs.next()) {

                        newTaskId = "" + rs.getInt("id");
                        newqueryId = "" + rs.getInt("query");
                        newqueryItem = rs.getString("queryItem");
                        newDataItems = rs.getString("dataItems");
                        newTaskType = rs.getString("queryModel");
                        newDataSet = "" + rs.getInt("dataset");
                        newTaskGroup = "" + rs.getInt("taskgroup");

                        break;
                        // System.out.println("   "+firstName+" "+password);
                    }
                }
                // rs.close();
                // stmt.close();
                toSend = newTaskId + ";" + newqueryId + ";" + newqueryItem + ";" + newDataItems
                        + ";" + newTaskType + ";" + newDataSet;
                if ("2".equals(newDataSet)) {
                    toSend = toSend + ";" + getDataSetItemDetails(newqueryItem);
                    String[] ss = newDataItems.split(",");
                    for (String sss : ss) {
                        if (sss != null && !"".equals(sss))
                            toSend = toSend + "@" + getDataSetItemDetails(sss);
                    }
                } else {
                    toSend = toSend + ";_";
                }
                if ("informed".equals(newTaskType)) { // we need to show the
                    // selection of other
                    // workers
                    String frequenceies = getInformedTaskFrequenceis(newTaskGroup);
                    toSend = toSend + ";" + frequenceies;
                }

                if ("".equals(newTaskId))
                    toSend = "No  tasks for now";
                else {
                    stmt = con.createStatement();
                    String statment = "REPLACE INTO crowdindex.taskgroupusers SET user =" + userId
                            + ", taskgroup = " + newTaskGroup
                            + "; update crowdindex.task set taskstatus = 1, user=" + userId
                            + " where id = " + newTaskId + ";";
                    stmt.execute(statment);
                    // stmt.close();
                }

            }
            con.commit();
        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                writeToFile(e1.getMessage());
                writeToFile(e1.getStackTrace().toString());
            }
            // connectToDB();

        } finally {
            closeEveryThing(con, stmt, rs);
        }
        // input string

        return toSend;
    }

    String getInformedTaskFrequenceis(String taskgroupId) {
        String result = "";
        int count = 0;
        String value = "";
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            stmt = con.createStatement();

            rs = stmt
                    .executeQuery("select returned_result,count(*)  c from crowdindex.task where taskgroup ="
                            + taskgroupId + " group by returned_result; ");
            while (rs.next()) {
                count = rs.getInt("c");

                value = rs.getString("returned_result");

                if (value != null && !"null".equals(value))

                    result = result + "," + value + "@" + count;

            }
            // rs.close();
            // stmt.close();
        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            // connectToDB();
        } finally {
            closeEveryThing(con, stmt, rs);
        }
        return result;

    }

    String getDataSetItemDetails(String id) {
        String result = "";
        int numPic = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            stmt = con.createStatement();

            rs = stmt
                    .executeQuery("select  id,picNum,price,title from crowdindex.datasetinfo where id = "
                            + id + ";");
            while (rs.next()) {
                numPic = rs.getInt("picNum");
                result = result + numPic;
                result = result + "'" + rs.getString("price");

                result = result + "'" + rs.getString("title");

            }
            // rs.close();
            // stmt.close();
        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            // connectToDB();
        } finally {
            closeEveryThing(con, stmt, rs);
        }
        return result;

    }

    @Override
    public String returnResult(String input) throws IllegalArgumentException {
        // in this function the result is returned from the client
        // result is result as a string result format is
        // taskid;userName;resultindex
        // remove task from open tasks to closed tasks
        // create tasks if neccessary
        String[] s = input.split(";");
        String userId = s[1];
        String taskType = s[3];

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String reply = "Result send successfully";
        try {
            con = getConnection();
            con.setAutoCommit(false);
            stmt = con.createStatement();
            if ("breadth".equals(taskType) || "informed".equals(taskType)) {
                reply = handleBreadth(con, stmt, rs, input);
            } else if ("depth".equals(taskType)) {
                reply = handleDepth(con, stmt, rs, input);
            } else if ("astar".equals(taskType)) {
                reply = handleAStar(con, stmt, rs, input);
            } else if ("test".equals(taskType)) {
                reply = handleTest(con, stmt, rs, input);
            }
            // increment the number of completer tasks for a worker
            stmt = con.createStatement();
            String statment = "update crowdindex.users set completedtasks= completedtasks+1 where id =  "
                    + userId + ";";
            stmt.execute(statment);

            /* We need to check the number of completed tasks of a user */

            con.commit();
        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                writeToFile(e1.getMessage());
                writeToFile(e1.getStackTrace().toString());
            }
            reply = "Error in the previous result please debug";

        } finally {
            closeEveryThing(con, stmt, rs);
        }
        return reply;
    }
    String handleBreadth(Connection con, Statement stmt, ResultSet rs, String input) throws Exception {
        // get task info to get task group
        // update the task to set end time stamp\
        // get task group
        // update task group
        // create new tasks if necessary
        // update query if necessary
        String reply = "Result send successfully";
        String[] s = input.split(";");
        String taskId = s[0];
        String userId = s[1];
        String result = s[2];
        String taskType = s[3];

        String taskgroup = "";
        String statment = "";
        String nodepath = "";
        int level = 0;
        String queryId = "";

        int treeIndex = 1;

        rs = stmt
                .executeQuery("select  id,taskgroup,nodepath,indexparam,level,query from crowdindex.task where id = "
                        + taskId + ";");
        while (rs.next()) {
            taskgroup = "" + rs.getInt("taskgroup");
            nodepath = rs.getString("nodepath");
            treeIndex = rs.getInt("indexparam");
            level = rs.getInt("level");
            queryId = "" + rs.getInt("query");
        }
        // rs.close();
        // stmt.close();
        stmt = con.createStatement();
        statment = "update crowdindex.task set returned_result= " + result
                + ", responseTime = CURRENT_TIMESTAMP, taskstatus =2 where id = " + taskId
                + ";";
        stmt.execute(statment);
        // stmt.close();
        statment = "update crowdindex.taskgroup set resultssofar= concat(ifnull( resultssofar,''),';"
                + result
                + "') , respondsofar =  respondsofar+1 where id = "
                + taskgroup
                + ";";
        stmt = con.createStatement();
        stmt.execute(statment);
        // stmt.close();
        stmt = con.createStatement();
        rs = stmt
                .executeQuery("select  id ,resultssofar,respondsofar,replicationnum from crowdindex.taskgroup where id = "
                        + taskgroup + ";");
        int replication = 5;
        int solvedsofar = 0;
        String resultSofar = "";

        while (rs.next()) {
            resultSofar = rs.getString("resultssofar");
            if (resultSofar == null)
                resultSofar = "";
            solvedsofar = rs.getInt("respondsofar");
            replication = rs.getInt("replicationnum");

        }

        if (solvedsofar >= replication) {
            // a decision can be made for taskgroup
            // either make new tasks and a task group
            // or consider the query as solved
            int decsion = makeDecsion(resultSofar);

            if (decsion % 2 == 0) { // no equality will go down the
                // index
                if (level == 0) {// this is a leaf task and you get
                    // the final
                    // result and no more search for this item
                    nodepath = nodepath + ";=" + decsion / 2;
                    String resultQuery = getDataItems(treeIndex, nodepath, taskType);
                    stmt = con.createStatement();
                    statment = "update crowdindex.query set responsetime= CURRENT_TIMESTAMP, retrievedresult='"
                            + resultQuery
                            + "' ,solved=1, retreivedpath='"
                            + nodepath
                            + "'  where id = " + queryId + ";";
                    stmt.execute(statment);
                    // stmt.close();

                } else {
                    String nodepath2 = nodepath + ";" + decsion / 2;
                    String dataItems = getDataItems(treeIndex, nodepath2, taskType);
                    String[] s2 = dataItems.substring(1).split(",");
                    if (s2 != null && s2.length == 1) {
                        nodepath = nodepath + ";=" + decsion / 2;
                        String resultQuery = getDataItems(treeIndex, nodepath, taskType);
                        stmt = con.createStatement();
                        statment = "update crowdindex.query set responsetime= CURRENT_TIMESTAMP, retrievedresult='"
                                + resultQuery
                                + "' ,solved=1, retreivedpath='"
                                + nodepath
                                + "'  where id = " + queryId + ";";
                        stmt.execute(statment);
                    } else {
                        // create tasks and task group
                        Query q = getQueryById(queryId);
                        int newReplication = getReplicationForQueryLevel(con, stmt, rs,
                                new Integer(queryId), level - 1);
                        int newtaskgourpid = createTaskGroup(con, stmt, rs, q, level - 1,
                                newReplication);
                        // ArrayList<Integer> users = getTaskUserList();
                        for (int i = 0; i < newReplication; i++) {
                            createTaskBreadth(con, stmt, rs, q, level - 1, newReplication,
                                    newtaskgourpid, dataItems, nodepath2, taskType);
                        }
                    }

                }
                // manger.getClosedTasks().add(t);
            } else { // equality of an item will not go furth in the
                // index
                // for this
                // task and just get a new task
                nodepath = nodepath + ";=" + (((decsion - 1) / 2) + 1);
                String resultQuery = getDataItems(treeIndex, nodepath, taskType);
                stmt = con.createStatement();
                statment = "update crowdindex.query set responsetime= CURRENT_TIMESTAMP, retrievedresult='"
                        + resultQuery
                        + "' ,solved=1, retreivedpath='"
                        + nodepath
                        + "'  where id = " + queryId + ";";
                stmt.execute(statment);
                // stmt.close();
            }

        }

        return reply;
    }
    String handleDepth(Connection con, Statement stmt, ResultSet rs, String input) throws Exception {
        String reply = "Result send successfully";
        String[] s = input.split(";");
        String taskId = s[0];
        String userId = s[1];
        String result = s[2];
        String taskType = s[3];

        // get task info
        // update the task to set end time stamp
        // create new task if necessary
        // update query if necessary



        String statment = "";
        String nodepath = "";
        String taskgroup = "";
        int level = 0;
        String queryId = "";

        int treeIndex = 1;
        stmt = con.createStatement();
        rs = stmt
                .executeQuery("select id,nodepath,taskgroup,indexparam,level,query from crowdindex.task where id = "
                        + taskId + ";");
        while (rs.next()) {
            nodepath = rs.getString("nodepath");
            taskgroup = "" + rs.getInt("taskgroup");
            treeIndex = rs.getInt("indexparam");
            level = rs.getInt("level");
            queryId = "" + rs.getInt("query");
        }
        // rs.close();
        // stmt.close();
        stmt = con.createStatement();
        statment = "update crowdindex.task set returned_result= " + result
                + ", responseTime = CURRENT_TIMESTAMP, taskstatus =2 where id = " + taskId
                + ";";
        stmt.execute(statment);
        // stmt.close();
        Query q = getQueryById(queryId);
        int decsion = new Integer(result);
        int queryReplication = getReplicationForQueryLevel(con, stmt, rs, new Integer(
                queryId), 0);

        if (decsion % 2 == 0) { // no equality will go down the index
            if (level == 0) {// this is a leaf task and you get
                // the final
                // result and no more search for this item
                nodepath = nodepath + ";=" + decsion / 2;
                String resultQuery = getDataItems(treeIndex, nodepath, taskType);
                if (q.getSolvedsofar() + 1 == queryReplication) {
                    int totalDecision = makeDecsion("" + q.getRetreivedResult() + ";"
                            + resultQuery);
                    statment = "update crowdindex.query set responsetime= CURRENT_TIMESTAMP, depthpaths=concat(ifnull( depthpaths,''),';"
                            + nodepath
                            + "') , retrievedresult=concat(ifnull( retrievedresult,''),';"
                            + resultQuery
                            + "') ,totalDecisionDepth='"
                            + totalDecision
                            + "' ,solved=1, solvedsofar='"
                            + (q.getSolvedsofar() + 1)
                            + "'  where id = " + queryId + ";";
                } else {
                    statment = "update crowdindex.query set  retrievedresult=concat(ifnull( retrievedresult,''),';"
                            + resultQuery
                            + "'), depthpaths=concat( ifnull(depthpaths,''),';"
                            + nodepath
                            + "')  , solvedsofar='"
                            + (q.getSolvedsofar() + 1)
                            + "'  where id = " + queryId + ";";
                }
                stmt = con.createStatement();
                stmt.execute(statment);
                // stmt.close();

            } else {
                String nodepath2 = nodepath + ";" + decsion / 2;
                String dataItems = getDataItems(treeIndex, nodepath2, taskType);
                String[] s2 = dataItems.substring(1).split(",");
                if (s2 != null && s2.length == 1) {
                    nodepath = nodepath + ";=" + decsion / 2;
                    String resultQuery = getDataItems(treeIndex, nodepath, taskType);
                    if (q.getSolvedsofar() + 1 == queryReplication) {
                        int totalDecision = makeDecsion("" + q.getRetreivedResult() + ";"
                                + resultQuery);
                        statment = "update crowdindex.query set responsetime= CURRENT_TIMESTAMP, depthpaths=concat(ifnull( depthpaths,''),';"
                                + nodepath
                                + "') , retrievedresult=concat(ifnull( retrievedresult,''),';"
                                + resultQuery
                                + "') ,totalDecisionDepth='"
                                + totalDecision
                                + "' ,solved=1, solvedsofar='"
                                + (q.getSolvedsofar() + 1)
                                + "'  where id = " + queryId + ";";
                    } else {
                        statment = "update crowdindex.query set  retrievedresult=concat(ifnull( retrievedresult,''),';"
                                + resultQuery
                                + "'), depthpaths=concat( ifnull(depthpaths,''),';"
                                + nodepath
                                + "')  , solvedsofar='"
                                + (q.getSolvedsofar() + 1)
                                + "'  where id = "
                                + queryId
                                + ";";
                    }
                    stmt = con.createStatement();
                    stmt.execute(statment);
                    // create tasks and task group
                } else
                    createTaskdepth(con, stmt, rs, q, level - 1, queryReplication,
                            new Integer(userId), dataItems, nodepath2, taskgroup);

            }
            // manger.getClosedTasks().add(t);
        } else { // equality of an item will not go furth in the index
            // for this
            // task and just get a new task
            nodepath = nodepath + ";=" + (((decsion - 1) / 2) + 1);
            String resultQuery = getDataItems(treeIndex, nodepath, taskType);
            if (q.getSolvedsofar() + 1 >= queryReplication) {
                int totalDecision = makeDecsion("" + q.getRetreivedResult() + ";"
                        + resultQuery);
                statment = "update crowdindex.query set responsetime= CURRENT_TIMESTAMP, depthpaths=concat(ifnull( depthpaths,''),';"
                        + nodepath
                        + "') , retrievedresult=concat(ifnull( retrievedresult,''),'_"
                        + resultQuery
                        + "') ,totalDecisionDepth='"
                        + totalDecision
                        + "' ,solved=1, solvedsofar='"
                        + (q.getSolvedsofar() + 1)
                        + "'  where id = " + queryId + ";";
            } else {
                statment = "update crowdindex.query set  retrievedresult=concat(ifnull( retrievedresult,''),';"
                        + resultQuery
                        + "'), depthpaths=concat( ifnull(depthpaths,''),'_"
                        + nodepath
                        + "')  , solvedsofar='"
                        + (q.getSolvedsofar() + 1)
                        + "'  where id = " + queryId + ";";
            }
            stmt = con.createStatement();
            stmt.execute(statment);
            // stmt.close();
        }
        return reply;
    }
    String handleTest(Connection con, Statement stmt, ResultSet rs, String input) throws Exception {
        String reply = "Result send successfully";
        String[] s = input.split(";");
        String taskId = s[0];
        String userId = s[1];
        String res = s[2];
        String taskType = s[3];

        Integer result = new Integer(res);
        if (result % 2 == 0)
            result = result / 2;
        else
            result = (((result - 1) / 2) + 1);
        // get task info to get task group
        // update the task to set end time stamp\
        // get task group
        // update task group
        // create new tasks if necessary
        // update query if necessary
        String taskgroup = "";
        String statment = "";
        String nodepath = "";
        int level = 0;
        String queryId = "";
        String dataItemsList = "";

        int treeIndex = 1;

        rs = stmt
                .executeQuery("select  id,taskgroup,nodepath,indexparam,level,query,dataitems from crowdindex.task where id = "
                        + taskId + ";");
        while (rs.next()) {
            taskgroup = "" + rs.getInt("taskgroup");
            nodepath = rs.getString("nodepath");
            treeIndex = rs.getInt("indexparam");
            level = rs.getInt("level");
            queryId = "" + rs.getInt("query");
            dataItemsList = "" + rs.getString("dataitems");
        }
        String[] itemsList = null;
        if (dataItemsList != null) {
            itemsList = dataItemsList.split(",");
        }
        stmt = con.createStatement();
        statment = "update crowdindex.task set returned_result= " + result
                + ", responseTime = CURRENT_TIMESTAMP, taskstatus =2 where id = " + taskId + ";";
        stmt.execute(statment);

        stmt = con.createStatement();
        rs = stmt
                .executeQuery("select  count(*) solvedsofar from crowdindex.task where indexparam = "
                        + treeIndex
                        + " and taskstatus =2 and level = "
                        + level
                        + " and queryModel = 'test';");

        int solvedsofar = 0;
        while (rs.next()) {
            solvedsofar = rs.getInt("solvedsofar");
        }
        double expectedDistError = 0;
        if (solvedsofar >= testQueryNum * testReplication) {
            ArrayList<Double> errorCount = getErrorCount(con, stmt, rs, treeIndex, level);
            for (int i = 1; i <= testFanout; i++) {
                expectedDistError += i * errorCount.get(i) * Math.pow(testFanout, level);
            }
            stmt = con.createStatement();
            stmt.execute("insert into crowdindex.expectedDistanceError (tree,level,disterr) values ("
                    + treeIndex + "," + level + "," + expectedDistError + ");");
            stmt = con.createStatement();
            rs = stmt
                    .executeQuery("select  tree,level,disterr  from crowdindex.expectedDistanceError where tree = "
                            + treeIndex + " order by level;");

            ArrayList<Double> errors = new ArrayList<Double>();
            double sum = 0;
            double val;
            while (rs.next()) {
                val = rs.getDouble("disterr");
                sum += val;
                errors.add(val);

            }

            if (st.get(treeIndex).height() + 1 == errors.size()) {
                // create query and replication per level

                for (int i = 0; i < errors.size(); i++)
                    errors.set(i, errors.get(i) / sum);
                createProbalisticQueries(con, stmt, rs, treeIndex, errors);
            }

        }

        return reply;
    }

    ArrayList<Double> getErrorCount(Connection con, Statement stmt, ResultSet rs, int treeIndex,
            int level) throws Exception {
        ArrayList<Double> list = new ArrayList<Double>();
        for (int i = 0; i < 50; i++) {
            list.add(0.0);
        }
        int maxError = 0;
        stmt = con.createStatement();
        String result = "", expectedResult = "";
        int error = 0;
        String nodepath = "";
        rs = stmt
                .executeQuery("select  returned_result, expected_result,nodepath from crowdindex.task where indexparam = "
                        + treeIndex
                        + " and taskstatus =2 and level = "
                        + level
                        + " and queryModel = 'test';");
        while (rs.next()) {
            result = rs.getString("returned_result");
            expectedResult = rs.getString("expected_result");
            nodepath = rs.getString("nodepath");
            error = getError(result, expectedResult, nodepath);

            if (error > maxError)
                maxError = error;
            list.set(error, (list.get(error) + 1));
        }
        double sum = 0;
        for (int i = 0; i <= maxError; i++)
            sum = sum + list.get(i);
        for (int i = 0; i <= maxError; i++)
            list.set(i, list.get(i) / sum);

        return list;

    }

    int getError(String result, String expectedResult, String nodePath) {
        Integer res = new Integer(result);
        String expected = "0";
        Integer exp = 0;
        String[] expResList = expectedResult.split(";");
        expResList[expResList.length - 1] = expResList[expResList.length - 1].substring(1);
        if ("root".equals(nodePath)) {
            expected = expResList[1];
            exp = new Integer(expected);
        } else {
            String[] nodePathList = nodePath.split(";");
            int loc = nodePathList.length;
            if (loc < expResList.length)
                exp = new Integer(expResList[loc]);
        }

        return Math.abs(res - exp);
    }

    String handleAStar(Connection con, Statement stmt, ResultSet rs, String input) throws Exception {
        String reply = "Result send successfully";
        String[] s = input.split(";");
        String taskId = s[0];
        String userId = s[1];
        String result = s[2];
        String taskType = s[3];

        // get task info to get task group
        // update the task to set end time stamp\
        // get task group
        // update task group
        // create new tasks if necessary
        // update query if necessary
        String taskgroup = "";
        String statment = "";
        String nodepath = "";
        int level = 0;
        String queryId = "";
        String dataItemsList = "";
        Integer updateFactor = 1;
        int treeIndex = 1;
        Double gAstar = 1.0;
        int treeOrder = 2;
        int treeHeight=0;
        rs = stmt
                .executeQuery("select  id,taskgroup,nodepath,indexparam,level,query,dataitems,gAstar from crowdindex.task where id = "
                        + taskId + ";");
        while (rs.next()) {
            taskgroup = "" + rs.getInt("taskgroup");
            nodepath = rs.getString("nodepath");
            treeIndex = rs.getInt("indexparam");
            level = rs.getInt("level");
            queryId = "" + rs.getInt("query");
            dataItemsList = "" + rs.getString("dataitems");
            gAstar = rs.getDouble("gAstar");
        }
        String[] itemsList = null;
        if (dataItemsList != null) {
            itemsList = dataItemsList.split(",");
        }
        treeOrder = st.get(treeIndex).getOrder();
        treeHeight = st.get(treeIndex).getHT();
        stmt = con.createStatement();
        statment = "update crowdindex.task set returned_result= " + result
                + ", responseTime = CURRENT_TIMESTAMP, taskstatus =2 where id = " + taskId + ";";
        stmt.execute(statment);
        statment = "update crowdindex.taskgroup set resultssofar= concat(ifnull( resultssofar,''),';"
                + result + "') , respondsofar =  respondsofar+1 where id = " + taskgroup + ";";
        stmt = con.createStatement();
        stmt.execute(statment);
        stmt = con.createStatement();
        rs = stmt
                .executeQuery("select  id ,resultssofar,respondsofar,replicationnum from crowdindex.taskgroup where id = "
                        + taskgroup + ";");
        int replication = 5;
        int solvedsofar = 0;
        String resultSofar = "";

        while (rs.next()) {
            resultSofar = rs.getString("resultssofar");
            if (resultSofar == null)
                resultSofar = "";
            solvedsofar = rs.getInt("respondsofar");
            replication = rs.getInt("replicationnum");

        }
        stmt = con.createStatement();
        rs = stmt.executeQuery("select  astarUpdateScore from crowdindex.query where id = "
                + queryId + ";");

        while (rs.next()) {
            updateFactor = rs.getInt("astarUpdateScore");
            if (updateFactor == null)
                updateFactor = Constants.UPDATE_FACTOR;
        }

        if (solvedsofar >= replication) {

            ServerSideQueryAnswer finalResult = expandNodeBasedOnWorkerResponses(con, stmt, rs,
                    resultSofar, itemsList, level, updateFactor, queryId, nodepath, treeOrder,
                    gAstar,treeIndex,treeHeight);
            if (finalResult != null) {
                String resultQuery="";
                // TODO store the final result and mark this tasks as solved
                //storing that the path taken to rach the final answer did reach the max sub tree key
                if(Constants.MAX_SUBTREE_KEY.equals( ""+finalResult.getIndexWithinNode())||Constants.MIN_SUBTREE_KEY.equals( ""+finalResult.getIndexWithinNode())){
                    nodepath = nodepath + ";=" + finalResult.getIndexWithinNode() ;

                }

                else{
                    //this is a regular key that is found from within a regular node
                    //since index within node includes odd values for branches, even values for key
                    //then we devide by two to get the exact key value
                    nodepath = nodepath + ";=" + finalResult.getIndexWithinNode() / 2;

                }
                resultQuery  =  ""+finalResult.getKey();
                stmt = con.createStatement();
                statment = "update crowdindex.query set responsetime= CURRENT_TIMESTAMP, retrievedresult='"
                        + resultQuery
                        + "' ,solved=1, retreivedpath='"
                        + nodepath
                        + "'  where id = " + queryId + ";";
                stmt.execute(statment);

                // queue
                statment = "delete from crowdindex.backtrack where query = " + queryId + ";";
                stmt.execute(statment);

            } else {
                // TODO select node with max score to expand
                selectNodeToExpand(con, stmt, rs, queryId, nodepath, taskType, treeIndex, level);
            }
        }

        return reply;
    }

    boolean selectNodeToExpand(Connection con, Statement stmt, ResultSet rs, String queryId,
            String nodePath, String taskType, int treeIndex, int level) throws Exception {

        String backTrackNodePath = null;
        int backTrackLevel = 0;
        int id = 0;
        double gValue = -1;
        double score= -1;
        double h = -1;
        stmt = con.createStatement();
        rs = stmt
                .executeQuery("select  id, nodepath,level,g ,score,h from crowdindex.backtrack where query = "
                        + queryId + " order by score desc limit 1; ");
        while (rs.next()) {
            id = rs.getInt("id");
            backTrackLevel = rs.getInt("level");
            backTrackNodePath = rs.getString("nodepath");
            gValue = Double.parseDouble(rs.getString("g"));
            h = Double.parseDouble(rs.getString("h"));
            score = Double.parseDouble(rs.getString("score"));
        }

        // create tasks for backtracking
        if (backTrackNodePath != null && !"".equals(backTrackNodePath)
                && !"null".equals(backTrackNodePath)) {
            String statment = "delete from crowdindex.backtrack where id = " + id + ";";
            stmt.execute(statment);

            String backTrackDataItems = getDataItems(treeIndex, backTrackNodePath, taskType);
            Query q = getQueryById(queryId);
            int newReplication = getReplicationForQueryLevel(con, stmt, rs, new Integer(queryId),
                    backTrackLevel);
            int newtaskgourpid = createTaskGroup(con, stmt, rs, q, backTrackLevel, newReplication);
            // ArrayList<Integer> users = getTaskUserList();
            for (int i = 0; i < newReplication; i++) {
                createTaskAstar(con, stmt, rs, q, backTrackLevel, newReplication, newtaskgourpid,
                        backTrackDataItems, backTrackNodePath, taskType, gValue);
            }
            return true;
        } else
            return false;

    }

    // this function returns true if the back trackpath is larger than the
    // nodepath and direction =1 or
    // back trackpath is smaller than the nodepath and direction =0 and false
    // otherwise
    boolean comapreBackTrackNodePaths(String backTrackPath, String nodePath, int direction) {
        String[] backPathList = backTrackPath.split(";");
        String[] nodeList = nodePath.split(";");
        int length = Math.min(backPathList.length, nodeList.length);
        for (int i = 1; i < length; i++) {
            if (direction == 0) {
                if (new Integer(backPathList[i]) < new Integer(nodeList[i]))
                    return true;
            } else {
                if (new Integer(backPathList[i]) > new Integer(nodeList[i]))
                    return true;
            }
        }
        return false;
    }

    public int makeDecsion(String r) {

        String[] results = r.split(";");
        // majority voting decsion making
        int range = 10000;
        int A[] = new int[range]; // 100 is the max fanout for a node
        for (int i = 0; i < range; i++)
            A[i] = 0;
        for (String s : results) {
            ;
            if (s != null && !"".equals(s))
                A[new Integer(s)]++;
        }
        int maxloc = -1, maxvalue = -1;
        for (int i = 0; i < range; i++) {

            if (A[i] > maxvalue) {
                maxvalue = A[i];
                maxloc = i;
            }

        }
        return maxloc;
    }

    public ArrayList<DecisionItem> makeDecsionStatistics(String r) {
        ArrayList<DecisionItem> frequenceis = new ArrayList<DecisionItem>();
        double sum = 0;
        String[] results = r.split(";");
        int negCount = 0;
        // majority voting decsion making
        int range = Constants.MAX_NODE_RANGE;
        int A[] = new int[range]; // 100 is the max fanout for a node
        for (int i = 0; i < range; i++)
            A[i] = 0;
        for (String s : results) {

            if (s != null && !"".equals(s)) {
                sum++;
                if (new Integer(s) >= 0)
                    A[new Integer(s)]++;
                else
                    negCount++;
            }

        }
        for (int i = 0; i < range; i++) {
            if (A[i] != 0) {
                frequenceis.add(new DecisionItem(i, A[i] / sum));
            }
        }
        if (negCount != 0)
            frequenceis.add(new DecisionItem(-1, negCount / sum));
        Collections.sort(frequenceis);
        return frequenceis;
    }

    /**
     * Escape an html string. Escaping data received from the client helps to
     * prevent cross-site script vulnerabilities.
     * 
     * @param html
     *            the html string to escape
     * @return the escaped string
     */
    private String escapeHtml(String html) {
        if (html == null) {
            return null;
        }
        return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    @Override
    public String printQueryResults(String s) throws IllegalArgumentException {

        try {
            java.sql.Connection con = null;
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {

                e.printStackTrace();
                writeToFile(e.getMessage());
                writeToFile(e.getStackTrace().toString());
                connectToDB();
            }

            try {
                con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/sakila?allowMultiQuery=true", "root", "1234");
            } catch (SQLException e) {
                e.printStackTrace();
                writeToFile(e.getMessage());
                writeToFile(e.getStackTrace().toString());
                connectToDB();

            }

            try {

                stmt = con.createStatement();

            } catch (SQLException e) {

                e.printStackTrace();
                writeToFile(e.getMessage());
                writeToFile(e.getStackTrace().toString());
                connectToDB();

            }

            try {
                stmt = con.createStatement();
                rs = stmt.executeQuery("select  *from sakila.actor;");
                while (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    System.out.println("   " + firstName + " " + lastName);
                }
                // rs.close();
                // stmt.close();
            } catch (SQLException e) {

                e.printStackTrace();
                writeToFile(e.getMessage());
                writeToFile(e.getStackTrace().toString());
                connectToDB();

            }
            String username = "xxxx";
            String password = "xxxx";
            String result = "hey there" + username;

        } catch (Exception e) {
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
        }
        return "Result printed successfully";
    }

    @Override
    public String registerUser(String input) throws IllegalArgumentException {

        String[] s = input.split(";");
        String firstName = "";
        String password = "";
        int id = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select  id, name, password from crowdindex.users where name='"
                    + s[0] + "' and password ='" + s[1] + "';");
            while (rs.next()) {
                id = rs.getInt("id");
                firstName = rs.getString("name");
                password = rs.getString("password");
                // System.out.println("   "+firstName+" "+password);
            }
            // rs.close();
            // stmt.close();
        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            // connectToDB();

        } finally {
            closeEveryThing(con, stmt, rs);
        }

        if (firstName.equals(s[0]) && password.equals(s[1]))
            return "Login successfull your id is:" + id;
        else
            return "Incorrect user name and password";
    }

    @Override
    public String reset(String s) throws IllegalArgumentException {
        reset();
        return null;
    }

    @Override
    public String getAvailTask(String s) throws IllegalArgumentException {

        String result = "";
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            stmt = con.createStatement();

            rs = stmt.executeQuery("select count( *) c from crowdindex.task where (user = " + s
                    + "  and( taskstatus =1) )or taskstatus =0 ;");
            while (rs.next()) {
                result = "" + rs.getInt("c");

            }
            rs = stmt.executeQuery("select completedtasks c from crowdindex.users where id = " + s
                    + "  ;");
            while (rs.next()) {
                result = result + "," + rs.getInt("c");

            }

        } catch (Exception e) {
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            e.printStackTrace();
        } finally {
            closeEveryThing(con, stmt, rs);
        }
        return result;
    }

    @Override
    public String signUp(String input) throws IllegalArgumentException {
        String[] s = input.split(";");
        String firstName = s[0];
        String password = s[1];
        int count = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String reply = "error";
        try {
            con = getConnection();
            stmt = con.createStatement();

            rs = stmt.executeQuery("select count( *) c from crowdindex.users where name='" + s[0]
                    + "' ;");
            while (rs.next()) {
                count = rs.getInt("c");
            }
            // rs.close();
            // stmt.close();

            if (count == 0) {
                stmt = con.createStatement();
                String statment = "INSERT INTO crowdindex.users (name,password,opentasks,completedtasks)values ('"
                        + firstName + "' ,'" + password + "',0,0);";
                // stmt.close();
                stmt.execute(statment);
                // stmt.close();
                reply = "user registered please log in now to get tasks";
            } else
                reply = "User name already taken";
        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());

            // connectToDB();

        } finally {
            closeEveryThing(con, stmt, rs);
        }
        return reply;
    }

    @Override
    public String test(String s) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return "testing";
    }

    void buildTestCases() {

        // this is an important function that builds test cases for
        // crowd-sourced indexing
        // the table "query" contains queries to be asked to asked over indexes
        // the table "index-param" contains details of a specific index
        // "order(fanout), dataset, description(the type of operation performed on an index "insert")  "

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        int id = 1;
        String s = "";
        try {

            con = getConnection();
            stmt = con.createStatement();
            s = "delete from crowdindex.query";
            stmt.execute(s);
            s = "delete from crowdindex.indexparam";
            stmt.execute(s);
            s = "delete from crowdindex.replicationperlevel";
            stmt.execute(s);
            s = "delete from crowdindex.insertionorder";
            stmt.execute(s);
            int insertdataset1id, insertdataset2id;

            // ----setting up trees -------------------
            id = 1;

            for (int i = 3; i <= 7; i++) {
                s = "INSERT INTO crowdindex.indexparam (id,fanout,dataset,descrip)values (" + id
                        + "," + i + ",1,'query')";
                stmt.execute(s);
                id++;
                s = "INSERT INTO crowdindex.indexparam (id,fanout,dataset,descrip)values (" + id
                        + "," + i + ",2,'query')";
                stmt.execute(s);
                id++;
            }
            insertdataset1id = id;
            s = "INSERT INTO crowdindex.indexparam (id,fanout,dataset,descrip)values (" + id + ","
                    + 3 + ",1,'insert')";
            stmt.execute(s);
            s = "INSERT INTO crowdindex.insertionorder (treeid,insertorder,itemid,dataset)values ("
                    + id + ",1," + 74 + ",1)";
            stmt.execute(s);
            id++;
            insertdataset2id = id;
            s = "INSERT INTO crowdindex.indexparam (id,fanout,dataset,descrip)values (" + id + ","
                    + 3 + ",2,'insert')";
            stmt.execute(s);
            s = "INSERT INTO crowdindex.insertionorder (treeid,insertorder,itemid,dataset)values ("
                    + id + ",1," + 600 + ",2)";
            stmt.execute(s);
            id++;

            // ----Experiment 1 testing insertion perfromance
            // -------------------
            id = 1;
            int startId = 0, EndId = 0, repId = 1;

            int rep = 7;
            startId = id;
            Random randomGenerator = new Random();
            // testing insertion for the first data set
            for (int i = 0; i < 20; i++) {
                int randomindert = randomGenerator.nextInt(120) + 10;
                s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                        + "values ("
                        + id
                        + ","
                        + randomindert
                        + ","
                        + insertdataset1id
                        + ",'insert',0,CURRENT_TIMESTAMP,1)";
                stmt.execute(s);
                EndId = id;
                id++;
            }
            // -----------------------------------------------------------------------------------

            // testing insertion for the second data set
            /*
             * for(int i =0;i<20;i++){ int randomindert =
             * randomGenerator.nextInt(1100) +100; s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+randomindert+","+insertdataset2id+
             * ",'insert',0,CURRENT_TIMESTAMP,2)"; stmt.execute(s); EndId =id;
             * id++; }
             * 
             * for(int j=startId;j<=EndId;j++){ s =
             * "INSERT INTO crowdindex.replicationperlevel (id,level,replication,query)"
             * + "values ("+repId+",'all',"+rep+","+j+")"; stmt.execute(s);
             * repId++; }
             */
            // ----Experiment 2 testing replication effect -------------------

            for (rep = 5; rep <= 20; rep += 5) {
                startId = id;
                for (int i = 1; i <= 5; i++) {

                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                            + "values ("
                            + id
                            + ","
                            + (i * 140 / 5)
                            + ",5,'breadth',0,CURRENT_TIMESTAMP,1)";
                    stmt.execute(s);
                    id++;
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                            + "values ("
                            + id
                            + ","
                            + (i * 140 / 5)
                            + ",5,'depth',0,CURRENT_TIMESTAMP,1)";
                    stmt.execute(s);
                    id++;
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                            + "values ("
                            + id
                            + ","
                            + (i * 140 / 5)
                            + ",5,'informed',0,CURRENT_TIMESTAMP,1)";
                    stmt.execute(s);
                    id++;
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset,astarUpdateScore)"
                            + "values ("
                            + id
                            + ","
                            + (i * 140 / 5)
                            + ",5,'astar',0,CURRENT_TIMESTAMP,1," + Constants.UPDATE_FACTOR + ")";
                    stmt.execute(s);
                    id++;

                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                            + "values ("
                            + id
                            + ","
                            + (i * 1100 / 5 + 96)
                            + ",6,'breadth',0,CURRENT_TIMESTAMP,2)";
                    stmt.execute(s);
                    id++;
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                            + "values ("
                            + id
                            + ","
                            + (i * 1100 / 5 + 96)
                            + ",6,'depth',0,CURRENT_TIMESTAMP,2)";
                    stmt.execute(s);
                    id++;
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                            + "values ("
                            + id
                            + ","
                            + (i * 1100 / 5 + 96)
                            + ",6,'informed',0,CURRENT_TIMESTAMP,2)";
                    stmt.execute(s);
                    id++;
                    EndId = id;
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset,astarUpdateScore)"
                            + "values ("
                            + id
                            + ","
                            + (i * 1100 / 5 + 96)
                            + ",6,'astar',0,CURRENT_TIMESTAMP,2," + Constants.UPDATE_FACTOR + ")";
                    stmt.execute(s);
                    id++;

                }
                for (int j = startId; j <= EndId; j++) {
                    s = "INSERT INTO crowdindex.replicationperlevel (id,level,replication,query)"
                            + "values (" + repId + ",'all'," + rep + "," + j + ")";
                    stmt.execute(s);
                    repId++;
                }
            }
            /*
             * 
             * //----Experiment 3 testing fanout effect ------------------- //
             * id = 1; startId=0;EndId=0; int indexParams []={1,9,10}; startId =
             * id; for(int fan = 0;fan<indexParams.length;fan++){ for(int i
             * =1;i<=5;i++){
             * 
             * s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+(i*145/6)+","+indexParams[fan]+
             * ",'breadth',0,CURRENT_TIMESTAMP,1)"; stmt.execute(s); id++; s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+(i*145/6)+","+indexParams[fan]+
             * ",'depth',0,CURRENT_TIMESTAMP,1)"; stmt.execute(s); id++; s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+(i*145/6)+","+indexParams[fan]+
             * ",'informed',0,CURRENT_TIMESTAMP,1)"; stmt.execute(s); id++; s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+(i*145/6)+","+indexParams[fan]+
             * ",'astar',0,CURRENT_TIMESTAMP,1)"; stmt.execute(s); id++;
             * 
             * 
             * s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+(i*1150/6 +
             * 96)+","+(indexParams[fan]+1)+",'breadth',0,CURRENT_TIMESTAMP,2)";
             * stmt.execute(s); id++; s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+(i*1150/6 +
             * 96)+","+(indexParams[fan]+1)+",'depth',0,CURRENT_TIMESTAMP,2)";
             * stmt.execute(s); id++; s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+(i*1150/6 +
             * 96)+","+(indexParams[fan]+1)+",'informed',0,CURRENT_TIMESTAMP,2)"
             * ; stmt.execute(s); id++; EndId = id; s =
             * "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
             * + "values ("+id+","+(i*1150/6 +
             * 96)+","+(indexParams[fan]+1)+",'astar',0,CURRENT_TIMESTAMP,2)";
             * stmt.execute(s); id++;
             * 
             * }
             * 
             * } for(int j=startId;j<=EndId;j++){ s =
             * "INSERT INTO crowdindex.replicationperlevel (id,level,replication,query)"
             * + "values ("+repId+",'all',"+10+","+j+")"; stmt.execute(s);
             * repId++; }
             */
            // ----Experiment 4 getting expected distance of error
            // -------------------
            // task 1 generating 50 queries and calculating
            startId = id;
            for (int k = 1; k <= testQueryNum; k++) {

                s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                        + "values ("
                        + id
                        + ","
                        + (k * 140 / (testQueryNum + 1))
                        + ","
                        + dataSet1TestTree + ",'test',0,CURRENT_TIMESTAMP,1)";
                stmt.execute(s);
                // id++;
                // s =
                // "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset)"
                // +
                // "values ("+id+","+(k*1150/(testQueryNum+1) +
                // 96)+","+dataSet2TestTree+",'test',0,CURRENT_TIMESTAMP,2)";
                // stmt.execute(s);
                EndId = id;
                id++;
            }
            for (int j = startId; j <= EndId; j++) {
                s = "INSERT INTO crowdindex.replicationperlevel (id,level,replication,query)"
                        + "values (" + repId + ",'all'," + testReplication + "," + j + ")";
                stmt.execute(s);
                repId++;
            }

        } catch (Exception e) {

            e.printStackTrace();
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());

            // connectToDB();

        } finally {
            closeEveryThing(con, stmt, rs);
        }
    }

    @Override
    public String resetUserTasks(String input) throws IllegalArgumentException {
        // reset user task count for the given user id

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String res = "OK";
        try {
            con = getConnection();
            stmt = con.createStatement();

            String s = "update crowdindex.users set opentasks =   0  where id = '" + input + "' ;";
            stmt.execute(s);

        } catch (Exception e) {
            writeToFile(e.getMessage());
            writeToFile(e.getStackTrace().toString());
            e.printStackTrace();
            res = "Fail";
        } finally {
            closeEveryThing(con, stmt, rs);
        }
        return res;

    }

    void createProbalisticQueries(Connection con, Statement stmt, ResultSet rs, int tree,
            ArrayList<Double> errors) throws Exception {

        int id = 1, repId = 1;
        String s = "";
        int startId = 0, endId = 0;
        ArrayList<Query> queryList = new ArrayList<Query>();

        stmt = con.createStatement();
        rs = stmt.executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.task; ");
        while (rs.next()) {
            id = rs.getInt("maxid");
        }
        stmt = con.createStatement();
        rs = stmt
                .executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.replicationperlevel; ");
        while (rs.next()) {
            repId = rs.getInt("maxid");
        }
        for (int rep = 5; rep <= 10; rep += 5) {
            startId = 0;
            for (int i = 1; i <= 5; i++) {
                Query q1 = new Query();
                Query q2 = new Query();
                q1.setQueryId(id);
                q2.setQueryId(id + 1);
                q1.setQueryModel("breadth");
                q2.setQueryModel("astar");
                q1.setIndexParam(tree);
                q2.setIndexParam(tree);

                if (tree == dataSet1TestTree) {

                    StringBuilder path = new StringBuilder();
                    st.get(tree).getPath((i * 140 / 5), path);
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset,expectedpath)"
                            + "values ("
                            + id
                            + ","
                            + (i * 140 / 5)
                            + ","
                            + tree
                            + ",'breadth',0,CURRENT_TIMESTAMP,1,'" + path.toString() + "')";
                    stmt.execute(s);
                    id++;
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset,expectedpath,astarUpdateScore)"
                            + "values ("
                            + id
                            + ","
                            + (i * 140 / 5)
                            + ","
                            + tree
                            + ",'astar',0,CURRENT_TIMESTAMP,1,'"
                            + path.toString()
                            + "' "
                            + Constants.UPDATE_FACTOR + ")";
                    stmt.execute(s);
                    endId = id;
                    id++;
                    q1.setExpectedPath(path.toString());
                    q2.setExpectedPath(path.toString());
                    q1.setDataItem("" + (i * 140 / 5));
                    q2.setDataItem("" + (i * 140 / 5));
                    q1.setDataset(1);
                    q2.setDataset(1);
                } else {
                    StringBuilder path = new StringBuilder();
                    st.get(tree).getPath((i * 1100 / 5), path);
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset,expectedpath)"
                            + "values ("
                            + id
                            + ","
                            + (i * 1100 / (testQueryNum + 1) + 96)
                            + ","
                            + tree + ",'breadth',0,CURRENT_TIMESTAMP,2,'" + path.toString() + "')";
                    stmt.execute(s);
                    id++;
                    s = "INSERT INTO crowdindex.query (id,dataitem,indexparam,querymodel,solved,createtime,dataset,expectedpath,astarUpdateScore)"
                            + "values ("
                            + id
                            + ","
                            + (i * 1100 / (testQueryNum + 1) + 96)
                            + ","
                            + tree
                            + ",'astar',0,CURRENT_TIMESTAMP,2,'"
                            + path.toString()
                            + "'"
                            + Constants.UPDATE_FACTOR + ")";
                    stmt.execute(s);
                    endId = id;
                    id++;
                    q1.setExpectedPath(path.toString());
                    q2.setExpectedPath(path.toString());
                    q1.setDataItem("" + (i * 1100 / (testQueryNum + 1) + 96));
                    q2.setDataItem("" + (i * 1100 / (testQueryNum + 1) + 96));
                    q1.setDataset(2);
                    q2.setDataset(2);
                }
                queryList.add(q1);
                queryList.add(q2);

            }
            for (int j = startId; j <= endId; j++) {
                for (int i = 0; i < errors.size(); i++) {
                    int repPerLevel = minRep + (int) (errors.get(i) * errors.size() * (rep - 1));
                    s = "INSERT INTO crowdindex.replicationperlevel (id,level,replication,query)"
                            + "values (" + repId + "," + i + "," + repPerLevel + "," + j + ")";
                    stmt.execute(s);
                    repId++;
                }
            }

        }
        con.commit();
        generateTasks(con, stmt, rs, queryList);

    }

    private ServerSideQueryAnswer expandNodeBasedOnWorkerResponses(Connection con, Statement stmt,
            ResultSet rs, String allResults, String[] itemsList, int level, int updateFactor,
            String queryId, String nodePath, int order, double currentGValue, int treeIndex,
            int treeHeight) throws SQLException {
        String[] resultsSplit = allResults.split(";");
        ArrayList<ServerSideQueryAnswer> resultList = new ArrayList<ServerSideQueryAnswer>();
        // this part builds results from all workers, it identifies answers with
        // backtracking ranges, the result list
        // will be used to build statisictis accross all retrieved results
        // itemslist will have all keys including subtree bounds
        // itemslist will be ["",minsubtreekey, nodekeys ,maxsubtreekey]
        // indexwithinnodes will be [-2{less than subtree branch}, -1{minsubtree
        // key},0{less than node key},1{firstkeynode}, .....
        // ,{maxsubtreekey},{greater than subtree key}]
        for (int i = 0; i < resultsSplit.length; i++) {
            if (resultsSplit[i] != null && !"".equals(resultsSplit[i])) {
                Integer indexWithinNode = Integer.parseInt(resultsSplit[i]);
                // this is a branching decision
                if (Math.abs(indexWithinNode) % 2 == 0
                        && !indexWithinNode.equals(Integer.parseInt(Constants.MAX_SUBTREE_KEY))) {
                    resultList.add(new ServerSideQueryAnswer(indexWithinNode));
                    // this is a node key and not a max sub tree key
                    // this maps index within nodes into an item of the items
                    // list
                } else if (Math.abs(indexWithinNode) % 2 != 0
                        && !indexWithinNode.equals(Integer.parseInt(Constants.MAX_SUBTREE_KEY))) {
                    Double key = Double.parseDouble(itemsList[(indexWithinNode + 3) / 2]);
                    resultList.add(new ServerSideQueryAnswer(key, indexWithinNode));
                    // this is the max sub tree key
                } else if (indexWithinNode.equals(Integer.parseInt(Constants.MAX_SUBTREE_KEY))) {
                    Double key = Double.parseDouble(itemsList[itemsList.length - 1]);
                    resultList.add(new ServerSideQueryAnswer(key, indexWithinNode));
                }
            }
        }
        // the total number of votes is equal to the size of the result list,
        // this is important in the calculations of the probabilities for node
        // branches in the
        // 100 is a very large number to represent the content of a node
        int[] childFrequenceis = new int[100];
        int[] keyFrequenceis = new int[100];
        int higherBackTrackingFreq = 0; // this is to count how many decisions
        // require higherBacktracking
        int smallerBackTrackingFreq = 0; // this is to count how many decisions
        // require smallerBackTracking
        int higherBoundCount = 0; // in the case of equality enabled, how many
        // decisions are equal to the higher bound of
        // the subtree of the current node
        int smallerBoundCount = 0; // in the case of equality enabled, how many
        // decisions are equal to the lower bound of
        // the subtree of the current node
        // initialization of counters
        for (int i = 0; i < 100; i++) {
            childFrequenceis[i] = 0;
            keyFrequenceis[i] = 0;
        }
        for (int i = 0; i < resultList.size(); i++) {
            if (resultList.get(i).isBranch()) // this is a decision to branch to
                // a child node
                childFrequenceis[resultList.get(i).getIndexWithinNode()]++;
            else if (resultList.get(i).isHigherBackTracking()) // this is a
                // backtracking
                // to the higher
                // side of the
                // node
                higherBackTrackingFreq++;
            else if (resultList.get(i).isSmallerBackTracking()) // this is a
                // backTracking
                // to the lower
                // side of the
                // node
                smallerBackTrackingFreq++;
            else if (resultList.get(i).isHigherBoundKey()) // this is equality
                // to the higher
                // bound of the node
                higherBoundCount++;
            else if (resultList.get(i).isSmallerBoundKey()) // this is equality
                // to the smaller
                // bound of the node
                smallerBoundCount++;
            else
                keyFrequenceis[resultList.get(i).getIndexWithinNode()]++; // this
            // is
            // equality
            // to
            // the
        }
        // find the maximum frequencies
        int maxloc = 0;
        int maxFreq = 0;
        MaxFreqType maxFreqType = MaxFreqType.DEFAULT;
        // when equality, giving precedence to a child branch, then a node key,
        // then a node bound, then backtracking
        if (higherBackTrackingFreq > maxFreq) {
            maxFreq = higherBackTrackingFreq;
            maxFreqType = MaxFreqType.HIGHER_BACKTRACKING;
        }
        if (smallerBackTrackingFreq > maxFreq) {
            maxFreq = smallerBackTrackingFreq;
            maxFreqType = MaxFreqType.SMALLER_BACKTRACKING;
        }
        if (higherBoundCount > maxFreq) {
            maxFreq = higherBoundCount;
            maxFreqType = MaxFreqType.HIGHER_BOUND_KEY;
        }
        if (smallerBoundCount > maxFreq) {
            maxFreq = smallerBoundCount;
            maxFreqType = MaxFreqType.SMALLER_BOUND_KEY;
        }
        for (int i = 0; i < 100; i++) {
            if (keyFrequenceis[i] > maxFreq) {
                maxFreq = keyFrequenceis[i];
                maxloc = i;
                maxFreqType = MaxFreqType.NODE_KEY;

            }
        }
        for (int i = 0; i < 100; i++) {
            if (childFrequenceis[i] > maxFreq) {
                maxFreq = childFrequenceis[i];
                maxloc = i;
                maxFreqType = MaxFreqType.CHILD_BRANCH;
            }
        }
        ArrayList<ServerSideQueryAnswer> finalResults = new ArrayList<ServerSideQueryAnswer>();
        // step 1 check if a final decision at a key is reached then return it
        // and it is done
        if (maxFreqType == MaxFreqType.NODE_KEY) {
            for (int i = 0; i < 100; i++) {
                if (keyFrequenceis[i] == maxFreq) {
                    for (int j = 0; j < resultList.size(); j++) {
                        if (resultList.get(j).isKey()
                                && resultList.get(j).getIndexWithinNode() == i)
                            finalResults.add(resultList.get(j));
                    }
                }
            }
            return finalResults.get(finalResults.size() / 2);
        } else if (maxFreqType == MaxFreqType.HIGHER_BOUND_KEY) {
            for (int j = 0; j < resultList.size(); j++) {
                if (resultList.get(j).isHigherBoundKey())
                    return (resultList.get(j));
            }

        } else if (maxFreqType == MaxFreqType.SMALLER_BOUND_KEY) {
            for (int j = 0; j < resultList.size(); j++) {
                if (resultList.get(j).isSmallerBoundKey())
                    return (resultList.get(j));
            }

        }

        int nodeKeyIndex = 0;
        // step 2 add other node content to the the priority queue, with proper
        // score.
        // we add all child branches
        // nodes chosen from workers have a higher score
        // this is dones only for non leaf nodes

        if (level > 0) {
            // we subtract from the items list the first empty item and the last
            // items representing the maxsubtree key
            // the items list is {""[empty key],"minsubtree key"[lessthan
            // k1],"k1"[less than k2],"k2"..."km"[greater than
            // km],"maxsubtree key"[for maximum backtracking]}
            int numOfBranches = itemsList.length - 2;
            double scoreDenom = resultList.size() + numOfBranches;
            String expandedNodePath = "";
            int expanedLevel;
            double g;
            double h;
            double score;
            int startingIndex;
            for (int i = 0; i < numOfBranches; i++) {

                expandedNodePath = nodePath + ";" + i;

                expanedLevel = level - 1;
                g = currentGValue * (childFrequenceis[i * 2] + 1) / scoreDenom;
                h = 1.0 / Math.pow(order, level);
                score = g * h;
                startingIndex = Integer.parseInt(itemsList[i + 1]); // the added 1 because the list starts with an empty item
                addBackTrackingInfo(con, stmt, rs, queryId, expandedNodePath, g, h, score,
                        expanedLevel, startingIndex);

            }
        }
        else if(level ==0 && maxFreqType ==maxFreqType.CHILD_BRANCH){
            //reaching here means that we are a leaf level and we did not have an equality with a key
            //This means we will choose a key from this key list items
            //as a choice we choose to pick the key less than the current value
            //TODO another option is choose a midvalue between the two values
            // this will be marked by returning a key less than the current branch
            return new ServerSideQueryAnswer(Double.parseDouble(itemsList[maxloc/2+1]), maxloc);

        }
        // step 3 check the direction of backtracking and update the scores of
        // nodes accordingly
        //TODO when higher backtracking is choosen,
        //TODO we need to consider the case when
        int direction;
        if (maxFreqType == MaxFreqType.HIGHER_BACKTRACKING) {
            //  if (level != treeHeight) {
            direction = Constants.DIRECTION_HIGHER;
            nodeKeyIndex = Integer.parseInt(itemsList[itemsList.length-1]);
            updateAstarScore(con, stmt, rs, queryId, direction, updateFactor, nodeKeyIndex);
            // } else {
            // return new
            // ServerSideQueryAnswer(Double.parseDouble(itemsList[itemsList.length-1]),
            // Integer.parseInt(Constants.MAX_SUBTREE_KEY));
            // }

        } else if (maxFreqType == MaxFreqType.SMALLER_BACKTRACKING) {
            //   if (level != treeHeight) {
            direction = Constants.DIRECTION_LOWER;
            nodeKeyIndex = Integer.parseInt(itemsList[1]);
            updateAstarScore(con, stmt, rs, queryId, direction, updateFactor, nodeKeyIndex);
            //            } else {
            //                return new ServerSideQueryAnswer(Double.parseDouble(itemsList[1]), Integer.parseInt(Constants.MIN_SUBTREE_KEY));
            //            }
        }
        return null;

    }

    private void updateAstarScore(Connection con, Statement stmt, ResultSet rs, String queryID,
            int direction, int updateFactor, int nodeKeyIndex) throws SQLException {
        String statment = null;

        if (direction == Constants.DIRECTION_HIGHER) {
            //get the number of branches higher than the backtracking index and the max starting index
            //if there are branches higher than max index  update them with the update factor
            //if not just update the max index so far such that it reflects the higher backtracking direction
            int maxCount = 0,maxIndex = 0;
            stmt = con.createStatement();
            rs = stmt
                    .executeQuery("select  count(*) as maxCount  from crowdindex.backtrack where  query = " + queryID
                            + " and startingIndex >=" + nodeKeyIndex + " ;");
            while (rs.next()) {
                maxCount = rs.getInt("maxCount");
            }
            stmt = con.createStatement();
            rs = stmt
                    .executeQuery("select  max(startingIndex) as maxIndex  from crowdindex.backtrack where  query = " + queryID
                            +" ;");
            while (rs.next()) {
                maxIndex = rs.getInt("maxIndex");
            }
            if(maxCount!=0)
                statment = "update crowdindex.backtrack set " + "score= score * " + updateFactor + " ,"
                        + "g = g * " + updateFactor + " where query = " + queryID
                        + " and startingIndex >=" + nodeKeyIndex + " ";
            else
                statment = "update crowdindex.backtrack set " + "score= score * " + updateFactor + " ,"
                        + "g = g * " + updateFactor + " where query = " + queryID
                        + " and startingIndex =" + maxIndex + " ";

        } else {
            //get the number of branches lower  than the backtracking index and the min starting index
            //if there are branches higher than min index  update them with the update factor
            //if not just update the min index so far such that it reflects the higher backtracking direction
            int minCount = 0,minIndex = 0;
            stmt = con.createStatement();
            rs = stmt
                    .executeQuery("select  count(*) as minCount  from crowdindex.backtrack where  query = " + queryID
                            + " and startingIndex <=" + nodeKeyIndex + " ;");
            while (rs.next()) {
                minCount = rs.getInt("minCount");
            }
            stmt = con.createStatement();
            rs = stmt
                    .executeQuery("select  min(startingIndex) as minIndex  from crowdindex.backtrack where  query = " + queryID
                            +" ;");
            while (rs.next()) {
                minIndex = rs.getInt("maxIndex");
            }
            if(minIndex!=0)
                statment = "update crowdindex.backtrack set " + "score= score * " + updateFactor + " ,"
                        + "g = g * " + updateFactor + " where query = " + queryID
                        + " and startingIndex <=" + nodeKeyIndex + " ";
            else
                statment = "update crowdindex.backtrack set " + "score= score * " + updateFactor + " ,"
                        + "g = g * " + updateFactor + " where query = " + queryID
                        + " and startingIndex =" + minIndex + " ";

        }
        stmt = con.createStatement();
        if(statment!=null)
            stmt.execute(statment);
    }

    // task for a certain user
    private void addBackTrackingInfo(Connection con, Statement stmt, ResultSet rs, String queryId,
            String nodepath, double g, double h, double score, int level, int startingIndex)
                    throws SQLException {
        int id = 0;

        stmt = con.createStatement();
        rs = stmt.executeQuery("select  ifnull(max(id),0)+1 as maxid  from crowdindex.backtrack; ");
        while (rs.next()) {
            id = rs.getInt("maxid");
        }

        stmt = con.createStatement();
        String statment = "insert into crowdindex.backtrack (id,query,nodepath,level,g,h,startingIndex,score) "
                + "values ("
                + id
                + ","
                + queryId
                + ",'"
                + nodepath
                + "',"
                + level
                + ","
                + g
                + ","
                + h + "," + startingIndex + "," + score + "); ";
        stmt.execute(statment);

    }
}
