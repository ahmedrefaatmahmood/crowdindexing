package edu.purdue.crowdindex.workersimulation;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;


public class RandomGenerator {
    Random fRandom;
    private static RandomGenerator instance = null;
    public RandomGenerator() {
        fRandom =new Random(Parameters.randomGeneratorSeed);

    }
    public RandomGenerator(int seed) {
        fRandom =new Random(seed);

    }
    public static RandomGenerator getInstance() {
        if(instance == null) {
            instance = new RandomGenerator();
        }
        return instance;
    }
    //this function is tested
    public double getGaussian(double aMean, double aVariance){
        return aMean + fRandom.nextGaussian() * aVariance;
    }
    public  int getRandomInt(int min, int max){
        return min+ fRandom.nextInt(max-min);
    }
    public double getNextDouble(){
        return fRandom.nextDouble();
    }

    public double getAnAnswerFromMultipleDsitributions(double mean, ArrayList<Double > variances){
        int workerIndex = getRandomInt(0, variances.size());
        return getGaussian(mean, variances.get(workerIndex));
    }
    public Double getProbLessThan(Double mean, Double stdDev,Double val){

        NormalDistributionImpl d = new NormalDistributionImpl(mean, stdDev);
        Double probValue = null;
        try {
            probValue = new Double(d.cumulativeProbability(val));
            //   System.out.println("less than = "+val+ " prob value ="+probValue);
        } catch (MathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return probValue;
    }
    public Double getProbInBetween(Double mean,Double stdDev,Double min,Double max){

        Double probValue1 = null;
        Double probValue = null;
        Double probValue2 = null;
        NormalDistributionImpl d = new NormalDistributionImpl(mean, stdDev);
        try {
            probValue1 = new Double(d.cumulativeProbability(min));
            probValue2 = new Double(d.cumulativeProbability(max));
            probValue = probValue2- probValue1;
            //    System.out.println("Min= "+min+", max = "+max+ " prob value ="+probValue);
        } catch (MathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return probValue;
    }
    public Double getProbGreaterThan(Double mean, Double stdDev, Double val){

        NormalDistributionImpl d = new NormalDistributionImpl(mean, stdDev);
        Double probValue = null;
        try {
            probValue = 1.0-new Double(d.cumulativeProbability(val));
            //     System.out.println("greater than = "+val+ " prob value ="+probValue);
        } catch (MathException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return probValue;
    }

}
