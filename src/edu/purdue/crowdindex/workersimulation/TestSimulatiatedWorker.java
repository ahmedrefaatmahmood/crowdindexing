package edu.purdue.crowdindex.workersimulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import edu.purdue.crowdindex.dbconnection.DbConnection;
import edu.purdue.crowdindex.server.index.BTree;
import edu.purdue.crowdindex.server.index.Node;

public class TestSimulatiatedWorker {
    static RandomGenerator randomGenerator;

    public TestSimulatiatedWorker() {
        init();
    }

    private static DbConnection connBuilder ;//= new DbConnection();

    // private static traversalStrategies;
    public static void init() {
        randomGenerator = RandomGenerator.getInstance();

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        init();
        // ArrayList<Double> carsDataSet = buildCarsDataSet("carsDataSet.txt");


        // buildUniformDataSet("uniformDataSet.txt");
        // buildUniformDataSetQueries("uniformDataSetQueries.txt");
        // ArrayList<Double> randomDataSet =
        // buildRandomDataSet("randomDataSet.txt");
        // buildRandomQueries(randomDataSet, "randomDataSetQueries.txt");
        // ArrayList<Double> skewdDataSet =
        // buildSkewdDataSet("skewdDataSet.txt");
        // buildRandomQueries(skewdDataSet, "skewdDataSetQueries.txt");

        ArrayList<Double> carsDataSet = readDataFromFile("carsDataSet.txt");

        ArrayList<Double> uniformDataSet = readDataFromFile("uniformDataSet.txt");
        ArrayList<Double> randomDataSet =
                readDataFromFile("randomDataSet.txt");
        ArrayList<Double> skewdDataSet = readDataFromFile("skewdDataSet.txt");
        //

        //  buildRandomQueries(carsDataSet, "carsDataSetQueries.txt");
        ArrayList<Query> carsDataSetQueries = readQueriesFromFile("carsDataSetQueries.txt");
        ArrayList<Query> uniformDataSetQueries = readQueriesFromFile("uniformDataSetQueries.txt");
        ArrayList<Query> randomDataSetQueries =
                readQueriesFromFile("randomDataSetQueries.txt");
        ArrayList<Query> skewdDataSetQueries = readQueriesFromFile("skewdDataSetQueries.txt");

        int workerType = Parameters.proximityWorker;
        //  int workerType = Parameters.normalWorker;
        testCostEffect(workerType, carsDataSet, carsDataSetQueries);
        // testOrderEffect(workerType, carsDataSet, carsDataSetQueries);
        //   testVarEffect(workerType, carsDataSet, carsDataSetQueries);

        //        testCostEffect(workerType, uniformDataSet, uniformDataSetQueries);
        //        testOrderEffect(workerType, uniformDataSet, uniformDataSetQueries);
        //        testVarEffect(workerType, uniformDataSet, uniformDataSetQueries);

        // testCostEffect(workerType,randomDataSet,randomDataSetQueries);
        // testOrderEffect(workerType,randomDataSet,randomDataSetQueries);
        // testVarEffect(workerType,randomDataSet,randomDataSetQueries);
        //
        //        testCostEffect(workerType,skewdDataSet,skewdDataSetQueries);
        //        testOrderEffect(workerType,skewdDataSet,skewdDataSetQueries);
        //        testVarEffect(workerType,skewdDataSet,skewdDataSetQueries);
        //testProb(carsDataSet, carsDataSetQueries);
        //  testTraversalStartegies(workerType,carsDataSet,carsDataSetQueries);
        System.out.println("Test done");
    }


    static  void testProb(ArrayList<Double>dataset,ArrayList<Query> queries){
        Worker w = new Worker();
        int order = Parameters.defaultOrder;
        int var =5000;

        BTree<Double, Double> tree = buildBtree(dataset, order);
        Node node1 = tree.getRoot();
        Node node2 = node1.children[1].next;
        Node node3 = node2.children[4].next;
        Node node4 = node3.children[5].next;
        Node node5 = node4.children[7].next;
        Query query = new  Query(83650);
        System.out.println(" Query "+query.queryKey);
        Node node;int level ;
        node= node5;level =0;
        //        node =node2;level =3;
        //        node = node3; level =2;
        //        node = node4 ; level =1;
        //        node = node5; level =0;
        System.out.println(" , "+node.minEntry.key);
        for(int i=1;i<node.m;i++){
            System.out.print(" , "+node.children[i].key);
        }
        System.out.println();
        System.out.println(" , "+node.maxEntry.key);
        if(level!=0){
            System.out.println("****************No backtracking, No euality***************");
            QueryResult result1 =  w.getProximityWorkerResponse(tree.getRoot(), node, query, false, var,level,tree.getHT(), false);
            // System.out.println(result1.getKey());
            System.out.println("****************No backtracking, Equality*********");
            QueryResult result2 =  w.getProximityWorkerResponse(tree.getRoot(), node, query, true, var,level,tree.getHT(), false);
            // System.out.println(result2.getKey());
            System.out.println("*****************Backtracking, No equality*******");
            QueryResult result3 =  w.getProximityWorkerResponse(tree.getRoot(), node, query, false, var,level,tree.getHT(), true);
            //  System.out.println(result3.getKey());
            System.out.println("******************Backtracking, Equality **********");
            QueryResult result4 =  w.getProximityWorkerResponse(tree.getRoot(), node, query, true, var,level,tree.getHT(), true);
            //  System.out.println(result4.getKey());
            System.out.println("**********************************************");
        }

        if(level==0){
            for(int i =0;i<10;i++){
                System.out.println("****************No backtracking, No euality***************");
                QueryResult result1 =  w.getProximityWorkerResponse(tree.getRoot(), node, query, false, var,level,tree.getHT(), false);
                System.out.println(result1.getKey());
                System.out.println("****************No backtracking, Equality*********");
                QueryResult result2 =  w.getProximityWorkerResponse(tree.getRoot(), node, query, true, var,level,tree.getHT(), false);
                System.out.println(result2.getKey());
                System.out.println("*****************Backtracking, No equality*******");
                QueryResult result3 =  w.getProximityWorkerResponse(tree.getRoot(), node, query, false, var,level,tree.getHT(), true);
                System.out.println(result3.getKey());
                System.out.println("******************Backtracking, Equality **********");
                QueryResult result4 =  w.getProximityWorkerResponse(tree.getRoot(), node, query, true, var,level,tree.getHT(), true);
                System.out.println(result4.getKey());
                System.out.println("**********************************************");
            }
        }

    }
    static void testTraversalStartegies(int workerType,ArrayList<Double> dataset,ArrayList<Query> queries){
        int order = Parameters.defaultOrder;
        int var = 7000;
        int cost = Parameters.defaultCost;
        BTree<Double, Double> tree = buildBtree(dataset, order);
        TraversalStrategy strategy = new TraversalStrategy();
        CostCounter counter1 = new CostCounter();
        CostCounter counter2 = new CostCounter();
        CostCounter counter3 = new CostCounter();
        counter1.setValue(0);
        counter2.setValue(0);
        counter3.setValue(0);
        double queryValue=8950;
        int [] levels;
        // if(equalCostPerLevel){
        levels = strategy.getEqualCostPerLevel(cost, tree);
        //   }
        //   else{
        //  levels = getProportionalCostPerLevel(cost, tree);
        //  }
        PriorityQueue  queue = new PriorityQueue(order);
        PriorityQueueNode node = new PriorityQueueNode();
        node.setNode(tree.getRoot());
        node.setgValue(1);
        node.setScore(node.getgValue()/Math.pow(order, tree.getHT()));
        node.setRank((Double)tree.getRoot().minEntry.key);
        node.setNodeHeight(tree.getHT());
        queue.insert(node);

        //    QueryResult result1 =  strategy.searchLeafLevel(tree.getRoot(), tree.getRoot(), new Query(queryValue), false, var, tree.getHT(), tree.getHT(), counter1, workerType);
        for(int i=0;i<queries.size();i++){
            queryValue =queries.get(i).getQueryKey();
            QueryResult result2 =  strategy.searchLevelByLevel(tree.getRoot(), tree.getRoot(), new Query(queryValue), true, var, tree.getHT(), tree.getHT(), counter2, workerType, levels, Parameters.majorityAggreecation);
            QueryResult result4 =  strategy.searchLevelByLevel(tree.getRoot(), tree.getRoot(), new Query(queryValue), false, var, tree.getHT(), tree.getHT(), counter2, workerType, levels, Parameters.majorityAggreecation);
            //   QueryResult result3 =  strategy.searchAstarBounded(tree.getRoot(), queue, new Query(queryValue), true, var, counter3, workerType, levels, tree.getHT(), Parameters.majorityAggreecation);
            if(result2==null)
                System.out.println("No results retrieved");
            else{
                System.out.println("***************"+queryValue);
                System.out.println("Error Equality ="+Math.abs(result2.getKey()-queryValue));
                System.out.println("Error No Equality ="+Math.abs(result4.getKey()-queryValue));
            }
        }
    }
    // this function is tested
    static void testCostEffect(int workerType, ArrayList<Double> dataset, ArrayList<Query> queries) {
        try {
            // Create file
            FileWriter fstream = new FileWriter("costResults.csv");
            BufferedWriter out = new BufferedWriter(fstream);
            System.out
            .println("*************************Testing cost effect ****************************");
            int order = Parameters.defaultOrder;
            int var = Parameters.defaultVar;

            BTree<Double, Double> tree = buildBtree(dataset, order); // this is
            // an
            // initial
            // order of 4
            TraversalStrategy traversalStrategies = new TraversalStrategy();
            String description = "Order = " + order + ", var = " + var + ", tree height = "
                    + tree.getHT() + ", Equalitiy zone = " + Parameters.bufferZoneRatio + "\n";

            // out.write(description);

            String r = "Cost,LLEqErr,LLNoEqErr,LLEqCost,LLNoEqCost,"
                    + "LBLEqDistEqErr,LBLEqDistNoEqErr,LBLEqDistEqCost,LBLEqDistNoEqCost,"
                    + "LBLVarDistEqErr,LBLVarDistNoEqErr,LBLVarDistEqCost,LBLVarDistNoEqCost,"
                    +  "BTEqDistEqErr,BTEqDistNoEqErr,BTEqDistEqCost,BTEqDistNoEqCost\n";
            for (int cost = Parameters.minTaskCost; cost < Parameters.maxTaskCost; cost += Parameters.costStep) {
                System.out.println("Cost = " + cost);
                r = r
                        + cost
                        + ","
                        + traversalStrategies.testLeafLevel(
                                tree, queries, cost, var, workerType);
                r = r
                        + ","
                        + traversalStrategies.testLevelByLevel(
                                tree, queries, cost, var,
                                workerType, true);
                r = r
                        + ","
                        + traversalStrategies.testLevelByLevel(
                                tree, queries, cost, var,
                                workerType, false) ;
                r = r
                        + ","
                        + traversalStrategies.testAstarBoundedDirectional(
                                tree, queries, cost, var,
                                workerType, true) ;
                r = r
                        + ","
                        + traversalStrategies.testAstarBoundedDirectional(
                                tree, queries, cost, var,
                                workerType, false) + "\n";

            }
            out.write(r);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this function is tested
    static void testOrderEffect(int workerType, ArrayList<Double> dataset, ArrayList<Query> queries) {
        try {
            // Create file
            FileWriter fstream = new FileWriter("orderResults.csv");
            BufferedWriter out = new BufferedWriter(fstream);
            System.out
            .println("*************************Testing tree order effect ****************************");
            int cost = Parameters.defaultCost;
            int var = Parameters.defaultVar;

            ArrayList<BTree<Double, Double>> treeList = new ArrayList<BTree<Double, Double>>();
            for (int i = Parameters.minTreeOrder; i < Parameters.maxTreeOrder; i++)
                treeList.add(buildBtree(dataset, i)); // this is an initial
            // order of
            // 4
            TraversalStrategy traversalStrategies = new TraversalStrategy();
            System.out.println("Cost = " + cost + ", var = " + var);
            String description = "Cost = " + cost + ", var = " + var + ", Equality zone = "
                    + Parameters.bufferZoneRatio + "\n";
            // out.write(description);
            String r = "Order,LLEqErr,LLNoEqErr,LLEqCost,LLNoEqCost,"
                    + "LBLEqDistEqErr,LBLEqDistNoEqErr,LBLEqDistEqCost,LBLEqDistNoEqCost,"
                    + "LBLVarDistEqErr,LBLVarDistNoEqErr,LBLVarDistEqCost,LBLVarDistNoEqCost,"
                    + "BTEqDistEqErr,BTEqDistNoEqErr,BTEqDistEqCost,BTEqDistNoEqCost\n";
            for (int i = Parameters.minTreeOrder; i < Parameters.maxTreeOrder; i++) {

                int index = i - Parameters.minTreeOrder;
                System.out.println("order = " + i + ", tree height = "
                        + treeList.get(index).getHT());
                r = r
                        + i
                        + ","
                        + traversalStrategies.testLeafLevel(
                                treeList.get(index), queries, cost, var, workerType); // an
                r = r
                        + ","
                        + traversalStrategies.testLevelByLevel( treeList.get(index), queries,
                                cost, var, workerType, true);
                r = r
                        + ","
                        + traversalStrategies.testLevelByLevel( treeList.get(index), queries,
                                cost, var, workerType, false) ;
                r = r
                        + ","
                        + traversalStrategies.testAstarBoundedDirectional( treeList.get(index), queries, cost, var,
                                workerType, true) ;
                r = r
                        + ","
                        + traversalStrategies.testAstarBoundedDirectional( treeList.get(index), queries, cost, var,
                                workerType, false) + "\n";
            }
            out.write(r);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this function is tested
    static void testVarEffect(int workerType, ArrayList<Double> dataset, ArrayList<Query> queries) {
        try {
            // Create file
            FileWriter fstream = new FileWriter("varResults.csv");
            BufferedWriter out = new BufferedWriter(fstream);
            System.out
            .println("*************************Testing variance effect ****************************");
            int order = Parameters.defaultOrder;
            int cost = Parameters.defaultCost;

            BTree<Double, Double> tree = buildBtree(dataset, order); // this is
            // an
            // initial
            // order of 4
            TraversalStrategy traversalStrategies = new TraversalStrategy();
            System.out.println("Order = " + order + ", cost = " + cost);
            String description = "Order = " + order + ", cost = " + cost + ", Equality zone = "
                    + Parameters.bufferZoneRatio + "\n";
            // out.write(description);
            String r = "Var,LLEqErr,LLNoEqErr,LLEqCost,LLNoEqCost,"
                    + "LBLEqDistEqErr,LBLEqDistNoEqErr,LBLEqDistEqCost,LBLEqDistNoEqCost,"
                    + "LBLVarDistEqErr,LBLVarDistNoEqErr,LBLVarDistEqCost,LBLVarDistNoEqCost,"
                    + "BTEqDistEqErr,BTEqDistNoEqErr,BTEqDistEqCost,BTEqDistNoEqCost\n";
            for (int var = Parameters.minVar; var < Parameters.maxVar; var += Parameters.varStep) {
                System.out.println("Var = " + var);
                r = r
                        + var
                        + ","
                        + traversalStrategies.testLeafLevel(
                                tree, queries, cost, var, workerType); // an
                // initial
                // std
                // deviation
                // of 500
                r = r
                        + ","
                        + traversalStrategies.testLevelByLevel( tree, queries, cost, var,
                                workerType, true);
                r = r
                        + ","
                        + traversalStrategies.testLevelByLevel( tree, queries, cost, var,
                                workerType, false) ;
                r = r
                        + ","
                        + traversalStrategies.testAstarBoundedDirectional(tree, queries, cost, var,
                                workerType, true) ;
                r = r
                        + ","
                        + traversalStrategies.testAstarBoundedDirectional( tree, queries, cost, var,
                                workerType, false) + "\n";

            }
            out.write(r);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this function is tested
    public static BTree<Double, Double> buildBtree(ArrayList<Double> datasetKeys, int order) {
        BTree<Double, Double> tree = new BTree<Double, Double>(order);
        for (int i = 0; i < datasetKeys.size(); i++) {
            tree.put(new Double(datasetKeys.get(i)), new Double(datasetKeys.get(i)));
        }
        return tree;
    }

    // this function is tested
    public static ArrayList<Double> buildUniformDataSet(String fileName) {
        ArrayList<Double> uniformDataSet = new ArrayList<Double>();
        try {
            // Create file
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);

            for (int i = 1; i <= Parameters.maxUniformDataSet; i++) {
                uniformDataSet.add(new Double(i));
                out.write("" + new Double(i) + "\n");
            }
            out.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        return uniformDataSet;
    }

    // this function is tested
    public static ArrayList<Query> buildUniformDataSetQueries(String fileName) {
        ArrayList<Query> uniformDataSetQuereis = new ArrayList<Query>();
        try {
            // Create file
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);
            int queriesStep = Parameters.maxUniformDataSet / Parameters.numberOfQueries;
            for (double i = 1; i <= Parameters.numberOfQueries; i++) {
                uniformDataSetQuereis.add(new Query(i * queriesStep));
                out.write("" + new Double(i * queriesStep) + "\n");
            }
            out.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        return uniformDataSetQuereis;
    }

    // this function is tested
    public static ArrayList<Double> buildRandomDataSet(String fileName) {
        ArrayList<Double> dataset = new ArrayList<Double>();
        try {
            // Create file
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);
            for (int i = 0; i < Parameters.datasetSize; i++) {
                double val = randomGenerator.getRandomInt(0, Parameters.datasetSizeRange);
                // System.out.println(val);
                out.write("" + val + "\n");
                dataset.add(val);
            }
            out.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        return dataset;

    }

    // this function is tested
    public static ArrayList<Double> buildSkewdDataSet(String fileName) {

        ArrayList<Double> dataset = new ArrayList<Double>();
        try {
            // Create file
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);

            for (int i = 0; i < Parameters.datasetSize; i++) {
                double val = randomGenerator.getGaussian(Parameters.skewdDataSetMean,
                        Parameters.skewdDaraSetVar);
                out.write("" + val + "\n");
                // System.out.println(val);
                dataset.add(val);
            }
            out.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        return dataset;
    }

    // this function is tested
    public static ArrayList<Query> buildRandomQueries(ArrayList<Double> dataset, String fileName) {
        ArrayList<Query> queries = new ArrayList<Query>();
        try {
            // Create file
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);

            for (int i = 0; i < Parameters.numberOfQueries; i++) {
                int index = randomGenerator.getRandomInt(0, dataset.size());
                // System.out.println(index);

                queries.add(new Query(dataset.get(index)));
                out.write("" + dataset.get(index) + "\n");
            }
            out.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        return queries;
    }

    // this function is tested
    private static ArrayList<Double> readDataFromFile(String fileName) {
        ArrayList<Double> dataset = new ArrayList<Double>();
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(fileName);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                dataset.add(new Double(strLine));
            }

            in.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }

        return dataset;
    }

    // this function is tested
    private static ArrayList<Query> readQueriesFromFile(String fileName) {
        ArrayList<Query> queries = new ArrayList<Query>();
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(fileName);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                queries.add(new Query(new Double(strLine)));
            }

            in.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }

        return queries;
    }

    public static ArrayList<Double> buildCarsDataSet(String fileName) {
        ArrayList<Double> carsDataSet = new ArrayList<Double>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = connBuilder.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select  id,picNum,price,title from crowdindex.datasetinfo  ;");
            while (rs.next()) {
                String price = "" + rs.getString("price");
                if (price.contains("$"))
                    price = price.substring(1);
                if (price.contains(" "))
                    price = price.substring(0, price.indexOf(" "));
                if (price.contains("<"))
                    price = price.substring(0, price.indexOf("<"));
                try {
                    Double p = new Double(price);
                    carsDataSet.add(p);
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                    // logger.log("Invalid number" );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            connBuilder.closeEveryThing(con, stmt, rs);
        }
        Collections.sort(carsDataSet) ;
        try {
            // Create file
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);
            for (int i =0;i< carsDataSet.size();i++){
                if(carsDataSet.get(i)>=200&&carsDataSet.get(i)<=500000)
                    out.write(""+carsDataSet.get(i)+"\n");
            }
            out.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        return carsDataSet;
    }

    public static ArrayList<Query> buildCarsDataSetQueries() {
        ArrayList<Query> carsDataSetQuereis = new ArrayList<Query>();
        for (double i = 1000; i <= 7000; i += 30)
            carsDataSetQuereis.add(new Query(i));
        return carsDataSetQuereis;
    }

}
