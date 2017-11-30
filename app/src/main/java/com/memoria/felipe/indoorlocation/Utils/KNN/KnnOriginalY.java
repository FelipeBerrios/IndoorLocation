package com.memoria.felipe.indoorlocation.Utils.KNN;

/**
 * Created by felip on 29-11-2017.
 */

import java.util.*;

public class KnnOriginalY {

    private static class Neighbor {
        Integer clazz;
        Double dist;
        public Neighbor(int clazz, double dist) {
            this.clazz = clazz;
            this.dist = dist;
        }
    }

    public static double compDist(double[] temp, double[] cand, double q) {
        double dist = 0.;
        double diff;
        for (int i = 0, l = temp.length; i < l; i++) {
            diff = Math.abs(temp[i] - cand[i]);
            if (q==1) {
                dist += diff;
            } else if (q==2) {
                dist += diff*diff;
            } else if (q==Double.POSITIVE_INFINITY) {
                if (diff > dist) {
                    dist = diff;
                }
            } else {
                dist += Math.pow(diff, q);
            }
        }
        if (q==1 || q==Double.POSITIVE_INFINITY) {
            return dist;
        } else if (q==2) {
            return Math.sqrt(dist);
        } else {
            return Math.pow(dist, 1. / q);
        }
    }

    public static int predict(double[] atts, double[][] X, int[] y) {
        if (atts.length != 8) {
            return -1;
        }

        int classIdx = -1;
        int nNeighbors = 2;
        int nTemplates = 6600;
        int nClasses = 4;
        double power = 2;

        if (nNeighbors == 1) {
            double minDist = Double.POSITIVE_INFINITY;
            double curDist;
            for (int i = 0; i < nTemplates; i++) {
                curDist = KnnOriginalY.compDist(X[i], atts, power);
                if (curDist <= minDist) {
                    minDist = curDist;
                    classIdx = y[i];
                }
            }
        } else {
            int[] classes = new int[nClasses];
            ArrayList<Neighbor> dists = new ArrayList<Neighbor>();
            for (int i = 0; i < nTemplates; i++) {
                dists.add(new Neighbor(y[i], KnnOriginalY.compDist(X[i], atts, power)));
            }
            Collections.sort(dists, new Comparator<Neighbor>() {
                @Override
                public int compare(Neighbor n1, Neighbor n2) {
                    return n1.dist.compareTo(n2.dist);
                }
            });
            for (Neighbor neighbor : dists.subList(0, nNeighbors)) {
                classes[neighbor.clazz]++;
            }
            int classVal = -1;
            for (int i = 0; i < nClasses; i++) {
                if (classes[i] > classVal) {
                    classVal = classes[i];
                    classIdx = i;
                }
            }
        }
        return classIdx;
    }
}
