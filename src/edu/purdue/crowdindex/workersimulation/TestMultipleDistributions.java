package edu.purdue.crowdindex.workersimulation;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class TestMultipleDistributions {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        RandomGenerator g1 =new RandomGenerator(1);
        RandomGenerator g2 =new RandomGenerator(2);
        RandomGenerator g3 =new RandomGenerator(3);
        RandomGenerator g4 =new RandomGenerator(4);
        RandomGenerator g5 =new RandomGenerator(5);
        try {
            // Create file
            FileWriter fstream1 = new FileWriter("randomOutfile1.txt");
            BufferedWriter out1 = new BufferedWriter(fstream1);
            FileWriter fstream2 = new FileWriter("randomOutfile2.txt");
            BufferedWriter out2 = new BufferedWriter(fstream2);
            double var1=3,var2=15,var3=27,var4=30;
            double varCombined = 13.37;
            for(int i=0;i<100000;i++){
                int rand = g5.getRandomInt(1, 5);
                if(rand==1)
                    out1.write("" +g1.getGaussian(0, var1) + "\n");
                if(rand==2)
                    out1.write("" +g2.getGaussian(0, var2) + "\n");
                if(rand==3)
                    out1.write("" +g3.getGaussian(0, var3) + "\n");
                if(rand==4)
                    out1.write("" +g3.getGaussian(0, var4) + "\n");

                out2.write("" +g4.getGaussian(0, varCombined) + "\n");
                //                out2.write("" +g4.getGaussian(0, varCombined) + "\n");
                //                out2.write("" +g4.getGaussian(0, varCombined) + "\n");

            }
            System.out.println("test done");




            out1.close();
            out2.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }


    }

}
