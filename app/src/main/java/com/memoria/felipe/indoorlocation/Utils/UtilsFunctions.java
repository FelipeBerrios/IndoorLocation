package com.memoria.felipe.indoorlocation.Utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.apache.commons.math3.linear.RealVector;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Created by felip on 24-11-2017.
 */

public class UtilsFunctions {

    public static double [][] readFromFileMatrix(int filas, int columnas, String archivo, Context ctx) throws IOException {
        double [][] matrix = new double[filas][columnas];


        InputStream inputstream = ctx.getAssets().open(archivo);
        String line = "";
        BufferedReader bf = new BufferedReader(new InputStreamReader(inputstream));

        int lineCount = 0;
        while ((line = bf.readLine()) != null)
        {
            String[] numbers = line.split(" ");
            for ( int i = 0 ; i < columnas ; i++)
                matrix[lineCount][i] = Double.parseDouble(numbers[i]);

            lineCount++;
        }
        bf.close();
        return matrix;
    }


    public static double [] readFromFileVector(int columnas, String archivo, Context ctx) throws IOException {
        double [] vector = new double[columnas];


        InputStream inputstream = ctx.getAssets().open(archivo);
        String line = "";
        BufferedReader bf = new BufferedReader(new InputStreamReader(inputstream));

        int lineCount = 0;
        if((line = bf.readLine()) != null) {
            String[] numbers = line.split(" ");
            for (int i = 0; i < columnas; i++)
                vector[i] = Double.parseDouble(numbers[i]);

            lineCount++;

            bf.close();
        }
        return vector;
    }

    public static RealVector scaleData(RealVector mean, RealVector scale, RealVector newData){

        RealVector x = newData.subtract(mean);
        x = x.ebeDivide(scale);
        return x;
    }

}
