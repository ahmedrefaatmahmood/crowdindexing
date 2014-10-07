package edu.purdue.crowdindex.workersimulation;

public class Parameters {
    //********Worker parameters****************/
    public static int randomSpammerWorker=1  ;
    public static int intentionalSpammerWorker=2  ;
    public static int normalWorker=3  ;
    public static int proximityWorker=4  ;
    public static int normalWorker2=5  ;
    public static int workerType =  normalWorker ;
    public static double bufferZoneRatio = 1; //this is the distance around the answert to indicate an  falls within equality range
    public static int randomGeneratorSeed =  1 ; //this is the seed for generating a worker response based on a uniform distribution


    public static int equalityDivisionFactor=50; //this is to identify the range for equality for a key


    public static int defaultVar = 4000; //(100 uniform)  //(1000 random)
    public static int minVar = 1; //Highly accurate
    public static int maxVar =10000;//(1000 unifom);//Low accuracy  // (50000 random)
    public static int varStep =500;// (50 uniform);//Low accuracy //(2000 random)

    //********Queries parameters****************/
    public static int numberOfQueries = 1000;//2000;
    //********tree order parameters****************/
    public static int minTreeOrder  =1; //this means the order of the tree is 1, number of keys between 1, 2 , max number of children = 3
    public static int maxTreeOrder  =10; //this means the order of the tree is 5, number of keys between 5, 10, max number of children = 11
    public static int defaultOrder = 4;
    //********Cost parameters****************/
    public static int minTaskCost = 10;
    public static int maxTaskCost = 500;
    public static int defaultCost = 50; // (50 uniform)
    public static int costStep = 40;
    //********Aggregation parameters****************/
    public static int majorityAggreecation = 1; //this is a regular majority voting with random selection at equality
    public static int distributionAggreecation = 2; //this looks at the distribution of votes to select the on eat median of votes
    //********Dataset parameters****************/
    public static int consecutiveNumbersDataset =1;
    public static int randomNumberDataset =2;
    public static int skewdDataSet=3;
    static int maxUniformDataSet = 10000;
    public static int datasetSize = 10000;
    public static int datasetSizeRange = 100000;
    public static int skewdDataSetMean=datasetSizeRange*3/4;
    public static int skewdDaraSetVar = 100000;

    //********Traversal strategy parameters *********/
    public static int equalityTrue =1;
    public static int equalityFalse =2;
    public static int leafLevel =1;
    public static int levelByLevel =2;
    public static int Astar=3;
    //****************************Backtracking parameters **********************/
    public static int smallerBoundIndex =-1;
    public static int higherBoundIndex = -2;
    public static int smallerBackTracking = -3;
    public static int higherBackTracking=-4;
    public static double scoreUpdateFactor =2;





}
