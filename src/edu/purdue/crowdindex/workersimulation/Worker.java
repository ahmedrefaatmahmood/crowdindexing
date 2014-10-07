package edu.purdue.crowdindex.workersimulation;

import java.util.ArrayList;

import edu.purdue.crowdindex.server.index.Node;

public class Worker {
    RandomGenerator fRandom;


    public Worker(){
        fRandom =RandomGenerator.getInstance(); //new Random(Parameters.randomGeneratorSeed);
    }
    public QueryResult getWorkerResponse(Node root,Node node,Query query,boolean equalityShortCut, double probSeed,int level,int treeHeight, int workerModel,boolean backtracking){
        if(backtracking&&workerModel==Parameters.normalWorker)
            return getNormalWorkerResponseWithBacktracking(root,node, query, equalityShortCut, probSeed, level,backtracking);
        else if(workerModel==Parameters.normalWorker)
            return getNormalWorkerResponse(root,node, query, equalityShortCut, probSeed, level,backtracking);
        else if (workerModel==Parameters.normalWorker2)
            return getNormalWorkerResponse2(root,node, query, equalityShortCut, probSeed, level,backtracking);
        else if(workerModel == Parameters.proximityWorker)
            return getProximityWorkerResponse(root,node, query, equalityShortCut, probSeed, level,treeHeight,backtracking);
        return null;
    }



    /**
     * 
     * @param node
     * @param query
     * @param equalityShortCut
     * @param probSeed this parameter give an initial seed for selecting the correct answer
     * @return
     */
    public QueryResult getProximityWorkerResponse(Node root,Node node,Query query,boolean equalityShortCut, double probSeed,int level,int treeHeight,boolean backtracking){
        ArrayList<QueryResult> resultList = getNodeResultContent(node, equalityShortCut, level,backtracking);
        ArrayList<Double> probablities ;
        if(level==0){
            Double stdDev = probSeed/(Math.pow(2, treeHeight));//Math.sqrt(probSeed);
            double preceivedValue = RandomGenerator.getInstance().getGaussian(query.queryKey, stdDev);
            int minloc =-1;
            double minDis = 1000000000000000.0;
            for(int i =0;i<node.m;i++){
                double dist = Math.abs(preceivedValue-(Double)node.children[i].key);
                if(dist<minDis){
                    minDis = dist;
                    minloc = i;
                }
            }
            return new QueryResult((Double) node.children[minloc].key,
                    (Double) node.children[minloc].key, minloc);

        }

        probablities = calculateProbablities(root,node,query,probSeed, level,equalityShortCut,backtracking);
        if(probablities.size()!=resultList.size())
            System.out.println("Size of probablites and node content do not match");

        double sum =0;
        for(int i=0;i<probablities.size();i++){
            sum = sum+probablities.get(i);
        }
        //this is not a good solution
        for(int i=0;i<probablities.size();i++){
            probablities.set(i, probablities.get(i)/sum);
            //    System.out.println(probablities.get(i));
        }

        for(int i=1;i<probablities.size();i++){
            probablities.set(i, probablities.get(i)+probablities.get(i-1));
        }

        double randomVal = fRandom.getNextDouble();
        int i =0;
        while(randomVal>probablities.get(i)&&i<probablities.size()){
            i++;
        }
        return resultList.get(i);
    }

    /**
     * 
     * @param node
     * @param query
     * @param equalityShortCut
     * @param probSeed this parameter is the standard deviation of the normal distribution used in the calculations
     * @return
     */

    public QueryResult getNormalWorkerResponse(Node root,Node node,Query query,boolean equalityShortCut, double probSeed,int level,boolean backtracking){
        double precievedValue =  fRandom.getGaussian(query.getQueryKey(), probSeed);
        if(level==0){ //select the value closest to the perceived value
            int minloc =-1;
            double minDis = 1000000000000000.0;
            for(int i =0;i<node.m;i++){
                double dist = Math.abs(precievedValue-(Double)node.children[i].key);
                if(dist<minDis){
                    minDis = dist;
                    minloc = i;
                }
            }
            return new QueryResult((Double) node.children[minloc].key,
                    (Double) node.children[minloc].key, minloc);
        }
        else{
            if(equalityShortCut){//for equality shortcut, check if the perceived value of the query key falls with in the equality zone
                double equalityZone = Parameters.bufferZoneRatio;
                for (int j = 1; j < node.m; j++) {
                    if(Math.abs(precievedValue-(Double)node.children[j].key)<equalityZone){
                        return new QueryResult((Double) node.children[j].key,
                                (Double) node.children[j].key, j);
                    }
                }
            }
            //select the child where the perceived value of the query key fits in
            for (int j = 0; j < node.m; j++) {
                if (j + 1 == node.m || (precievedValue<(Double)node.children[j + 1].key)){
                    return new QueryResult(node.children[j].next, j);
                }
            }

        }
        return null;
    }
    public QueryResult getNormalWorkerResponse2(Node root, Node node,Query query,boolean equalityShortCut, double probSeed,int level,boolean backtracking){
        Double nodeRange =  (Double) node.maxEntry.key-(Double)node.minEntry.key;
        Double treeRange = (Double) root.maxEntry.key-(Double)root.minEntry.key;

        double precievedValue =  fRandom.getGaussian(query.getQueryKey(), probSeed*treeRange/nodeRange);
        if(level==0){ //select the value closest to the perceived value
            int minloc =0;
            double minDis = Math.abs(precievedValue-(Double)node.children[0].key);
            for(int i =1;i<node.m;i++){
                double dist = Math.abs(precievedValue-(Double)node.children[i].key);
                if(dist<minDis){
                    minDis = dist;
                    minloc = i;
                }
            }
            return new QueryResult((Double) node.children[minloc].key,
                    (Double) node.children[minloc].key, minloc);
        }
        else{
            if(equalityShortCut){//for equality shortcut, check if the perceived value of the query key falls with in the equality zone
                double equalityZone = Parameters.bufferZoneRatio;
                for (int j = 1; j < node.m; j++) {
                    if(Math.abs(precievedValue-(Double)node.children[j].key)<equalityZone){
                        return new QueryResult((Double) node.children[j].key,
                                (Double) node.children[j].key, j);
                    }
                }
            }
            //select the child where the perceived value of the query key fits in
            for (int j = 0; j < node.m; j++) {
                if (j + 1 == node.m || (precievedValue<(Double)node.children[j + 1].key)){
                    return new QueryResult(node.children[j].next, j);
                }
            }

        }
        return null;
    }

    public QueryResult getNormalWorkerResponseWithBacktracking(Node root,Node node,Query query,boolean equalityShortCut, double probSeed,int level,boolean backtracking){
        double precievedValue =  fRandom.getGaussian(query.getQueryKey(), probSeed);

        //this is how to deal with the leaf level with back tracking,
        // we allow backtracking even at the leaf level
        //no equality with boundary keys at leaf level, as boundary keys are within the content of the node
        if(level==0){ //select the value closest to the perceived value
            int minloc =-1;
            double minDis = 1000000000000.0;
            /*
            //One important questions is, should we back track on the leaf level or not ?
            if(precievedValue<(Double)node.minEntry.key) {
                QueryResult res = new QueryResult(null, Parameters.smallerBoundIndex);
                res.setSmallerBackTracking(true); //this means backtracking is required at the smaller side of the node
                return res;

            }
            else if(precievedValue>(Double)node.minEntry.key) {
                QueryResult res = new QueryResult(null, Parameters.higherBoundIndex);
                res.setHigherBackTracking(true);//this means backtracking is required at the higher side of the node
                return res;
            }
            else{ //this is if the perceived value is within the nodes range
             */
            for(int i =0;i<node.m;i++){
                double dist = Math.abs(precievedValue-(Double)node.children[i].key);
                if(dist<minDis){
                    minDis = dist;
                    minloc = i;
                }
            }
            return new QueryResult((Double) node.children[minloc].key,
                    (Double) node.children[minloc].key, minloc);
            // }
        }
        else{ //this is non-leaf levels
            if(equalityShortCut){//for equality shortcut, check if the perceived value of the query key falls with in the equality zone
                //we also need to check equality with the boundaries of the node
                double equalityZone = Parameters.bufferZoneRatio;
                if(Math.abs(precievedValue-(Double)node.minEntry.key)<equalityZone)
                    return new QueryResult((Double)node.minEntry.key,(Double)node.minEntry.key, Parameters.smallerBoundIndex);
                else if (Math.abs(precievedValue-(Double)node.maxEntry.key)<equalityZone)
                    return new QueryResult((Double)node.maxEntry.key,(Double)node.maxEntry.key, Parameters.higherBoundIndex);
                for (int j = 1; j < node.m; j++) {
                    if(Math.abs(precievedValue-(Double)node.children[j].key)<equalityZone){
                        return new QueryResult((Double) node.children[j].key,
                                (Double) node.children[j].key, j);
                    }
                }
            }
            //here we should backtrack to the left of the tree
            if(precievedValue<(Double)node.minEntry.key) {
                QueryResult res = new QueryResult(null, Parameters.smallerBoundIndex);
                res.setSmallerBackTracking(true);
                return res;

            }
            //here we should backtrack to the righ of the tree
            else if(precievedValue>(Double)node.maxEntry.key) {
                QueryResult res = new QueryResult(null, Parameters.higherBoundIndex);
                res.setHigherBackTracking(true);
                return res;
            }
            //no equality with all other keys check the best brach to go to
            //select the child where the perceived value of the query key fits in
            for (int j = 0; j < node.m; j++) {
                if (j + 1 == node.m || (precievedValue<(Double)node.children[j + 1].key)){
                    return new QueryResult(node.children[j].next, j);
                }
            }

        }
        return null;
    }
    public ArrayList<QueryResult> getNodeResultContent(Node node, boolean equalityShortCut,
            int level,boolean backtracking) {
        ArrayList<QueryResult> queryResults = new ArrayList<QueryResult>();

        if (level == 0) {
            for (int i = 0; i < node.m; i++) {
                queryResults.add(new QueryResult((Double) node.children[i].key,
                        (Double) node.children[i].key, i));
            }
        } else {
            if(backtracking){
                QueryResult q = new QueryResult(null,Parameters.smallerBackTracking);
                q.setSmallerBackTracking(true);
                queryResults.add(q);
            }
            if (equalityShortCut) {
                if(backtracking){
                    queryResults.add(new QueryResult((Double) node.minEntry.key,
                            (Double) node.minEntry.key, Parameters.smallerBoundIndex));
                }
                int indexWithinNode = 0; // this variable indicates which node
                // alternative to be chooses either
                // equality or less or greater than,
                // used to accumulate worker's answers
                queryResults.add(new QueryResult(node.children[0].next, indexWithinNode++));
                for (int i = 1; i < node.m; i++) {
                    // queryResults.add(new
                    // QueryResult((Double)node.children[i].key,(Double)node.children[i].value,indexWithinNode++));
                    queryResults.add(new QueryResult((Double) node.children[i].key,
                            (Double) node.children[i].key, indexWithinNode++));
                    queryResults.add(new QueryResult(node.children[i].next, indexWithinNode++));
                }
                if(backtracking){
                    queryResults.add(new QueryResult((Double) node.maxEntry.key,
                            (Double) node.maxEntry.key, Parameters.higherBoundIndex));
                }

            } else {

                for (int i = 0; i < node.m; i++) {
                    queryResults.add(new QueryResult(node.children[i].next, i));
                }

            }
            if(backtracking){
                QueryResult q = new QueryResult(null,Parameters.higherBackTracking);
                q.setHigherBackTracking(true);
                queryResults.add(q);
            }

        }

        return queryResults;
    }
    /**
     * this is an initial implementation for calculating probabilities of each alternative we MUST revisit this once again
     * @param node
     * @param query
     * @param probSeed
     * @return
     */
    private ArrayList<Double> calculateProbablities (Node root,Node node,Query query,double probSeed,int level,boolean equalitity ,boolean backtracking){
        Double mean = query.getQueryKey();

        Double stdDev = probSeed/(Math.pow(2, 4-level));//Math.sqrt(probSeed);
        //Double stdDev =probSeed ;
        double minVal , maxVal;
        double distDivFactor = Parameters.equalityDivisionFactor;
        ArrayList<Double> probablities = new ArrayList<Double>();


        double k1,k2,dist;
        k1 = (Double)node.minEntry.key;
        k2 = (Double)node.children[1].key;
        dist = (k2-k1)/distDivFactor;
        if(equalitity){

            if(backtracking){
                minVal = (Double)node.minEntry.key-dist;
                //  probablities.add(fRandom.getProbLessThan(mean, stdDev,minVal));
                if(query.getQueryKey()<(Double)node.minEntry.key){
                    probablities.add(fRandom.getProbLessThan(mean, stdDev, minVal));
                }else{
                    probablities.add(fRandom.getProbInBetween(mean, stdDev,minVal-dist*distDivFactor,minVal));
                }

                maxVal =  (Double)node.minEntry.key+dist;
                probablities.add(fRandom.getProbInBetween(mean, stdDev,minVal,maxVal));
                minVal = maxVal ;
                maxVal  = (Double)node.children[1].key-dist;
                probablities.add(fRandom.getProbInBetween(mean, stdDev,minVal,maxVal));
                minVal =  maxVal ;
            }
            else{
                minVal = (Double)node.children[1].key-dist;

                if(query.getQueryKey()<(Double)node.children[1].key){
                    probablities.add(fRandom.getProbLessThan(mean, stdDev, minVal));
                }else{
                    probablities.add(fRandom.getProbInBetween(mean, stdDev,minVal-dist*distDivFactor,minVal));
                }
            }
            for(int i =2;i<node.m;i++){
                k1=k2;
                k2=(Double)node.children[i].key;
                dist = (k2-k1)/distDivFactor;
                maxVal =  (Double)node.children[i-1].key+dist;
                probablities.add(fRandom.getProbInBetween(mean, stdDev, minVal, maxVal));
                minVal = maxVal;
                maxVal = (Double)node.children[i].key-dist;
                probablities.add(fRandom.getProbInBetween(mean, stdDev, minVal, maxVal));
                minVal = maxVal;
            }
            k2 = (Double)node.maxEntry.key;
            k1= (Double)node.children[node.m-1].key;
            dist = (k2-k1)/distDivFactor;
            maxVal = (Double)node.children[node.m-1].key+dist;
            probablities.add(fRandom.getProbInBetween(mean, stdDev, minVal, maxVal));
            minVal = maxVal;
            if(backtracking){

                maxVal =  (Double)node.maxEntry.key-dist;
                probablities.add(fRandom.getProbInBetween(mean, stdDev, minVal, maxVal));
                minVal = maxVal;
                maxVal = (Double)node.maxEntry.key+dist;
                probablities.add(fRandom.getProbInBetween(mean, stdDev, minVal, maxVal));
                minVal = maxVal;

                if(query.getQueryKey()>(Double)node.maxEntry.key){
                    probablities.add(fRandom.getProbGreaterThan(mean, stdDev, minVal));
                }else{
                    probablities.add(fRandom.getProbInBetween(mean, stdDev,minVal,minVal+dist*distDivFactor));
                }
            }
            else{


                minVal =maxVal;

                if(query.getQueryKey()>(Double)node.children[node.m-1].key){
                    probablities.add(fRandom.getProbGreaterThan(mean, stdDev, minVal));
                }else{
                    probablities.add(fRandom.getProbInBetween(mean, stdDev,minVal,minVal+dist*distDivFactor));
                }
            }
        }
        else{

            if(backtracking){
                dist = ((Double)node.children[1].key-(Double)node.minEntry.key);
                double prevValue = (Double)node.minEntry.key;
                if(query.getQueryKey()<(Double)node.minEntry.key){
                    probablities.add(fRandom.getProbLessThan(mean, stdDev, (Double)node.minEntry.key));
                }
                else{

                    probablities.add(fRandom.getProbInBetween(mean, stdDev, (Double)node.minEntry.key-dist, (Double)node.minEntry.key));
                }
                for(int i =1;i<node.m;i++){
                    probablities.add(fRandom.getProbInBetween(mean, stdDev, prevValue,(Double)node.children[i].key));
                    prevValue = (Double)node.children[i].key;
                }
                probablities.add(fRandom.getProbInBetween(mean, stdDev, prevValue,(Double)node.maxEntry.key));

                dist = ((Double)node.maxEntry.key-(Double)node.children[node.m-1].key);
                if(query.getQueryKey()>(Double)node.maxEntry.key){
                    probablities.add(fRandom.getProbGreaterThan(mean, stdDev, (Double)node.maxEntry.key));
                }else{
                    probablities.add(fRandom.getProbInBetween(mean, stdDev, (Double)node.maxEntry.key,  (Double)node.maxEntry.key+dist));
                }
            }
            else{
                dist = ((Double)node.children[1].key-(Double)node.minEntry.key);
                double prevValue;
                if(query.getQueryKey()<(Double)node.children[1].key){
                    prevValue = -10000000000000.0;
                }
                else{
                    prevValue = (Double)node.children[1].key-dist;
                }
                for(int i =1;i<node.m;i++){
                    probablities.add(fRandom.getProbInBetween(mean, stdDev, prevValue,(Double)node.children[i].key));
                    prevValue = (Double)node.children[i].key;
                }
                //
                dist = ((Double)node.maxEntry.key-(Double)node.children[node.m-1].key);
                if(query.getQueryKey()>(Double)node.children[node.m-1].key){
                    probablities.add(fRandom.getProbGreaterThan(mean, stdDev, (Double)node.children[node.m-1].key));
                }
                else{
                    probablities.add(fRandom.getProbInBetween(mean, stdDev, (Double)node.children[node.m-1].key, (Double)node.children[node.m-1].key+dist));
                }
            }
        }

        return probablities;

    }






}
