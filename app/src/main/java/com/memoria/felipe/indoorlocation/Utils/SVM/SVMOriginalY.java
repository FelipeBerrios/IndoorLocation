package com.memoria.felipe.indoorlocation.Utils.SVM;

import java8.util.stream.IntStreams;

/**
 * Created by felip on 29-11-2017.
 */

public class SVMOriginalY {

    static int[] n_svs = {848, 790, 813, 747};
    static int  N_svs = IntStreams.of(n_svs).sum();

    public static int getN_svs() {
        return N_svs;
    }

    public static void setN_svs(int n_svs) {
        N_svs = n_svs;
    }

    public static double predict(double[] atts,double[][] svs,double[][] coeffs) {


        double[] inters = {0.11567295551071466, 0.18478963790113828, 0.17238138108077952, 0.073046594623601815, 0.071947706603388506, -0.0099720706870654648};
        double[] classes = {8.0, 12.0, 16.0, 20.0};

        // exp(-y|x-x'|^2)
        double[] kernels = new double[3198];
        double kernel;
        for (int i = 0; i < 3198; i++) {
            kernel = 0.;
            for (int j = 0; j < 8; j++) {
                kernel += Math.pow(svs[i][j] - atts[j], 2);
            }
            kernels[i] = Math.exp(-4 * kernel);
        }

        int[] starts = new int[4];
        for (int i = 0; i < 4; i++) {
            if (i != 0) {
                int start = 0;
                for (int j = 0; j < i; j++) {
                    start += n_svs[j];
                }
                starts[i] = start;
            } else {
                starts[0] = 0;
            }
        }

        int[] ends = new int[4];
        for (int i = 0; i < 4; i++) {
            ends[i] = n_svs[i] + starts[i];
        }

        double[] decisions = new double[6];
        for (int i = 0, d = 0, l = 4; i < l; i++) {
            for (int j = i + 1; j < l; j++) {
                double tmp = 0.;
                for (int k = starts[j]; k < ends[j]; k++) {
                    tmp += coeffs[i][k] * kernels[k];
                }
                for (int k = starts[i]; k < ends[i]; k++) {
                    tmp += coeffs[j - 1][k] * kernels[k];
                }
                decisions[d] = tmp + inters[d];
                d++;
            }
        }

        int[] votes = new int[6];
        for (int i = 0, d = 0, l = 4; i < l; i++) {
            for (int j = i + 1; j < l; j++) {
                votes[d] = decisions[d] > 0 ? i : j;
                d++;
            }
        }

        int[] amounts = new int[4];
        for (int i = 0, l = 6; i < l; i++) {
            amounts[votes[i]] += 1;
        }

        int class_val = -1,
                class_idx = -1;
        for (int i = 0, l = 4; i < l; i++) {
            if (amounts[i] > class_val) {
                class_val = amounts[i];
                class_idx = i;
            }
        }
        return classes[class_idx];
    }

}
