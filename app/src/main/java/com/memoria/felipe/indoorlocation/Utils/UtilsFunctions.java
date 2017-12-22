package com.memoria.felipe.indoorlocation.Utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
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

    // Como los datos vienen escalados, no se necesita la media = 0
    public static RealVector PCATransform(RealMatrix pcaMatrixTransform, RealVector v ){

        RealVector newVector = pcaMatrixTransform.transpose().preMultiply(v);
        return newVector;

    }

    public static Properties load(String propertiesName, Context ctx) throws IOException {
        Properties props = new Properties();
        InputStream inputStream =  ctx.getAssets().open(propertiesName);
        props.load(inputStream);
        return props;
    }

    public static double[][][] convert(double[][][] output, String[] data) {
        for (int i = 0, x = 0, xl = output.length; x < xl; x++) {
            for (int y = 0, yl = output[x].length; y < yl; y++) {
                for (int z = 0, zl = output[x][y].length; z < zl; z++) {
                    output[x][y][z] = Double.parseDouble(data[i++]);
                }
            }
        }
        return output;
    }

    public static double[][] convert(double[][] output, String[] data) {
        for (int i = 0, x = 0, xl = output.length; x < xl; x++) {
            for (int y = 0, yl = output[x].length; y < yl; y++) {
                output[x][y] = Double.parseDouble(data[i++]);
            }
        }
        return output;
    }

    public static double[] convert(double[] output, String[] data) {
        for (int i = 0, x = 0, xl = output.length; x < xl; x++) {
            output[x] = Double.parseDouble(data[i++]);
        }
        return output;
    }

    public static int[] convert(int[] output, String[] data) {
        for (int i = 0, x = 0, xl = output.length; x < xl; x++) {
            output[x] = Integer.parseInt(data[i++].trim());
        }
        return output;
    }

    public static String[] getData(String stringArray){
        String[] nuevo  = stringArray.replace("{","").replace("}","").split(",");
        return nuevo;
    }

}
