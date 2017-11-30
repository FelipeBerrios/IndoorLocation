package com.memoria.felipe.indoorlocation.Utils.SVM;

import java8.util.stream.IntStreams;

/**
 * Created by felip on 29-11-2017.
 */

public class SVMOriginalX {

    static int[] n_svs = {208, 212, 293, 279, 449, 336, 298, 359, 366, 297, 398};
    static int  N_svs = IntStreams.of(n_svs).sum();

    public static int getN_svs() {
        return N_svs;
    }

    public static void setN_svs(int n_svs) {
        N_svs = n_svs;
    }

    public static double predict(double[] atts, double[][] svs,double[][] coeffs) {

        double[] inters = {0.062074107355811842, -0.16474109473753726, -0.10875418198191726, -0.45772352135004402, -0.21364858378028226, -0.18692890738507936, -0.31532151166784594, -0.46229595914678973, -0.29459157957242921, -0.54857127248439608, -0.21456174568077629, -0.17393117972433969, -0.52547562302892381, -0.26242820186218341, -0.23570787416085198, -0.35972160877974507, -0.5010572401181862, -0.33851222573547762, -0.58308557804714578, 0.046398839176254952, -0.34558465837632385, -0.059431441947649342, -0.031381207688412557, -0.16855256410127456, -0.32912157302303441, -0.14561800349032294, -0.42809870565460112, -0.40362319160922855, -0.11700964048186614, -0.080795220782798632, -0.21807912454062409, -0.37170007279588868, -0.19220543146382463, -0.46645263182702873, 0.29983316085334738, 0.35537004345760798, 0.17718634961368471, -0.011711381842535024, 0.18381015856505598, -0.12955299683565408, 0.042239684569318287, -0.12304001356181822, -0.28412124480505802, -0.089655434266199388, -0.38030360845886402, -0.15157857433631791, -0.31249078949488324, -0.11620551617955274, -0.41013900074964538, -0.18303320287466823, 0.024395510162621439, -0.28099801282012904, 0.19578904893496521, -0.11529483459670267, -0.30304199071354815};
        double[] classes = {2.0, 6.0, 10.0, 14.0, 18.0, 22.0, 26.0, 30.0, 34.0, 38.0, 42.0};

        // exp(-y|x-x'|^2)
        double[] kernels = new double[3495];
        double kernel;
        for (int i = 0; i < 3495; i++) {
            kernel = 0.;
            for (int j = 0; j < 8; j++) {
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
