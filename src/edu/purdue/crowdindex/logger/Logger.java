package edu.purdue.crowdindex.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    //this is an initial logging class
    //TODO we should use a proper logging function
    public void log(String s){
        FileWriter fstream;
        try {
            fstream = new FileWriter("AR_Logging.txt");

            BufferedWriter out2 = new BufferedWriter(fstream);
            // PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter(, true)));

            //out2.println(s);
            out2.write(s);

            out2.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log(e.getMessage());
            log(e.getStackTrace().toString());
        }

    }
}
