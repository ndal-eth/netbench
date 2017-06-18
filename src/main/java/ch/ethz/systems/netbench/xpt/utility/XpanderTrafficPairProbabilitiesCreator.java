package ch.ethz.systems.netbench.xpt.utility;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.HashSet;

public class XpanderTrafficPairProbabilitiesCreator extends TrafficPairProbabilitiesCreator {

	
	
    public static void main(String args[]) throws IOException {

        HashSet<Pair<Integer,Integer>> values = new HashSet<Pair<Integer,Integer>>();
        
        // add FatTree sizes
        for(int k=8; k<49; k+=2){
        		values.add(new ImmutablePair<Integer,Integer>(k*k*5/4, k*k/2)); // FatTree nodes
        		values.add(new ImmutablePair<Integer,Integer>(k+k/2, k/2)); // 2 layered folded clos network
        }
        
        // add #switches from Xpander paper (vs. 2FCN)
        values.add(new ImmutablePair<Integer,Integer>(42,42)); // d=32
        values.add(new ImmutablePair<Integer,Integer>(66,66)); // d=48
        
        // add #switches from Xpander paper (vs. FatTree)
        values.add(new ImmutablePair<Integer,Integer>(64,64)); // d=8
        values.add(new ImmutablePair<Integer,Integer>(125,125)); // d=10
        values.add(new ImmutablePair<Integer,Integer>(144,144)); // d=12
        values.add(new ImmutablePair<Integer,Integer>(237,237)); // d=14
        values.add(new ImmutablePair<Integer,Integer>(256,256)); // d=16
        values.add(new ImmutablePair<Integer,Integer>(365,365)); // d=18
        values.add(new ImmutablePair<Integer,Integer>(400,400)); // d=20
        values.add(new ImmutablePair<Integer,Integer>(544,544)); // d=22
        values.add(new ImmutablePair<Integer,Integer>(576,576)); // d=24
        values.add(new ImmutablePair<Integer,Integer>(1152,1152)); // d=32
        
        
        generate_from_values(values);
    }
    
}
