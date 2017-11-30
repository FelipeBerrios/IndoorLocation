package com.memoria.felipe.indoorlocation.Utils.SVM;

import java8.util.stream.IntStreams;

/**
 * Created by felip on 29-11-2017.
 */

public class SVMPCAX {

    static int[] n_svs = {198, 298, 358, 313, 442, 349, 362, 379, 288, 230, 243};
    static int  N_svs = IntStreams.of(n_svs).sum();

    public static int getN_svs() {
        return N_svs;
    }

    public static void setN_svs(int n_svs) {
        N_svs = n_svs;
    }

    public static double predict(double[] atts, double[][] svs,double[][] coeffs) {

        int[] n_svs = {198, 298, 358, 313, 442, 349, 362, 379, 288, 230, 243};
        double[] inters = {0.29598152408326944, -0.1940288199150258, -0.13854616021925156, -0.36509836841065629, -0.18857197024898517, -0.17410580224923833, -0.28008155641471832, -0.47818528856542492, -0.3201453332974793, -0.59977337814005793, -0.24602878498374553, -0.24481324914073255, -0.50092930254949597, -0.27899678550858642, -0.26842544830669185, -0.3569058301119456, -0.53656423308136603, -0.38851384084001406, -0.64792748866985039, -0.083780007681158758, -0.40053807545346104, -0.13241453547901841, -0.12618154311224405, -0.21704458483731989, -0.41121900147070239, -0.24559672614839975, -0.54641474505888998, -0.29519917834484943, -0.076513453074223595, -0.055773895453525929, -0.13908050776082842, -0.36959753510638244, -0.20395448430610683, -0.50860530275771987, 0.23225108169521752, 0.31472620202134094, 0.11570378443485227, -0.18009321407926576, 0.011228335239393024, -0.33331158291126284, -0.032060787758192003, -0.1274332267732726, -0.34680099028972455, -0.13914872416236729, -0.47345480212634855, -0.20817452739044309, -0.37779945267650461, -0.17599390957501981, -0.48522906893887296, -0.24910208658622771, -0.044952864790934285, -0.39097736543569606, 0.2153102523868696, -0.17013570211348128, -0.3591006386067484};
        double[] classes = {2.0, 6.0, 10.0, 14.0, 18.0, 22.0, 26.0, 30.0, 34.0, 38.0, 42.0};

        // exp(-y|x-x'|^2)
        double[] kernels = new double[3460];
        double kernel;
        for (int i = 0; i < 3460; i++) {
            kernel = 0.;
            for (int j = 0; j < 4; j++) {
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
}