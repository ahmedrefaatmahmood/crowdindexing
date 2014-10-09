package edu.purdue.crowdindex.shared.control;

public   class Constants {
    //Task constants
    public static String taskStatusNew = "New";
    public static String taskStatusOpen = "Open";
    public static String taskStatusSolved = "Solve";
    public static String taskStatusCanceled = "Cancel";
    public static String levelBased = "levelbased" ;
    public static String pathBased = "pathbased" ;


    //Task Type constanst
    public static String astar = "astar";
    public static String breadth = "breadth";
    public static String depth = "depth";
    public static String informed = "informed";

    //A* Backtracking constants
    public static String GREATER_THAN_SUBTREE = "100000" ;
    public static String LESS_THAN_SUBTREE = "-2" ;
    public static String MAX_SUBTREE_KEY = "10000" ;
    public static String MIN_SUBTREE_KEY = "-1" ;
    public static String SQUARES_DATA_SET_ID = "1" ;
    public static double UPDATE_FACTOR = 2 ;



    public static String Unselected_item = "unseleceted" ;
    public  static  int MAX_NODE_RANGE= 20000;

    public  static  int M = 4;    // max children per B-tree node = M-1
    public  static  int imageRectangleSize = 200;    // max children per B-tree node = M-1
    public  static  int replicationNum = 1;    // number of replication per query item
    //   public static String path = "D://downloads//Animals_with_Attributes//";
    //   public static String path= "E://work//purdue//database//research//Implementation (1)//crowd indexing//squareimages//";
    public static String path= "squareimages";
    //   public static String queryModel = pathBased;  //node based or level based
    public static String queryModel = levelBased;  //node based or level based


    public static final int MaxReplications = 3;

    public static int MaxUserTasks=50;



}
