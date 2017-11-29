package com.memoria.felipe.indoorlocation.Utils;

import com.memoria.felipe.indoorlocation.Utils.UtilsFunctions;
import java8.util.stream.IntStreams;

public class SVMPorter {

    static int[] n_svs = {420, 504, 545, 406, 565, 504, 538, 522, 519, 392, 316};
    static int  N_svs = IntStreams.of(n_svs).sum();

    public static int getN_svs() {
        return N_svs;
    }

    public static void setN_svs(int n_svs) {
        N_svs = n_svs;
    }

    public static double predict(double[] atts, double[][] svs, double [][] coeffs) {


        double[] inters = {0.33920265144211631, 0.10333102784368538, -0.11139317839608177, -0.1894588121005889, -0.084656584773724411, -0.084818933128557344, -0.2158453421878288, -0.48329516845837101, -0.33503226228591082, -0.5284598313177753, -0.3835275869576652, -0.31071804904262396, -0.46390597078050266, -0.16914788759210095, -0.22386077496807799, -0.33869510323776342, -0.54225446948888756, -0.41130294488343228, -0.60049866572897614, -0.18649721308537404, -0.25601292379926399, -0.16383444947181486, -0.1136514768287309, -0.25031304346118349, -0.49647374396032995, -0.36048969779128193, -0.56531903431106467, -0.5522551309373358, -0.21041860360270445, -0.4044868721251389, -0.28799721117245236, -0.49632836080294229, -0.3420346684685005, -0.54452131963938155, 0.17435973448044645, 0.35097710657810716, 0.026810019529783076, -0.37889802581280863, -0.1425606172115097, -0.43209853172348384, -0.24484236045279539, -0.0027142388116163547, -0.47065684563926119, -0.22243776542123628, -0.51136916696756507, -0.19468237384511439, -0.4705371155531749, -0.28475656932905263, -0.50904345929922934, -0.35812161261178826, -0.10567776016110729, -0.36378995475601955, 0.21908268601301042, -0.10526372777185977, -0.41176541617162421};
        double[] classes = {2.0, 6.0, 10.0, 14.0, 18.0, 22.0, 26.0, 30.0, 34.0, 38.0, 42.0};


        // exp(-y|x-x'|^2)
        double[] kernels = new double[5231];
        double kernel;
        for (int i = 0; i < 5231; i++) {
            kernel = 0.;
            for (int j = 0; j < 2; j++) {
                kernel += Math.pow(svs[i][j] - atts[j], 2);
            }
            kernels[i] = Math.exp(-4 * kernel);
        }

        int[] starts = new int[11];
        for (int i = 0; i < 11; i++) {
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

        int[] ends = new int[11];
        for (int i = 0; i < 11; i++) {
            ends[i] = n_svs[i] + starts[i];
        }

        double[] decisions = new double[55];
        for (int i = 0, d = 0, l = 11; i < l; i++) {
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

        int[] votes = new int[55];
        for (int i = 0, d = 0, l = 11; i < l; i++) {
            for (int j = i + 1; j < l; j++) {
                votes[d] = decisions[d] > 0 ? i : j;
                d++;
            }
        }

        int[] amounts = new int[11];
        for (int i = 0, l = 55; i < l; i++) {
            amounts[votes[i]] += 1;
        }

        int class_val = -1,
                class_idx = -1;
        for (int i = 0, l = 11; i < l; i++) {
            if (amounts[i] > class_val) {
                class_val = amounts[i];
                class_idx = i;
            }
        }
        return classes[class_idx];
    }

    /*public static void main(String[] args) {
        if (args.length == 2) {
            float[] atts = new float[args.length];
            for (int i = 0, l = args.length; i < l; i++) {
                atts[i] = Float.parseFloat(args[i]);
            }
            System.out.println(SVMPorter.predict(atts));
        }
    }*/
}