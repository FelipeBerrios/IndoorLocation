package com.memoria.felipe.indoorlocation.Utils;

import android.util.Log;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by felip on 25-11-2017.
 */

public class KNN {

    private int k= 3;
    private RealMatrix trainingMatrix;
    private RealVector output;

    public KNN(int k, RealMatrix trainingMatrix, RealVector output) {
        this.k = k;
        this.trainingMatrix = trainingMatrix;
        this.output = output;
    }

    public void predict(RealVector predict){

        List<Double> answers = euclideanDistance(trainingMatrix, predict, output);

        Double prediction = getMostOccurringElement(answers, this.k);
        Log.e("Prediccion", prediction.toString());
    }


    //Slice the list from 0 to k and return the character that occurs the most
    public static Double getMostOccurringElement(List answers, int k){
        int max = 0;
        Double mostFrecuent = 0d;
        List sublist = answers.subList(0,k);

        for (int i=0; i<sublist.size(); i++){
            int occurrences = Collections.frequency(answers.subList(0,k), sublist.get(i));

            //If counted occurrences are greater than what I have update to current values
            if(occurrences > max) {
                mostFrecuent = (Double) sublist.get(i);
                max = occurrences;
            }
        }

        Log.e("Sorted by distance : ",   Arrays.toString( sublist.toArray()));
        Log.e("Predict", mostFrecuent.toString() + " occurs most frequently with value " + max +".");

        return mostFrecuent;
    }

    //Perform Euclidean distance formula to find out the distance
    //between our prediction value and each row in the matrix
    public static List euclideanDistance(RealMatrix m, RealVector y,
                                         RealVector output ){

        Map map = new HashMap();
        //Lets turn out 'y' value or label into vector for easier math operations

        for (int i=0; i<m.getRowDimension(); i++){
            RealVector vec = m.getRowVector(i);
            RealVector sub = vec.subtract( y );

            //Take square root of sum of square values that were subtracted a line above
            double distance = Math.sqrt(StatUtils.sumSq(sub.toArray()));
            //Use the distance to each data point(or row) as key with the 'default' option as value
            map.put( distance  , output.getEntry(i) );
        }

        //Now lets sort the map's keys into a set
        SortedSet<Double> keys = new TreeSet<Double>(map.keySet());
        List<Double> neighbors = new ArrayList<Double>();

        //For each key add the values in that order into the list
        for (Double key : keys) {
            neighbors.add((Double) map.get(key));
        }

        return neighbors;
    }

}
