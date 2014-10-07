package edu.purdue.crowdindex.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.purdue.crowdindex.logger.Logger;

public class DbConnection {
    Logger logger = new Logger();
    public Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        return DriverManager
                .getConnection(
                        "jdbc:mysql://localhost:3306/crowdindex?allowMultiQueries=true",
                        "root", "1234");
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
                logger.log(logOrIgnore.getMessage());
                logger.log(logOrIgnore.getStackTrace().toString());
            }
        if (stmt != null)
            try {
                stmt.close();
            } catch (SQLException logOrIgnore) {
                logger.log(logOrIgnore.getMessage());
                logger.log(logOrIgnore.getStackTrace().toString());
            }
        if (con != null)
            try {
                con.close();
            } catch (SQLException logOrIgnore) {
                logger.log(logOrIgnore.getMessage());
                logger.log(logOrIgnore.getStackTrace().toString());
            }

    }
}
