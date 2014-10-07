package edu.purdue.crowdindex.workersimulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import edu.purdue.crowdindex.server.index.BTree;
import edu.purdue.crowdindex.server.index.Node;

public class TraversalStrategy {

    public enum MaxFreqType {
        DEFAULT, CHILD_BRANCH, NODE_KEY, SMALLER_BOUND_KEY, HIGHER_BOUND_KEY,
        SMALLER_BACKTRACKING, HIGHER_BACKTRACKING
    }
    private final static Logger logger = Logger.getLogger(TraversalStrategy.class
            .getName());
    public TraversalStrategy(){
        try {
            FileHandler fileHandler = new FileHandler("myapp-log.%u.%g.txt",
                    true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // this is a test of leaf level aggregation with a specific number of workers

    public String testLeafLevel( BTree<Double, Double> tree, ArrayList<Query> queryList,
            int cost, double probSeed,int workerType) {
        int numberOfWorkers = getNumberOfWorkers(tree.height(), cost);

        // testing with equality and may not need to reach, this may result in a
        // lower actual cost
        double errorEqSum=0;
        double realCostEqSum=0;
        double errorNoEqSum=0;
        double realCostNoEqSum=0;
        int numOfQueries=0;
        for (int i = 0; i < queryList.size()&&i<Parameters.numberOfQueries; i++) {
            numOfQueries++;
            ArrayList<QueryResult> workerResultsEquality = new ArrayList<QueryResult>();
            ArrayList<QueryResult> workerResultsNoEquality = new ArrayList<QueryResult>();
            CostCounter realCostEq = new CostCounter();
            CostCounter realCostNoEq = new CostCounter();

            for (int j = 0; j < numberOfWorkers; j++) {
                workerResultsEquality.add(searchLeafLevel(tree.getRoot(),tree.getRoot(), queryList.get(i), true,
                        probSeed, tree.height(), tree.height(),realCostEq,workerType)); // testing with equality
                workerResultsNoEquality.add(searchLeafLevel(tree.getRoot(),tree.getRoot(), queryList.get(i),
                        false, probSeed, tree.height(),tree.height(), realCostNoEq,workerType)); // testing without equality
            }
            QueryResult finalResultMajorityEq = aggrecateResultsLeafLevel(workerResultsEquality,
                    Parameters.majorityAggreecation);

            errorEqSum+=Math.abs(finalResultMajorityEq.getKey()-queryList.get(i).getQueryKey());
            realCostEqSum+=realCostEq.getValue();

            QueryResult finalResultMajorityNoEq = aggrecateResultsLeafLevel(workerResultsNoEquality,
                    Parameters.majorityAggreecation);
            errorNoEqSum+=Math.abs(finalResultMajorityNoEq.getKey()-queryList.get(i).getQueryKey());
            realCostNoEqSum+=realCostNoEq.getValue();

        }
        errorEqSum = errorEqSum/numOfQueries;
        realCostEqSum= realCostEqSum/numOfQueries;
        errorNoEqSum= errorNoEqSum/numOfQueries;
        realCostNoEqSum = realCostNoEqSum/numOfQueries;
        //        System.out.println("Leaf level equality: error:"+errorEqSum+", real cost:"+realCostEqSum);
        //        System.out.println("Leaf level NO equality: error:"+errorNoEqSum+", real cost:"+realCostNoEqSum);
        String result = ""+errorEqSum+","+errorNoEqSum+","+realCostEqSum+","+realCostNoEqSum;
        return result;
    }

    public String testLevelByLevel(BTree<Double, Double> tree, ArrayList<Query> queryList, int cost, double probSeed,int workerType,boolean equalCostPerLevel) {
        // test with equality cost distribution per level
        // testing with equality and may not need to reach, this may result in a
        // lower actual cost
        double errorEqSum=0;
        double realCostEqSum=0;
        double errorNoEqSum=0;
        double realCostNoEqSum=0;
        int [] levels;
        if(equalCostPerLevel){
            levels = getEqualCostPerLevel(cost, tree);
        }
        else{
            levels = getProportionalCostPerLevel(cost, tree);
        }

        QueryResult result ;
        int numOfQueries=0;
        for (int i = 0; i < queryList.size() && i<Parameters.numberOfQueries; i++) {
            numOfQueries++;
            CostCounter realCost = new CostCounter();
            result = searchLevelByLevel(tree.getRoot(),tree.getRoot(), queryList.get(i), true, probSeed, tree.getHT(),tree.getHT(), realCost, workerType, levels, Parameters.majorityAggreecation);
            errorEqSum+=Math.abs(result.getKey()-queryList.get(i).getQueryKey());
            realCostEqSum+=realCost.getValue();
        }
        // testing without equality and must reach leaf level
        for (int i = 0; i < queryList.size()&& i<Parameters.numberOfQueries; i++) {
            CostCounter realCost = new CostCounter();
            result = searchLevelByLevel(tree.getRoot(),tree.getRoot(), queryList.get(i), false, probSeed, tree.getHT(), tree.getHT(),realCost, workerType, levels, Parameters.majorityAggreecation);

            errorNoEqSum+=Math.abs(result.getKey()-queryList.get(i).getQueryKey());
            realCostNoEqSum+=realCost.getValue();
        }

        errorEqSum = errorEqSum/numOfQueries;
        realCostEqSum= realCostEqSum/numOfQueries;
        errorNoEqSum= errorNoEqSum/numOfQueries;
        realCostNoEqSum = realCostNoEqSum/numOfQueries;
        String description ="";
        if(equalCostPerLevel)
            description = "Equal Cost Per Level";
        else
            description = "Proportional Cost Per Level";
        //        System.out.println("LevelByLevel equality: "+description+" error:"+errorEqSum+", real cost:"+realCostEqSum);
        //        System.out.println("LevelByLevel NO equality: "+description+"error:"+errorNoEqSum+", real cost:"+realCostNoEqSum);
        String r = ""+errorEqSum+","+errorNoEqSum+","+realCostEqSum+","+realCostNoEqSum;
        return r;

    }
    public int [] getEqualCostPerLevel(int cost, BTree<Double, Double> tree){
        int [] levels = new int [tree.getHT()+1];
        for(int i =0;i<tree.getHT()+1;i++){
            levels[i]= cost/(tree.getHT()+1);
        }
        return levels;
    }
    private int [] getProportionalCostPerLevel(int cost, BTree<Double, Double> tree){
        int [] levels =  new int [tree.getHT()+1];
        //Assign at least one worker per level then distribute the number of workers based on the number of level
        //(first term + last term)*totalNumber /2
        int totalLevelSum = (1+tree.getHT()+1)*(tree.getHT()+1)/2;
        for(int i =0;i<tree.getHT()+1;i++){
            levels[i]=1+(cost-tree.getHT()-1)*(i+1)/totalLevelSum;
        }



        return levels;
    }
    public QueryResult searchLevelByLevel(Node root, Node node, Query query, boolean equality, double probSeed,
            int height,int treeHeight, CostCounter realCost,int workerModel,int [] workersPerLevelArr , int aggrMethod){
        int numOfWorkers = workersPerLevelArr[height];
        ArrayList<QueryResult> workerResults = new ArrayList<QueryResult>();
        Worker w = new Worker();
        for(int i =0;i<numOfWorkers;i++){
            realCost.increment();
            workerResults.add( w.getWorkerResponse(root, node, query, equality, probSeed, height,treeHeight, workerModel, false));
        }
        QueryResult finalResult = aggrecateResultsNodeLevelByLevel(workerResults, aggrMethod);
        if(height ==0){
            return finalResult;
        }else{
            if (finalResult.getNode() != null)
                return searchLevelByLevel(root,finalResult.getNode(), query, equality, probSeed, height - 1,treeHeight,
                        realCost,workerModel,workersPerLevelArr,aggrMethod);
            else
                return finalResult;
        }

    }
    public QueryResult searchAstarBounded(Node root,PriorityQueue queue, Query query, boolean equality, double probSeed,
            CostCounter realCost,int workerModel,int [] workersPerLevelArr , int treeHeight,int aggrMethod){
        PriorityQueueNode topQueueNode ;
        int height;
        ArrayList<QueryResult> workerResults;
        while(queue.size()!=0){
            topQueueNode = queue.getTop();
            height =topQueueNode.getNodeHeight();
            int numOfWorkers = workersPerLevelArr[height];
            Node node = topQueueNode.getNode();

            workerResults = new ArrayList<QueryResult>();
            Worker w = new Worker();
            for(int i =0;i<numOfWorkers;i++){
                realCost.increment();
                workerResults.add( w.getWorkerResponse(root,node, query, equality, probSeed, height,treeHeight, workerModel,true));

            }


            QueryResult finalResult = expandNodeBasedOnWorkerResponses(queue, workerResults,topQueueNode,equality);

            if(finalResult!=null&&finalResult.getKey()!=null ){
                return finalResult;
            }
            //otherwise we should continue with node extraction
        }
        return null; //unable to reach a result


    }
    private QueryResult expandNodeBasedOnWorkerResponses(PriorityQueue queue, ArrayList<QueryResult> resultList,PriorityQueueNode  topQueueNode,boolean equality){
        //the total number of votes is equal to the size of the result list, this is important in the calculations of the probabilities for node branches in the
        //100 is a very large number to represent the content of a node
        int []childFrequenceis = new int[100];
        int []keyFrequenceis = new int[100];
        int higherBackTrackingFreq =0; //this is to count how many decisions require higherBacktracking
        int smallerBackTrackingFreq =0; //this is to count how many decisions require smallerBackTracking
        int higherBoundCount =0; //in the case of equality enabled, how many decisions are equal to the higher bound of the subtree of the current node
        int smallerBoundCount=0; //in the case of equality enabled, how many decisions are equal to the lower bound of the subtree of the current node
        //initialization of counters
        for(int i =0;i<100;i++){
            childFrequenceis[i]=0;
            keyFrequenceis [i]=0;
        }
        for(int i =0;i<resultList.size();i++){
            if(resultList.get(i).getNode()!=null) //this is a decision to branch to a child node
                childFrequenceis[resultList.get(i).getIndexWithinNode()]++;
            else if(resultList.get(i).isHigherBackTracking()) //this is a backtracking to the higher side of the node
                higherBackTrackingFreq++;
            else if (resultList.get(i).isSmallerBackTracking()) //this is a backTracking to the lower side of the node
                smallerBackTrackingFreq++;
            else if (resultList.get(i).getIndexWithinNode()==Parameters.higherBoundIndex) //this is equality to the higher bound of the node
                higherBoundCount++;
            else if  (resultList.get(i).getIndexWithinNode()==Parameters.smallerBoundIndex) //this is equality to the smaller bound of the node
                smallerBoundCount++;
            else
                keyFrequenceis[resultList.get(i).getIndexWithinNode()]++; //this is equality to the
        }
        //find the maximum frequencies
        int maxloc=0;
        int maxFreq=0;
        MaxFreqType maxFreqType = MaxFreqType.DEFAULT;
        //when equality, giving precedence to a child branch, then a node key, then a node bound, then backtracking
        if(higherBackTrackingFreq>maxFreq){
            maxFreq = higherBackTrackingFreq;
            maxFreqType = MaxFreqType.HIGHER_BACKTRACKING;
        }
        if (smallerBackTrackingFreq>maxFreq){
            maxFreq = smallerBackTrackingFreq;
            maxFreqType = MaxFreqType.SMALLER_BACKTRACKING;
        }
        if(higherBoundCount >maxFreq){
            maxFreq = higherBoundCount;
            maxFreqType = MaxFreqType.HIGHER_BOUND_KEY;
        }
        if(smallerBoundCount>maxFreq)  {
            maxFreq = smallerBoundCount;
            maxFreqType = MaxFreqType.SMALLER_BOUND_KEY;
        }
        for(int i =0;i<100;i++){
            if(keyFrequenceis[i]>maxFreq){
                maxFreq= keyFrequenceis[i];
                maxloc = i;
                maxFreqType = MaxFreqType.NODE_KEY;

            }
        }
        for(int i =0;i<100;i++){
            if(childFrequenceis[i]>maxFreq){
                maxFreq= childFrequenceis[i];
                maxloc = i;
                maxFreqType = MaxFreqType.CHILD_BRANCH;
            }
        }
        ArrayList<QueryResult> finalResults = new ArrayList<QueryResult>();
        //step 1 check if a final decision at a key is reached then return it and it is done
        if(maxFreqType ==MaxFreqType.NODE_KEY ){
            for(int i =0;i<100;i++){
                if(keyFrequenceis[i]==maxFreq){
                    for(int j =0;j<resultList.size();j++){
                        if(resultList.get(j).getNode()==null&&resultList.get(j).getIndexWithinNode()==i)
                            finalResults.add(resultList.get(j));
                    }
                }
            }
            return finalResults.get(finalResults.size()/2);
        }
        else if(maxFreqType == MaxFreqType.HIGHER_BOUND_KEY){
            for(int j =0;j<resultList.size();j++){
                if(resultList.get(j).getNode()==null&&resultList.get(j).getIndexWithinNode()==Parameters.higherBoundIndex&&!resultList.get(j).isHigherBackTracking())
                    return (resultList.get(j));
            }

        }
        else if(maxFreqType == MaxFreqType.SMALLER_BOUND_KEY){
            for(int j =0;j<resultList.size();j++){
                if(resultList.get(j).getNode()==null&&resultList.get(j).getIndexWithinNode()==Parameters.smallerBoundIndex&&!resultList.get(j).isSmallerBackTracking())
                    return (resultList.get(j));
            }

        }

        //step 2 add other node content to the the priority queue, with proper score.
        //we add all child branches
        //nodes chosen from workers have a higher score
        //this is dones only for non leaf nodes
        if(equality){
            if(topQueueNode.getNodeHeight()>0){
                Node node = topQueueNode.getNode();
                int numOfBranches = node.m;
                double scoreDenom= resultList.size()+numOfBranches;
                for(int i =0;i<numOfBranches;i++){
                    PriorityQueueNode queueNode= new PriorityQueueNode();
                    queueNode.setNode(node.children[i].next);
                    queueNode.setRank((Double)node.children[i].next.minEntry.key);
                    queueNode.setNodeHeight(topQueueNode.getNodeHeight()-1);
                    queueNode.setgValue(topQueueNode.getgValue()*(childFrequenceis[i*2]+1)/scoreDenom);
                    queueNode.setScore(queueNode.getgValue()/Math.pow(queue.order, topQueueNode.getNodeHeight()-1));
                    queue.insert(queueNode);
                }
            }
        }
        else{
            if(topQueueNode.getNodeHeight()>0){
                Node node = topQueueNode.getNode();
                int numOfBranches = node.m;
                double scoreDenom= resultList.size()+numOfBranches;
                for(int i =0;i<numOfBranches;i++){
                    PriorityQueueNode queueNode= new PriorityQueueNode();
                    queueNode.setNode(node.children[i].next);
                    queueNode.setRank((Double)node.children[i].next.minEntry.key);
                    queueNode.setNodeHeight(topQueueNode.getNodeHeight()-1);
                    queueNode.setgValue(topQueueNode.getgValue()*(childFrequenceis[i]+1)/scoreDenom);
                    queueNode.setScore(queueNode.getgValue()/Math.pow(queue.order, topQueueNode.getNodeHeight()-1));
                    queue.insert(queueNode);
                }
            }
        }
        //step 3 check the direction of backtracking and update the scores of nodes accordingly
        PriorityQueue.Direction direction ;
        if(maxFreqType == MaxFreqType.HIGHER_BACKTRACKING){
            direction =  PriorityQueue.Direction.higher;
            queue.updateScore(Parameters.scoreUpdateFactor, (Double)topQueueNode.getNode().maxEntry.key,direction);
        }
        else if(maxFreqType == MaxFreqType.SMALLER_BACKTRACKING){
            direction =  PriorityQueue.Direction.smaller;
            queue.updateScore(Parameters.scoreUpdateFactor, (Double)topQueueNode.getNode().minEntry.key,direction);
        }









        return null;


    }

    public String testAstarBoundedDirectional(BTree<Double, Double> tree, ArrayList<Query> queryList, int cost, double probSeed,int workerType,boolean equalCostPerLevel) {
        // test with equality cost distribution per level
        // testing with equality and may not need to reach, this may result in a
        // lower actual cost
        double errorEqSum=0;
        double realCostEqSum=0;
        double errorNoEqSum=0;
        double realCostNoEqSum=0;
        int [] levels;
        if(equalCostPerLevel){
            levels = getEqualCostPerLevel(cost, tree);
        }
        else{
            levels = getProportionalCostPerLevel(cost, tree);
        }

        QueryResult result ;
        PriorityQueue  queue = new PriorityQueue(tree.getOrder());
        PriorityQueueNode node;
        int numOfQueries =0;
        for (int i = 0; i < queryList.size()&&i<Parameters.numberOfQueries; i++) {
            numOfQueries++;
            node = new PriorityQueueNode();
            node.setNode(tree.getRoot());
            node.setgValue(1);
            node.setScore(node.getgValue()/Math.pow(queue.order, tree.getHT()));
            node.setRank((Double)tree.getRoot().minEntry.key);
            node.setNodeHeight(tree.getHT());
            queue.insert(node);
            CostCounter realCost = new CostCounter();
            result = searchAstarBounded(tree.getRoot(),queue, queryList.get(i), true, probSeed,  realCost, workerType, levels, tree.getHT(),Parameters.majorityAggreecation);
            if(result!=null){
                errorEqSum+=Math.abs(result.getKey()-queryList.get(i).getQueryKey());
                realCostEqSum+=realCost.getValue();
                //  System.out.println("Result retrieved ");
            }else{
                System.out.println("Error: No result retrieved from backtracking");
            }
            queue.clear();
        }
        // testing without equality and must reach leaf level

        for (int i = 0; i < queryList.size()&&i<Parameters.numberOfQueries; i++) {
            node = new PriorityQueueNode();
            node.setNode(tree.getRoot());
            node.setgValue(1);
            node.setScore(node.getgValue()/Math.pow(queue.order, tree.getHT()));
            node.setRank((Double)tree.getRoot().minEntry.key);
            node.setNodeHeight(tree.getHT());
            queue.insert(node);
            CostCounter realCost = new CostCounter();
            result = searchAstarBounded(tree.getRoot(),queue, queryList.get(i), false, probSeed,  realCost, workerType, levels,tree.getHT(), Parameters.majorityAggreecation);
            if(result!=null){
                errorNoEqSum+=Math.abs(result.getKey()-queryList.get(i).getQueryKey());
                realCostNoEqSum+=realCost.getValue();
                //   System.out.println("Result retrieved ");
            }else{
                System.out.println("Error: No result retreieved from backtracking");
            }
            queue.clear();
        }


        errorEqSum = errorEqSum/numOfQueries;
        realCostEqSum= realCostEqSum/numOfQueries;
        errorNoEqSum= errorNoEqSum/numOfQueries;
        realCostNoEqSum = realCostNoEqSum/numOfQueries;
        String description ="";
        if(equalCostPerLevel)
            description = "Equal Cost Per Level";
        else
            description = "Proportional Cost Per Level";
        //        System.out.println("LevelByLevel equality: "+description+" error:"+errorEqSum+", real cost:"+realCostEqSum);
        //        System.out.println("LevelByLevel NO equality: "+description+"error:"+errorNoEqSum+", real cost:"+realCostNoEqSum);
        String r = ""+errorEqSum+","+errorNoEqSum+","+realCostEqSum+","+realCostNoEqSum;
        return r;

    }



    public QueryResult searchLeafLevel(Node root,Node node, Query query, boolean equality, double probSeed,
            int height, int treeHeight,CostCounter realCost,int workerModel) {
        realCost.increment();
        Worker w = new Worker();
        QueryResult result = null;

        result = w.getWorkerResponse(root,node, query, equality, probSeed, height,treeHeight,workerModel,false); //here the probSeed is the probability of a correct selection

        if (height == 0) { // this means a leaf node
            return result; // this is a leaf level result, return result
            // directly should not proceed any further

        }
        // non leaf level
        else {
            if (result.getNode() != null)
                return searchLeafLevel(root,result.getNode(), query, equality, probSeed, height - 1,treeHeight,
                        realCost,workerModel);
            else
                return result;

        }
    }

    private QueryResult aggrecateResultsLeafLevel(ArrayList<QueryResult> resultList, int aggrMethod) {
        if (aggrMethod == Parameters.majorityAggreecation) {
            return  aggrecateResultsMajorityLeafLevel( resultList) ;
        } else if (aggrMethod == Parameters.distributionAggreecation) {
            return  aggrecateResultsDistribution(resultList) ;
        }
        return null;
    }

    private QueryResult aggrecateResultsNodeLevelByLevel(ArrayList<QueryResult> resultList, int aggrMethod) {
        if (aggrMethod == Parameters.majorityAggreecation) {
            return  aggrecateResultsMajorityNodeLevelByLevel( resultList) ;
        } else if (aggrMethod == Parameters.distributionAggreecation) {
            return  aggrecateResultsDistribution(resultList) ;
        }
        return null;
    }

    private QueryResult aggrecateResultsMajorityLeafLevel(ArrayList<QueryResult> resultList) {
        HashMap<Double, Integer> frequenceis = new HashMap<Double, Integer>();

        for(int i =0;i<resultList.size();i++){
            Double key = resultList.get(i).getKey();
            if(!frequenceis.containsKey(key))
                frequenceis.put(key, 1);
            else
                frequenceis.put(key,frequenceis.get(key)+1);
        }
        //find the maximum frequencies
        Double maxKey=0.0;
        int maxFreq=-1;
        Iterator it = frequenceis.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Double, Integer> pair = (Map.Entry<Double, Integer>)it.next();
            if(pair.getValue()> maxFreq){
                maxKey=pair.getKey();
                maxFreq = pair.getValue();
            }
        }
        //find all locations with max frequencies and select the median
        ArrayList<Double> maxKeys = new ArrayList<Double>();
        it =  frequenceis.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Double, Integer> pair = (Map.Entry<Double, Integer>)it.next();
            if(pair.getValue().equals(maxFreq))
                maxKeys.add(pair.getKey());
        }
        Collections.sort(maxKeys);
        maxKey = maxKeys.get(maxKeys.size()/2);
        //find the query result corresponding the best max location
        for(int i =0;i<resultList.size();i++){
            if(resultList.get(i).getKey().equals(maxKey))
                return resultList.get(i);
        }
        return null;
    }

    private QueryResult aggrecateResultsMajorityNodeLevelByLevel(ArrayList<QueryResult> resultList) {
        //100 is a very large number to the content of a node
        int []childFrequenceis = new int[100];
        int []keyFrequenceis = new int[100];
        for(int i =0;i<100;i++){
            childFrequenceis[i]=0;
            keyFrequenceis [i]=0;
        }
        for(int i =0;i<resultList.size();i++){
            if(resultList.get(i).getNode()!=null)
                childFrequenceis[resultList.get(i).getIndexWithinNode()]++;
            else
                keyFrequenceis[resultList.get(i).getIndexWithinNode()]++;
        }
        //find the maximum frequencies
        int maxloc=0;
        int maxFreq=-1;
        boolean childFreqIsMax = true;
        for(int i =0;i<100;i++){
            if(childFrequenceis[i]>maxFreq){
                maxFreq= childFrequenceis[i];
                maxloc = i;
                childFreqIsMax = true;
            }
        }
        for(int i =0;i<100;i++){
            if(keyFrequenceis[i]>maxFreq){
                maxFreq= keyFrequenceis[i];
                maxloc = i;
                childFreqIsMax = false;
            }
        }
        //find all locations with max frequencies and select the median
        ArrayList<Integer> maxLocationsChild = new ArrayList<Integer>();
        ArrayList<Integer> maxLocationsKeys = new ArrayList<Integer>();
        ArrayList<QueryResult> maxFreqResults = new ArrayList<QueryResult>();
        for(int i =0;i<100;i++){
            if(childFrequenceis[i]==maxFreq){
                maxLocationsChild.add(i);
                for(int j=0;j<resultList.size();j++){
                    if(resultList.get(j).getNode()!=null&&resultList.get(j).getIndexWithinNode()==i){
                        maxFreqResults.add(resultList.get(j));
                        break;
                    }
                }
            }
            if(keyFrequenceis[i]==maxFreq){
                maxLocationsKeys.add(i);
                for(int j=0;j<resultList.size();j++){
                    if(resultList.get(j).getNode()==null&&resultList.get(j).getIndexWithinNode()==i){
                        maxFreqResults.add(resultList.get(j));
                        break;
                    }
                }
            }
        }



        return maxFreqResults.get(maxFreqResults.size()/2);

    }
    private QueryResult aggrecateResultsDistribution(ArrayList<QueryResult> resultList) {
        return null;
    }

    private int getNumberOfWorkers(int height, int cost) {
        return cost / (height+1);
    }

    private String generateLoggingMessage(int dataSet, int treeOrder, int treeHeight,
            int traversalStrategy, int equality, int aggrecationMethod, double queryKey,
            double returnedResult, int initialCost, int realCost) {
        return "" + dataSet + "," + treeOrder + "," + treeHeight + "," + traversalStrategy + ","
                + equality + "," + aggrecationMethod + "," + queryKey + "," + returnedResult + ","
                + initialCost + "," + realCost;
    }
}
