package edu.purdue.crowdindex.shared.control;

public   class Constants {
    //Task constants
    public static String taskStatusNew = "New";
    public static String taskStatusOpen = "Open";
    public static String taskStatusSolved = "Solve";
    public static String taskStatusCanceled = "Cancel";
    public static String levelBased = "levelbased" ;
    public static String pathBased = "pathbased" ;
    public static int defaultReplications =10;


    //Task Type constanst
    public static String astar = "astar";
    public static String breadth = "breadth";
    public static String depth = "depth";
    public static String informed = "informed";
    public static String insert = "insert";
    public static String test = "test";
    public static int equality_true =1;  //this means that the task viewed by the user is allowed to have the equality shortcut
    public static int equality_false =0;  //this means that the task viewed by the user is NOT allowed to have the equality shortcut

    //A* Backtracking constants
    public static String GREATER_THAN_SUBTREE = "1000000" ;
    public static String LESS_THAN_SUBTREE = "-2" ;
    public static String MAX_SUBTREE_KEY = "100000" ;
    public static String MIN_SUBTREE_KEY = "-1" ;
    public static String SQUARES_DATA_SET_ID = "1" ;
    public static int UPDATE_FACTOR = 2 ;
    public static int DIRECTION_HIGHER = 2 ;
    public static int DIRECTION_LOWER = 1 ;

    //Tree insertion constants
    public static int MIN_KEY_VALUE = 1 ;
    public static int MAX_KEY_VALUE = 10000 ;
    public static int KEY_RELATION_LESS =-1;
    public static int KEY_RELATION_Between =0;
    public static int KEY_RELATION_Greater =1;

    public static String Unselected_item = "unseleceted" ;
    public  static  int MAX_NODE_RANGE= 20000;

    public  static  int M = 4;    // max children per B-tree node = M-1
    public  static  int imageRectangleSize = 200;    // max children per B-tree node = M-1

    //   public static String path = "D://downloads//Animals_with_Attributes//";
    //   public static String path= "E://work//purdue//database//research//Implementation (1)//crowd indexing//squareimages//";
    public static String path= "squareimages";
    //   public static String queryModel = pathBased;  //node based or level based
    public static String queryModel = levelBased;  //node based or level based


    public static final int MaxReplications = 3;

    public static int MaxUserTasks=50;



}
