package ch.ethz.systems.netbench.xpt.voijslav.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;

import ch.ethz.systems.netbench.core.Simulator;

import java.util.Random;
import java.util.TreeMap;

public class FctDistributions {
	
	public static Random rnd;
	static{
		rnd = new Random(12345);
	}
	
	//pfabric UpperDistribution
//	public static int flowSizeDistribution(double outcome){
//		if (outcome >= 0.0 && outcome <= 0.1) {
//            return 180;
//        } else if (outcome >= 0.1 && outcome <= 0.2) {
//            return 216;
//        } else if (outcome >= 0.2 && outcome <= 0.3) {
//            return 560;
//        } else if (outcome >= 0.3 && outcome <= 0.4) {
//            return 900;
//        } else if (outcome >= 0.4 && outcome <= 0.5) {
//            return 1100;
//        } else if (outcome >= 0.5 && outcome <= 0.6) {
//            return 1870;
//        } else if (outcome >= 0.6 && outcome <= 0.7) {
//            return 3160;
//        } else if (outcome >= 0.7 && outcome <= 0.8) {
//            return 10000;
//        } else if (outcome >= 0.8 && outcome <= 0.9) {
//            return 400000;
//        } else if (outcome >= 0.9 && outcome <= 0.95) {
//            return 3160000;
//        } else if (outcome >= 0.95 && outcome <= 0.98) {
//            return 100000000;
//        } else { // outcome >= 0.98 && outcome <= 1.0
//            return 1000000000;
//        }
//	}
//	
//	//pfabric UpperMean
//	public static int mean(){
//		return 23199798;
//	}
	
	//pfabric Lower Data Mining
	public static int flowSizeDistribution(double outcome){
		if (outcome >= 0.0 && outcome <= 0.15) {
            return 1;
        } else if (outcome >= 0.15 && outcome <= 0.2) {
            return 10000;
        } else if (outcome >= 0.2 && outcome <= 0.3) {
            return 20000;
        } else if (outcome >= 0.3 && outcome <= 0.4) {
            return 30000;
        } else if (outcome >= 0.4 && outcome <= 0.53) {
            return 50000;
        } else if (outcome >= 0.53 && outcome <= 0.6) {
            return 80000;
        } else if (outcome >= 0.6 && outcome <= 0.7) {
            return 200000;
        } else if (outcome >= 0.7 && outcome <= 0.8) {
            return 1000000;
        } else if (outcome >= 0.8 && outcome <= 0.9) {
            return 2000000;
        } else if (outcome >= 0.9 && outcome <= 0.97) {
            return 5000000;
        } else { // outcome >= 0.97 && outcome <= 1.0
            return 10000000;
        }
	}
	
	//pfabric LowerMean
	public static int mean(){
		return 987600;
	}
	
	private static TreeMap<Double, Double> priorities = null;
	
	protected static void init(){
		priorities = new TreeMap<Double, Double>();
		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(
				Simulator.getConfiguration().getPropertyOrFail("spark_error_distribution")
			));
			
			String line = reader.readLine();
		    while (line != null) {
		        String[] parsed = line.split(" ");
		        Double key = Double.parseDouble(parsed[1].trim());
		        Double value = Double.parseDouble(parsed[0].trim());
		        priorities.put(key, value);
		        line = reader.readLine();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double sparkDistribution(double random){
		if (priorities == null){
			init();
		}
		Entry<Double, Double> ent;
		ent = priorities.ceilingEntry(random);
		if(ent==null){
			ent = priorities.floorEntry(random);
		}
		if(ent==null){
			ent = priorities.firstEntry();
		}
		return ent.getValue();
	}
}
