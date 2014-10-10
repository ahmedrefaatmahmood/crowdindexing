package edu.purdue.crowdindex.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.google.appengine.api.utils.SystemProperty;

public class DbConnection {
    Logger logger = java.util.logging.Logger.getLogger(this.getClass().getName());
    		
    public Connection getConnection() throws Exception {
    	
    	String url = null;
    	if (SystemProperty.environment.value() ==
    	SystemProperty.Environment.Value.Production) {
    	// Connecting from App Engine.
    	// Load the class that provides the "jdbc:google:mysql://"
    	// prefix.
    	Class.forName("com.mysql.jdbc.GoogleDriver");
    	url =
    	"jdbc:google:mysql://ece595-tm-starter:crowdindex?allowMultiQueries=true";
    	} else {
    	 // Connecting from an external network.
    	Class.forName("com.mysql.jdbc.Driver");
    	url = "jdbc:mysql://173.194.250.134:3306?allowMultiQueries=true";
    	}
    	
    	return DriverManager.getConnection(url,"root","1234");
//        Class.forName("com.mysql.jdbc.Driver");
//
//        return DriverManager
//                .getConnection(
//                        "jdbc:mysql://localhost:3306/crowdindex?allowMultiQueries=true",
//                        "root", "1234");
        //        return DriverManager
        //                .getConnection(
        //                        "jdbc:mysql://localhost:10159/crowdindex?allowMultiQueries=true",
        //                        "root", "1234");

    }
    public  void closeEveryThing(Connection con, Statement stmt, ResultSet res) {
        if (res != null)
            try {
                res.close();
            } catch (SQLException logOrIgnore) {
                logger.info(logOrIgnore.getMessage());
                logger.info(logOrIgnore.getStackTrace().toString());
            }
        if (stmt != null)
            try {
                stmt.close();
            } catch (SQLException logOrIgnore) {
                logger.info(logOrIgnore.getMessage());
                logger.info(logOrIgnore.getStackTrace().toString());
            }
        if (con != null)
            try {
                con.close();
            } catch (SQLException logOrIgnore) {
                logger.info(logOrIgnore.getMessage());
                logger.info(logOrIgnore.getStackTrace().toString());
            }

    }
}
