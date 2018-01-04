package com.memoria.felipe.indoorlocation.Screens;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealVectorFormat;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;
import com.memoria.felipe.indoorlocation.Fragments.OnlineFragment;
import com.memoria.felipe.indoorlocation.Fragments.SettingsFragment;
import com.memoria.felipe.indoorlocation.Utils.App;
import com.memoria.felipe.indoorlocation.Utils.CustomBeacon;
import com.memoria.felipe.indoorlocation.Utils.FragmentAdapterIndoor;
import com.memoria.felipe.indoorlocation.Utils.KNN.KnnOriginalX;
import com.memoria.felipe.indoorlocation.Utils.KNN.KnnOriginalY;
import com.memoria.felipe.indoorlocation.Utils.KNN.KnnPCAX;
import com.memoria.felipe.indoorlocation.Utils.KNN.KnnPCAY;
import com.memoria.felipe.indoorlocation.Utils.KnnPorter;
import com.memoria.felipe.indoorlocation.Utils.MapBoxOfflineTileProvider;
import com.memoria.felipe.indoorlocation.Fragments.OfflineFragment;
import com.memoria.felipe.indoorlocation.R;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacon_RSSI;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacon_RSSIDao;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacons;
import com.memoria.felipe.indoorlocation.Utils.Model.BeaconsDao;
import com.memoria.felipe.indoorlocation.Utils.Model.DaoSession;
import com.memoria.felipe.indoorlocation.Utils.Model.Fingerprint;
import com.memoria.felipe.indoorlocation.Utils.Model.FingerprintDao;
import com.memoria.felipe.indoorlocation.Utils.SVM.SVMOriginalX;
import com.memoria.felipe.indoorlocation.Utils.SVM.SVMOriginalY;
import com.memoria.felipe.indoorlocation.Utils.SVM.SVMPCAX;
import com.memoria.felipe.indoorlocation.Utils.SVM.SVMPCAY;
import com.memoria.felipe.indoorlocation.Utils.SVMPorter;
import com.memoria.felipe.indoorlocation.Utils.UtilsFunctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import biz.laenger.android.vpbs.BottomSheetUtils;
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, OfflineFragment.OnFragmentOfflineListener,
        OnlineFragment.OnFragmentOnlineListener, SettingsFragment.OnFragmentInteractionListener{


    // Usados por libreria tensorflow
    static {
        System.loadLibrary("tensorflow_inference");
    }
    private static final String MODEL_FILE = "file:///android_asset/optimized_NN.pb";
    private static final String MODEL_FILE_PCA = "file:///android_asset/optimized_NN_PCA.pb";
    private static final String INPUT_NODE = "I";
    private static final String OUTPUT_NODE = "O";
    private static final String INPUT_NODE_PCA = "I";
    private static final String OUTPUT_NODE_PCA = "O";
    private TensorFlowInferenceInterface inferenceInterface;
    private TensorFlowInferenceInterface inferenceInterfacePCA;
    private SensorManager mSensorManager;
    private HashMap<Marker,LatLng> markers = new HashMap<Marker,LatLng>();

    private LinearLayout mLinearBotomSheet;
    private ViewPagerBottomSheetBehavior mBottomBehavior;
    private TabLayout mTabLayoutBottom;
    private ViewPager mViewPagerBottom;

    //Init proximity manager for ranging
    private ProximityManager proximityManager;
    public static final String TAG = "ProximityManager";

    // Permises granted
    public static final int REQUEST_CODE_PERMISSIONS = 100;

    private GoogleMap mMap;
    // Where is the actual map used
    private LatLngBounds restrictions = new LatLngBounds(
            new LatLng(-1, -1), new LatLng(37, 145.75));
    private final static  String MAP_OVERLAY_FILENAME = "parking_origin_0-145_HD.mbtiles";

    private CustomBeacon mCloseBeacon;
    private Map<String, CustomBeacon> map = new HashMap<String, CustomBeacon>();
    private DaoSession daoSession;
    private BeaconsDao beaconsDao;
    private FingerprintDao fingerprintDao;

    private ProgressDialog mProgressDialogScan;
    private ProgressDialog mProgressDialogOnline;
    private int counter=0;
    private Double ActualXPosition;
    private Double ActualYPosition;
    private Double mXPositionOnlineStatic;
    private Double mYPositionOnlineStatic;
    private boolean mPCAActive = false;
    // modo algoritmo: 0-knn, 1-svm, 2-nn
    private int mModeAlgorithm = 0;
    //private Integer ActualOrientation;
    private Boolean mcanStartTakeMeditions = false;
    private static Integer NUMBER_OF_MEDITIONS = 10;
    private static Integer INTERVAL_MEDITIONS = 350;
    private RealMatrix PCAMatrixTransform;
    private RealMatrix PCAMatrix;
    private RealMatrix originalScaled;
    private RealVector scaleVector;
    private RealVector meanVector;
    RealVectorFormat format = new RealVectorFormat();
    private double[][] svs_original_x;
    private double[][] coeffs_original_x;
    private double[][] svs_original_y;
    private double[][] coeffs_original_y;
    private double[][] svs_pca_x;
    private double[][] coeffs_pca_x;
    private double[][] svs_pca_y;
    private double[][] coeffs_pca_y;
    private int[] y_knn_original_x;
    private int[] y_knn_original_y;
    private List<Double> mAgrupacionX = new ArrayList<Double>();
    private List<Double> mAgrupacionY = new ArrayList<Double>();
    private FileOutputStream streamDataStatic;

    // Variables para imprimir en archivos
    private List<String> KNN_ORIGINAL_LIST_X = new ArrayList<String>();
    private List<String> KNN_ORIGINAL_LIST_Y = new ArrayList<String>();
    private List<String> KNN_PCA_LIST_X = new ArrayList<String>();
    private List<String> KNN_PCA_LIST_Y = new ArrayList<String>();
    private List<String> SVM_ORIGINAL_LIST_X = new ArrayList<String>();
    private List<String> SVM_ORIGINAL_LIST_Y = new ArrayList<String>();
    private List<String> SVM_PCA_LIST_X = new ArrayList<String>();
    private List<String> SVM_PCA_LIST_Y = new ArrayList<String>();
    private List<String> NN_ORIGINAL_LIST_X = new ArrayList<String>();
    private List<String> NN_ORIGINAL_LIST_Y = new ArrayList<String>();
    private List<String> NN_PCA_LIST_X = new ArrayList<String>();
    private List<String> NN_PCA_LIST_Y = new ArrayList<String>();
    private List<Long> mTimers = new ArrayList<Long>(Collections.nCopies(6, 0L));
    private int counterTimers = 0;
    private Boolean isDinamic = false;
    private Circle circle;
    private String mActualBeaconNameScaned;
    private List<String> actualRssiBeaconByName = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Init kontakt sdk
        KontaktSDK.initialize(this);
        checkPermissions();
        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i = 0;i<deviceSensors.size();i++){
            Log.e("Sensor", deviceSensors.get(i).toString());
        }

        try{
            Properties model = UtilsFunctions.load("app.properties", this);
            String[] dataSvs_original_x = UtilsFunctions.getData(model.getProperty("svs_original_x"));
            String[] dataCoeffs_original_x = UtilsFunctions.getData(model.getProperty("coeffs_original_x"));
            String[] dataSvs_original_y = UtilsFunctions.getData(model.getProperty("svs_original_y"));
            String[] dataCoeffs_original_y = UtilsFunctions.getData(model.getProperty("coeffs_original_y"));
            String[] dataSvs_pca_x = UtilsFunctions.getData(model.getProperty("svs_pca_x"));
            String[] dataCoeffs_pca_x = UtilsFunctions.getData(model.getProperty("coeffs_pca_x"));
            String[] dataSvs_pca_y = UtilsFunctions.getData(model.getProperty("svs_pca_y"));
            String[] dataCoeffs_pca_y = UtilsFunctions.getData(model.getProperty("coeffs_pca_y"));
            int N_svs_original_x = SVMOriginalX.getN_svs();
            int N_svs_original_y = SVMOriginalY.getN_svs();
            int N_svs_pca_x = SVMPCAX.getN_svs();
            int N_svs_pca_y = SVMPCAY.getN_svs();
            //svs = UtilsFunctions.convert(new double[N_svs][2], dataSvs);
            //coeffs = UtilsFunctions.convert(new double[10][N_svs], dataCoeffs);
            String[] dataKNNOriginalX = UtilsFunctions.getData(model.getProperty("y_original_X"));
            String[] dataKNNOriginalY = UtilsFunctions.getData(model.getProperty("y_original_Y"));
            y_knn_original_x = UtilsFunctions.convert(new int[6600], dataKNNOriginalX);
            y_knn_original_y = UtilsFunctions.convert(new int[6600], dataKNNOriginalY);

            svs_original_x = UtilsFunctions.convert(new double[N_svs_original_x][8], dataSvs_original_x);
            coeffs_original_x = UtilsFunctions.convert(new double[10][N_svs_original_x], dataCoeffs_original_x);
            svs_original_y = UtilsFunctions.convert(new double[N_svs_original_y][8], dataSvs_original_y);
            coeffs_original_y = UtilsFunctions.convert(new double[3][N_svs_original_y], dataCoeffs_original_y);
            svs_pca_x = UtilsFunctions.convert(new double[N_svs_pca_x][4], dataSvs_pca_x);
            coeffs_pca_x = UtilsFunctions.convert(new double[10][N_svs_pca_x], dataCoeffs_pca_x);
            svs_pca_y = UtilsFunctions.convert(new double[N_svs_pca_y][4], dataSvs_pca_y);
            coeffs_pca_y = UtilsFunctions.convert(new double[3][N_svs_pca_y], dataCoeffs_pca_y);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }


        // Shared preferences
        SharedPreferences sharedPref = this.getSharedPreferences(
                "Preferencias_Scan", Context.MODE_PRIVATE);

        NUMBER_OF_MEDITIONS = sharedPref.getInt("Numero_Mediciones", 10);
        INTERVAL_MEDITIONS = sharedPref.getInt("Intervalo_Mediciones", 350);

        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE);
        inferenceInterfacePCA = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE_PCA);

        try{
            double [][] PCA_transform = UtilsFunctions.readFromFileMatrix(4,8,"PCA_transform.txt", this);
            double [][] PCA = UtilsFunctions.readFromFileMatrix(6600,4,"PCA.txt", this);
            double [] vectorScale = UtilsFunctions.readFromFileVector(8,"scale.txt", this);
            double [] vectorMean = UtilsFunctions.readFromFileVector(8,"mean.txt", this);
            double [][] original_scaled = UtilsFunctions.readFromFileMatrix(6600,8,"original_scaled.txt", this);
            RealVector newRssi = new ArrayRealVector(new double[] { -66,-92,-84,-84,-92,-93,-98,-96}, false);


            /*Log.e("Scale", Arrays.toString(vectorScale));
            Log.e("Mean", Arrays.toString(vectorMean));
            Log.e("PCA", Arrays.deepToString(PCA_transform));*/
            PCAMatrixTransform = MatrixUtils.createRealMatrix(PCA_transform);
            PCAMatrix = MatrixUtils.createRealMatrix(PCA);
            originalScaled = MatrixUtils.createRealMatrix(original_scaled);
            scaleVector = new ArrayRealVector(vectorScale, false);
            meanVector = new ArrayRealVector(vectorMean, false);
            RealVector minus = newRssi.subtract(meanVector);

            //RealVector response =  UtilsFunctions.scaleData(meanVector,scaleVector,newRssi);
            //Log.e("Respuesta", format.format(response));

        }
        catch (IOException ex){
            ex.printStackTrace();
        }

        daoSession = ((App) getApplication()).getDaoSession();
        beaconsDao = daoSession.getBeaconsDao();
        fingerprintDao = daoSession.getFingerprintDao();
        /*Log.e("KNN Original X", String.valueOf(valueKnnOX));
        Log.e("KNN Original Y", String.valueOf(valueKnnOY));
        Log.e("KNN PCA X", String.valueOf(valueKnnPCAX));
        Log.e("KNN PCA Y", String.valueOf(valueKnnPCAY));
        Log.e("SVM Original X", String.valueOf(valueSVMOX));
        Log.e("SVM Original Y", String.valueOf(valueSVMOY));
        Log.e("SVM PCA X", String.valueOf(valueSVMPCAX));
        Log.e("SVM PCA Y", String.valueOf(valueSVMPCAY));
        double svmpredict = SVMPorter.predict(m.toArray(), svs, coeffs);
        Log.e("Porter", String.valueOf(svmpredict));*/

        mProgressDialogScan = new ProgressDialog(this);
        mProgressDialogOnline =  new ProgressDialog(this);
        mProgressDialogOnline.setIndeterminate(true);
        mProgressDialogOnline.setCancelable(false);
        mProgressDialogScan.setCancelable(false);
        initProximityManager();
        setupBottomSheet();

        createBeaconProMap("6273745a4532", "f7826da6bc5b71e0893f", "CF:72:17:A9:0E:79","",-77,"C1hA");
        createBeaconProMap("6b786a437062", "f7826da6bc5b71e0893f", "D9:D6:91:B5:F8:8B","",-77,"F39L");

        mCloseBeacon = new CustomBeacon();

        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);*/
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    public void setScanValues(Integer numeroMediciones, Integer intervalo){
        SharedPreferences sharedPref = this.getSharedPreferences("Preferencias_Scan", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("Numero_Mediciones", numeroMediciones);
        editor.putInt("Intervalo_Mediciones", numeroMediciones);
        editor.commit();

        NUMBER_OF_MEDITIONS = numeroMediciones;
        INTERVAL_MEDITIONS = intervalo;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //startScanning();
    }

    public void initProximityManager(){
        proximityManager = ProximityManagerFactory.create(this);
        //Configure proximity manager basic options
        proximityManager.configuration()
                //Using ranging for continuous scanning or MONITORING for scanning with intervals
                .scanPeriod(ScanPeriod.RANGING)
                //Using BALANCED for best performance/battery ratio
                .scanMode(ScanMode.BALANCED)
                //OnDeviceUpdate callback will be received with 5 seconds interval
                .deviceUpdateCallbackInterval(INTERVAL_MEDITIONS);

        //proximityManager.filters().eddystoneFilter(createFilterBeaconPro());
        proximityManager.setEddystoneListener(createEddystoneListener(1));
    }

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public void loadBeaconsMarkers(GoogleMap googleMap){
        List<Beacons> beacons = beaconsDao.loadAll();
        for( int i =0; i< beacons.size(); i++){
            Beacons actualBeacon = beacons.get(i);
            LatLng position = new LatLng(actualBeacon.getYPosition(), actualBeacon.getXPosition());
            Marker mk = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .draggable(true)
                    .title(actualBeacon.getUniqueId())
                    .snippet("x: " + actualBeacon.getXPosition() + ", y: " + actualBeacon.getYPosition())
                    .anchor(0.5f,0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("smart_beacon",136,77))));

            mk.setTag(actualBeacon);

        }
    }

    public void loadFingerprintsMarkers(GoogleMap googleMap){
        FingerprintDao fingerprintDao = daoSession.getFingerprintDao();
        List<Fingerprint> fingerprints = fingerprintDao.loadAll();
        Map<Double, List<Fingerprint> > mapX = StreamSupport.stream(fingerprints)
                                        .collect(Collectors.groupingBy(x->x.getXPosition()));

        for (Map.Entry<Double, List<Fingerprint>> entry : mapX.entrySet()) {

            Map<Double, List<Fingerprint> > mapY = StreamSupport.stream(entry.getValue())
                    .collect(Collectors.groupingBy(x->x.getYPosition()));

            for (Map.Entry<Double, List<Fingerprint>> entry2 : mapY.entrySet()) {
                LatLng position = new LatLng(entry2.getKey(), entry.getKey());
                Marker mk = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .draggable(true)
                        .title("Fingerprints")
                        .snippet("x: " + entry.getKey() + ", y: " + entry2.getKey()  +"\n"
                                +"Number of fingerprints: " + entry2.getValue().size() )
                        .anchor(0.5f,0.5f)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("red_circle",25,25))));

                mk.setTag(entry2.getValue().get(0));
                markers.put(mk,position);

            }

        }

    }

    public void deleteMarker(Marker marker){
        try{
            if(marker.getTag() instanceof Beacons){
                Beacons bc =(Beacons) marker.getTag();
                daoSession.delete(bc);
                Toast.makeText(getApplicationContext(), "Beacon borrado con exito", Toast.LENGTH_SHORT).show();
                marker.remove();
            }
            else{
                Fingerprint fingerprint =(Fingerprint)marker.getTag();
                FingerprintDao fingerprintDao = daoSession.getFingerprintDao();
                Beacon_RSSIDao beacon_rssiDao = daoSession.getBeacon_RSSIDao();
                List<Fingerprint> fingerprints = fingerprintDao.queryBuilder()
                        .where(FingerprintDao.Properties.XPosition.eq(fingerprint.getXPosition()),
                                FingerprintDao.Properties.YPosition.eq(fingerprint.getYPosition()))
                        .list();

                for(int i=0; i<fingerprints.size(); i++){
                    fingerprints.get(i).resetRssi();
                    List<Beacon_RSSI> beacon_rssis = fingerprints.get(i).getRssi();
                    beacon_rssiDao.deleteInTx(beacon_rssis);
                }

                fingerprintDao.deleteInTx(fingerprints);
                Toast.makeText(getApplicationContext(), "Fingerprint Borrados", Toast.LENGTH_SHORT).show();
                marker.remove();
            }

        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), "Error al intentar eliminar", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    @Override
    public void onDeleteDB(){
        try{
            daoSession.getBeacon_RSSIDao().deleteAll();
            daoSession.getBeaconsDao().deleteAll();
            daoSession.getFingerprintDao().deleteAll();
            markers.clear();
            mMap.clear();
            TileOverlayOptions opts = new TileOverlayOptions();

            // Get a File reference to the MBTiles file.
            File myMBTiles = loadFilefromAssets(null);

            // Create an instance of MapBoxOfflineTileProvider.
            MapBoxOfflineTileProvider provider = new MapBoxOfflineTileProvider(myMBTiles);

            // Set the tile provider on the TileOverlayOptions.
            opts.tileProvider(provider);

            // Add the tile overlay to the map.
            TileOverlay overlay = mMap.addTileOverlay(opts);
            LatLng sydney = new LatLng(0, 0);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            Toast.makeText(this, "Base de datos eliminada con exito", Toast.LENGTH_SHORT).show();

        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Problema al eliminar la base de datos", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     *
     * @param mode is scanning for insert beacon, 0 or 1 scanning data
     * @return
     */
    private EddystoneListener createEddystoneListener(int mode) {
        return new SimpleEddystoneListener() {
            @Override
            public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
                if(mode == 1){
                    Log.i(TAG, "onEddystonesUpdated: " + eddystones.size());
                    for(int i= 0; i<eddystones.size(); i++){
                        Log.i(TAG, "onEddystoneUpdate: " + eddystones.get(i).toString());
                    }

                    if(!mcanStartTakeMeditions){
                        return;
                    }

                    try{
                        counter+=1;
                        List<String> macsIn = new ArrayList<String>();

                        FingerprintDao fingerprintDao = daoSession.getFingerprintDao();
                        Fingerprint fingerprint = new Fingerprint();
                        fingerprint.setXPosition(ActualXPosition);
                        fingerprint.setYPosition(ActualYPosition);
                        /*switch(ActualOrientation){
                            case 0:
                                fingerprint.setNorte(1);
                                break;
                            case 1:
                                fingerprint.setEste(1);
                                break;
                            case 2:
                                fingerprint.setOeste(1);
                                break;
                            case 3:
                                fingerprint.setSur(1);
                                break;
                            default:
                                break;
                        }*/
                        long idFingerprint = daoSession.insert(fingerprint);
                        fingerprint = fingerprintDao.load(idFingerprint);

                        for(int i= 0; i<eddystones.size(); i++){
                            String macBeacon;
                            IEddystoneDevice actualEddystone = eddystones.get(i);
                            if(eddystones.get(i).getUniqueId()!=null){
                                macBeacon = actualEddystone.getAddress();
                            }
                            else{
                                macBeacon = map.get(actualEddystone.getInstanceId()).getMAC();
                            }
                            Beacons beacons = beaconsDao.queryBuilder()
                                    .where(BeaconsDao.Properties.MAC.eq(macBeacon)).unique();

                            if(beacons!=null){
                                Beacon_RSSI beacon_rssi = new Beacon_RSSI();
                                beacon_rssi.setRssi(actualEddystone.getRssi());
                                beacon_rssi.setFingerprintId(fingerprint.getId());

                                macsIn.add(macBeacon);
                                beacon_rssi.setBeaconId(beacons.getId());
                                daoSession.insert(beacon_rssi);
                            }

                        }

                        List<Beacons> notInBeacons = beaconsDao.queryBuilder()
                                .where(BeaconsDao.Properties.MAC.notIn(macsIn)).list();

                        for(int j = 0; j<notInBeacons.size();j++ ){
                            Beacon_RSSI beacon_rssi = new Beacon_RSSI();
                            beacon_rssi.setRssi(100);
                            beacon_rssi.setFingerprintId(fingerprint.getId());
                            Beacons beacons = beaconsDao.queryBuilder()
                                    .where(BeaconsDao.Properties.MAC.eq(notInBeacons.get(j).getMAC())).unique();
                            beacon_rssi.setBeaconId(beacons.getId());
                            daoSession.insert(beacon_rssi);
                        }

                        mProgressDialogScan.incrementProgressBy(1);

                        if(counter==NUMBER_OF_MEDITIONS){
                            onFingerprintCollected(fingerprint);
                            counter =0;
                        }
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }

                }
                else{
                    Log.i(TAG, "onEddystonesUpdated: " + eddystones.size());
                    for(int i= 0; i<eddystones.size(); i++){
                        Log.i(TAG, "onEddystoneUpdate: " + eddystones.get(i).toString());
                    }

                    IEddystoneDevice inmediatly =  StreamSupport.stream(eddystones)
                            .max((x1,x2)->Integer.compare(x1.getRssi(),x2.getRssi()))
                            .get();

                    if(inmediatly.getRssi()>mCloseBeacon.getRssi()){
                        if(inmediatly.getUniqueId() != null){
                            mCloseBeacon.setInstanceId(inmediatly.getInstanceId());
                            mCloseBeacon.setMAC(inmediatly.getAddress());
                            mCloseBeacon.setName(inmediatly.getName());
                            mCloseBeacon.setNameSpace(inmediatly.getNamespace());
                            mCloseBeacon.setRssi(inmediatly.getRssi());
                            mCloseBeacon.setTxPower(inmediatly.getTxPower());
                            mCloseBeacon.setUniqueId(inmediatly.getUniqueId());
                        }
                        else{
                            CustomBeacon mBeaconPro = map.get(inmediatly.getInstanceId());
                            mCloseBeacon.setInstanceId(mBeaconPro.getInstanceId());
                            mCloseBeacon.setMAC(mBeaconPro.getMAC());
                            mCloseBeacon.setName(mBeaconPro.getName());
                            mCloseBeacon.setNameSpace(mBeaconPro.getNameSpace());
                            mCloseBeacon.setRssi(inmediatly.getRssi());
                            mCloseBeacon.setTxPower(mBeaconPro.getTxPower());
                            mCloseBeacon.setUniqueId(mBeaconPro.getUniqueId());
                        }

                    }

                }

            }
        };
    }

    private EddystoneListener createEddystoneListenerOnline(int mode) {
        return new SimpleEddystoneListener() {
            @Override
            public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
                if(mode == 1){
                    Log.i(TAG, "onEddystonesUpdated: " + eddystones.size());
                    for(int i= 0; i<eddystones.size(); i++){
                        Log.i(TAG, "onEddystoneUpdate: " + eddystones.get(i).toString());
                    }

                    try{
                        if(mcanStartTakeMeditions){
                            List<Beacons> beaconses = beaconsDao.loadAll();
                            List<String> allUniqueId = StreamSupport.stream(beaconses)
                                    .map(x->x.getUniqueId()).collect(Collectors.toList());
                            HashMap<Integer, Double> mapBeacons = new LinkedHashMap<Integer, Double>();
                            List<String> idIn = new ArrayList<String>();
                            for(int i= 0; i<eddystones.size(); i++){
                                String macBeacon;
                                IEddystoneDevice actualEddystone = eddystones.get(i);
                                if(actualEddystone.getUniqueId()!=null){
                                    macBeacon = actualEddystone.getAddress();
                                }
                                else{
                                    macBeacon = map.get(actualEddystone.getInstanceId()).getMAC();
                                }
                                Beacons beacons = beaconsDao.queryBuilder()
                                        .where(BeaconsDao.Properties.MAC.eq(macBeacon)).unique();

                                if(beacons!=null){
                                    String actualUniqueId = beacons.getUniqueId();
                                    int elementPosition = allUniqueId.indexOf(actualUniqueId);

                                    if(elementPosition!=-1){
                                        mapBeacons.put(elementPosition,(double)actualEddystone.getRssi());
                                        idIn.add(actualUniqueId);
                                    }
                                }
                            }

                            List<Beacons> beaconsNotIn = beaconsDao.queryBuilder()
                                    .where(BeaconsDao.Properties.UniqueId.notIn(idIn)).list();

                            List<String> uniqueIdNotIn = StreamSupport.stream(beaconsNotIn)
                                    .map(x->x.getUniqueId()).collect(Collectors.toList());

                            for(int j =0; j<uniqueIdNotIn.size();j++){
                                int elementPosition = allUniqueId.indexOf(uniqueIdNotIn.get(j));
                                if(elementPosition!=-1) {
                                    mapBeacons.put(elementPosition, 100d);
                                }
                            }

                            double[] finalArray = new double[beaconses.size()];
                            for(int k =0; k<mapBeacons.size(); k++){
                                finalArray[k] = mapBeacons.get(k);
                            }

                            Log.e("Arreglo actual", Arrays.toString(finalArray));

                            List<Double> actualCoord =  calculatePosition(finalArray);
                            LatLng actualLatLng = new LatLng(actualCoord.get(1), actualCoord.get(0));

                            circle.setCenter(actualLatLng);

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(actualLatLng));



                        }


                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }

                }

            }
        };
    }


    private EddystoneListener createEddystoneListenerName() {
        return new SimpleEddystoneListener() {
            @Override
            public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {

                Log.i(TAG, "onEddystonesUpdated: " + eddystones.size());
                for(int i= 0; i<eddystones.size(); i++){
                    Log.i(TAG, "onEddystoneUpdate: " + eddystones.get(i).toString());
                }

                try{
                    if(mcanStartTakeMeditions){
                        List<Beacons> beaconses = beaconsDao.loadAll();
                        List<String> allUniqueId = StreamSupport.stream(beaconses)
                                .map(x->x.getUniqueId()).collect(Collectors.toList());
                        HashMap<Integer, Double> mapBeacons = new LinkedHashMap<Integer, Double>();
                        List<String> idIn = new ArrayList<String>();
                        for(int i= 0; i<eddystones.size(); i++){
                            String macBeacon;
                            IEddystoneDevice actualEddystone = eddystones.get(i);
                            if(actualEddystone.getUniqueId()!=null){
                                macBeacon = actualEddystone.getAddress();
                            }
                            else{
                                macBeacon = map.get(actualEddystone.getInstanceId()).getMAC();
                            }
                            Beacons beacons = beaconsDao.queryBuilder()
                                    .where(BeaconsDao.Properties.MAC.eq(macBeacon)).unique();

                            if(beacons!=null){
                                if(beacons.getUniqueId().equals(mActualBeaconNameScaned)){
                                    actualRssiBeaconByName.add(String.valueOf(actualEddystone.getRssi()));
                                }
                            }
                        }
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        };
    }

    //Since Android Marshmallow starting a Bluetooth Low Energy scan requires permission from location group.
    private void checkPermissions() {
        int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult) {
            //Permission not granted so we ask for it. Results are handled in onRequestPermissionsResult() callback.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (REQUEST_CODE_PERMISSIONS == requestCode) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            }
        } else {
            //disableButtons();
            Toast.makeText(this, "Location permissions are mandatory to use BLE features on Android 6.0 or higher", Toast.LENGTH_LONG).show();
        }
    }

    private void setupBottomSheet() {

        mLinearBotomSheet = (LinearLayout)findViewById(R.id.linear_botom_sheet);
        mBottomBehavior = ViewPagerBottomSheetBehavior.from(mLinearBotomSheet);
        mTabLayoutBottom = (TabLayout) findViewById(R.id.bottom_sheet_tabs);
        mViewPagerBottom = (ViewPager)findViewById(R.id.bottom_sheet_viewpager);

        mViewPagerBottom.setOffscreenPageLimit(3);
        mViewPagerBottom.setAdapter(new FragmentAdapterIndoor(getSupportFragmentManager(),
                MainActivity.this));
        mTabLayoutBottom.setupWithViewPager(mViewPagerBottom);
        int[] imageResId = {
                R.drawable.ic_edit_location_white_24dp,
                R.drawable.ic_location_searching_white_24dp,
                R.drawable.ic_settings_white_24dp };

        for (int i = 0; i < imageResId.length; i++) {
            mTabLayoutBottom.getTabAt(i).setIcon(imageResId[i]);
        }
        BottomSheetUtils.setupViewPager(mViewPagerBottom);
    }

    private void createBeaconProMap(String instanceId, String nameSpace,
                                    String MAC, String Name, int TxPower, String UniqueId){

        CustomBeacon nBeacon = new CustomBeacon();
        nBeacon.setInstanceId(instanceId);
        nBeacon.setName(Name);
        nBeacon.setUniqueId(UniqueId);
        nBeacon.setTxPower(TxPower);
        nBeacon.setMAC(MAC);
        nBeacon.setNameSpace(nameSpace);

        map.put(instanceId, nBeacon);

    }

    public void onFingerprintCollected(Fingerprint fingerprint){
        stopScanning();
        mcanStartTakeMeditions = false;
        mProgressDialogScan.dismiss();
        mProgressDialogScan.setProgress(0);
        //ActualOrientation = null;
        moveCameraToNewFingerprint(fingerprint);

    }

    public void moveCameraToNewFingerprint(Fingerprint fingerprint){
        if(mMap!=null){
            FingerprintDao fingerprintDao = daoSession.getFingerprintDao();
            List<Fingerprint> fingerprints = fingerprintDao.queryBuilder()
                    .where(FingerprintDao.Properties.XPosition.eq(ActualXPosition),
                            FingerprintDao.Properties.YPosition.eq(ActualYPosition))
                    .list();
            LatLng position = new LatLng(ActualYPosition, ActualXPosition);
            for(Iterator<Map.Entry<Marker, LatLng>> it = markers.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Marker, LatLng> entry = it.next();
                if(entry.getValue().equals(position)) {
                    entry.getKey().remove();
                    it.remove();
                }
            }
            Marker mk = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .draggable(true)
                    .title("Fingerprints")
                    .snippet("x: " + ActualXPosition + ", y: " + ActualYPosition +"\n"
                                +"Number of fingerprints: " + fingerprints.size() )
                    .anchor(0.5f,0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("red_circle",25,25))));

            mk.setTag(fingerprints.get(0));
            markers.put(mk,position);
            //mBottomBehavior.setState(ViewPagerBottomSheetBehavior.STATE_COLLAPSED);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        }
        ActualXPosition = null;
        ActualYPosition = null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        initializeMap();

        TileOverlayOptions opts = new TileOverlayOptions();

        // Get a File reference to the MBTiles file.
        File myMBTiles = loadFilefromAssets(null);

        // Create an instance of MapBoxOfflineTileProvider.
        MapBoxOfflineTileProvider provider = new MapBoxOfflineTileProvider(myMBTiles);

        // Set the tile provider on the TileOverlayOptions.
        opts.tileProvider(provider);

        // Add the tile overlay to the map.
        TileOverlay overlay = mMap.addTileOverlay(opts);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                deleteMarker(marker);
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });

        // Sometime later when the map view is destroyed, close the provider.
        // This is important to prevent a leak of the backing SQLiteDatabase.
        //provider.close();
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(0, 0);
        loadBeaconsMarkers(mMap);
        loadFingerprintsMarkers(mMap);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        circle = mMap.addCircle(new CircleOptions()
                .center(sydney)
                .radius(100000d)
                .strokeColor(Color.WHITE)
                .fillColor(Color.parseColor("#4285F4"))
                .zIndex(100f));


    }

    public File loadFilefromAssets(String fileName){
        // Get a File from Assets reference to the MBTiles file.
        File file = new File(getCacheDir()+"/" + MAP_OVERLAY_FILENAME);
        if (!file.exists())
            try {

                InputStream is = getAssets().open(MAP_OVERLAY_FILENAME);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();
                return file;
            }
            catch (Exception e) { throw new RuntimeException(e); }
        else{
            return file;
        }
    }

    public void initializeMap(){
        if(mMap!=null){
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setPadding(600,40,0,0);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(this, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setLatLngBoundsForCameraTarget(restrictions);
            mMap.setMinZoomPreference(4);
            mMap.setMaxZoomPreference(5);
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    LinearLayout info = new LinearLayout(MainActivity.this);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(MainActivity.this);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(MainActivity.this);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
        }
    }

    /*@Override
    public void onFragmentInteraction(Uri uri) {

    }*/

    private void startScanning(int mode) {
        //Connect to scanning service and start scanning when ready
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                //Check if proximity manager is already scanning
                if (proximityManager.isScanning()) {
                    Toast.makeText(MainActivity.this, "Ya esta escaneando", Toast.LENGTH_SHORT).show();
                    return;
                }

                proximityManager.setEddystoneListener(createEddystoneListener(mode));
                proximityManager.startScanning();
                Toast.makeText(MainActivity.this, "Escaneando...", Toast.LENGTH_SHORT).show();
                if(mode==0){
                    onServiceReadyBeacon();
                }

            }
        });
    }

    private void startScanningOnline(int mode){
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                //Check if proximity manager is already scanning
                if (proximityManager.isScanning()) {
                    Toast.makeText(MainActivity.this, "Ya esta escaneando", Toast.LENGTH_SHORT).show();
                    return;
                }

                proximityManager.setEddystoneListener(createEddystoneListenerOnline(mode));
                proximityManager.startScanning();
                Toast.makeText(MainActivity.this, "Escaneando...", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void startScanningName(){
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                //Check if proximity manager is already scanning
                if (proximityManager.isScanning()) {
                    Toast.makeText(MainActivity.this, "Ya esta escaneando", Toast.LENGTH_SHORT).show();
                    return;
                }

                proximityManager.setEddystoneListener(createEddystoneListenerName());
                proximityManager.startScanning();
                Toast.makeText(MainActivity.this, "Escaneando...", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void stopScanning() {
        //Stop scanning if scanning is in progress
        if (proximityManager.isScanning()) {
            proximityManager.stopScanning();
            Toast.makeText(this, "Escaneo detenido", Toast.LENGTH_SHORT).show();
        }
    }

    public List<Double> calculatePosition(double[] arregloRssi){

        List<Double> coordenadas = new ArrayList<Double>();
        RealVector newRssi = new ArrayRealVector(arregloRssi, false);
        RealVector scaledInput =  UtilsFunctions.scaleData(meanVector,scaleVector,newRssi);
        RealVector pcaInput = null;
        pcaInput = UtilsFunctions.PCATransform(PCAMatrixTransform, scaledInput);

        double[] pcaInputDouble = pcaInput.toArray();

        float[] arrayRssiPCAFloat = new float[pcaInputDouble.length];
        for (int i = 0 ; i < pcaInputDouble.length; i++)
        {
            arrayRssiPCAFloat[i] = (float) pcaInputDouble[i];
        }

        float[] arrayRssiFloat = new float[arregloRssi.length];
        for (int i = 0 ; i < arregloRssi.length; i++)
        {
            arrayRssiFloat[i] = (float) arregloRssi[i];
        }

        long time = System.currentTimeMillis();
        int valueKnnOX = KnnOriginalX.predict(scaledInput.toArray(), originalScaled.getData(), y_knn_original_x);
        int valueKnnOY = KnnOriginalY.predict(scaledInput.toArray(), originalScaled.getData(), y_knn_original_y);
        mTimers.set(0, mTimers.get(0) +  System.currentTimeMillis()-time);
        time = System.currentTimeMillis();
        int valueKnnPCAX = KnnPCAX.predict(pcaInput.toArray(), PCAMatrix.getData(), y_knn_original_x);
        int valueKnnPCAY = KnnPCAY.predict(pcaInput.toArray(), PCAMatrix.getData(), y_knn_original_y);
        mTimers.set(1, mTimers.get(1) +  System.currentTimeMillis()-time);
        time = System.currentTimeMillis();
        Double valueSVMOX = SVMOriginalX.predict(scaledInput.toArray(), svs_original_x, coeffs_original_x);
        Double valueSVMOY = SVMOriginalY.predict(scaledInput.toArray(), svs_original_y, coeffs_original_y);
        mTimers.set(2, mTimers.get(2) +  System.currentTimeMillis()-time);
        time = System.currentTimeMillis();
        Double valueSVMPCAX = SVMPCAX.predict(pcaInput.toArray(), svs_pca_x, coeffs_pca_x);
        Double valueSVMPCAY = SVMPCAY.predict(pcaInput.toArray(), svs_pca_y, coeffs_pca_y);
        mTimers.set(3, mTimers.get(3) +  System.currentTimeMillis()-time);

        //KNN
        KNN_ORIGINAL_LIST_X.add(mAgrupacionX.get(valueKnnOX).toString());
        KNN_ORIGINAL_LIST_Y.add(mAgrupacionY.get(valueKnnOY).toString());
        KNN_PCA_LIST_X.add(mAgrupacionX.get(valueKnnPCAX).toString());
        KNN_PCA_LIST_Y.add(mAgrupacionY.get(valueKnnPCAY).toString());

        // SVM

        SVM_ORIGINAL_LIST_X.add(valueSVMOX.toString());
        SVM_ORIGINAL_LIST_Y.add(valueSVMOY.toString());
        SVM_PCA_LIST_X.add(valueSVMPCAX.toString());
        SVM_PCA_LIST_Y.add(valueSVMPCAY.toString());

        // NN

        time = System.currentTimeMillis();
        inferenceInterface.feed(INPUT_NODE, arrayRssiFloat, 1,8);
        inferenceInterface.run(new String[] {OUTPUT_NODE});
        int[] resuNN = new int[2];
        inferenceInterface.fetch(OUTPUT_NODE,resuNN);

        mTimers.set(4, mTimers.get(4) +  System.currentTimeMillis()-time);
        time = System.currentTimeMillis();

        inferenceInterfacePCA.feed(INPUT_NODE_PCA, arrayRssiPCAFloat, 1,4);
        inferenceInterfacePCA.run(new String[] {OUTPUT_NODE_PCA});
        int[] resuNNPCA = new int[2];
        inferenceInterfacePCA.fetch(OUTPUT_NODE_PCA,resuNNPCA);

        mTimers.set(5, mTimers.get(5) +  System.currentTimeMillis()-time);

        NN_ORIGINAL_LIST_X.add(mAgrupacionX.get(resuNN[0]).toString());
        NN_ORIGINAL_LIST_Y.add(mAgrupacionY.get(resuNN[1]).toString());
        NN_PCA_LIST_X.add(mAgrupacionX.get(resuNNPCA[0]).toString());
        NN_PCA_LIST_Y.add(mAgrupacionY.get(resuNNPCA[1]).toString());

        counterTimers +=1;

        if(mModeAlgorithm==0){

            if(mPCAActive){
                coordenadas.add(mAgrupacionX.get(valueKnnPCAX));
                coordenadas.add(mAgrupacionY.get(valueKnnPCAY));
            }
            else{
                coordenadas.add(mAgrupacionX.get(valueKnnOX));
                coordenadas.add(mAgrupacionY.get(valueKnnOY));
            }

        }
        else if(mModeAlgorithm ==1){

            if(mPCAActive){
                coordenadas.add(valueSVMPCAX);
                coordenadas.add(valueSVMPCAY);
            }
            else{
                coordenadas.add(valueSVMOX);
                coordenadas.add(valueSVMOY);
            }

        }
        else{
            if(mPCAActive){
                coordenadas.add(mAgrupacionX.get(resuNNPCA[0]));
                coordenadas.add(mAgrupacionY.get(resuNNPCA[1]));
            }
            else{

                coordenadas.add(mAgrupacionX.get(resuNN[0]));
                coordenadas.add(mAgrupacionY.get(resuNN[1]));
            }
        }

        //Log.e("pca Aplicado", format.format(pcaInput));

        // Se escalan los datos
        return coordenadas;

    }

    @Override
    protected void onStop() {
        stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;
        super.onDestroy();
    }

    @Override
    public void onRequestCloseBeacon() {
        startScanning(0);
    }

    @Override
    public void onInsertBeacon(Beacons beacon) {
        if(mMap!=null){
            LatLng position = new LatLng(beacon.getYPosition(), beacon.getXPosition());
            Marker mk = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .draggable(true)
                    .title(beacon.getUniqueId())
                    .snippet("x: " + beacon.getXPosition() + ", y: " + beacon.getYPosition())
                    .anchor(0.5f,0.5f)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("smart_beacon",136,77))));

            mk.setTag(beacon);
            mBottomBehavior.setState(ViewPagerBottomSheetBehavior.STATE_COLLAPSED);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));

        }
    }

    @Override
    public void onGetFingerprint(Double x, Double y) {
        Integer numberOfBeacons = beaconsDao.loadAll().size();
        if(numberOfBeacons==0){
            Toast.makeText(MainActivity.this, "Debes ingresar al menos un Beacon", Toast.LENGTH_SHORT).show();
        }
        else{
            mProgressDialogScan.setMax(NUMBER_OF_MEDITIONS);
            mProgressDialogScan.setTitle("Generando fingerprint");
            mProgressDialogScan.setMessage("Conectando...");
            mProgressDialogScan.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialogScan.show();
            ActualXPosition = x;
            ActualYPosition = y;
            //ActualOrientation = orientation;
            startScanning(1);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressDialogScan.setMessage("Obteniendo mediciones");
                    mProgressDialogScan.setProgress(0);
                    mcanStartTakeMeditions = true;
                }
            }, 5000);
        }

    }

    public void onServiceReadyBeacon(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("Termine", "Termine");
                stopScanning();
                proximityManager.setEddystoneListener(createEddystoneListener(1));
                OfflineFragment offFragment =  (OfflineFragment)mViewPagerBottom.getAdapter().instantiateItem(mViewPagerBottom,0);
                offFragment.captureNewBeacon(mCloseBeacon);

                mCloseBeacon.clearFields();
            }
        }, 10000);
    }

    @Override
    public void onRequestMeditionsData() {
        SettingsFragment settingsFragment =  (SettingsFragment) mViewPagerBottom.getAdapter().instantiateItem(mViewPagerBottom,2);
        settingsFragment.catchDataResults(NUMBER_OF_MEDITIONS, INTERVAL_MEDITIONS);
    }

    @Override
    public void onSetMeditionsData(Integer meditions, Integer interval) {

        try{
            SharedPreferences sharedPref = this.getSharedPreferences(
                    "Preferencias_Scan", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("Numero_Mediciones", meditions);
            editor.putInt("Intervalo_Mediciones", interval);
            editor.commit();
            NUMBER_OF_MEDITIONS = meditions;
            INTERVAL_MEDITIONS = interval;
            proximityManager.configuration()
                    //OnDeviceUpdate callback will be received with 5 seconds interval
                    .deviceUpdateCallbackInterval(INTERVAL_MEDITIONS);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void getStaticPositionEstimation(int mode, boolean pca, Double xCoord, Double yCoord, Boolean dinamic) {
        mProgressDialogOnline.setTitle("Iniciando Online");
        mProgressDialogOnline.setMessage("Conectando...");
        mProgressDialogOnline.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialogOnline.show();
        mXPositionOnlineStatic = xCoord;
        mYPositionOnlineStatic = yCoord;
        isDinamic = dinamic;
        mModeAlgorithm = mode;
        mPCAActive = pca;
        startScanningOnline(1);
        List<Fingerprint> fingerprints = fingerprintDao.loadAll();
        Map<Double, List<Fingerprint>> unorderedX = StreamSupport.stream(fingerprints)
                .collect(Collectors.groupingBy(x->x.getXPosition()));

        mAgrupacionX = StreamSupport.stream(unorderedX.keySet()).sorted().collect(Collectors.toList());

        Map<Double, List<Fingerprint>> unorderedY = StreamSupport.stream(fingerprints)
                .collect(Collectors.groupingBy(x->x.getYPosition()));

        mAgrupacionY = StreamSupport.stream(unorderedY.keySet()).sorted().collect(Collectors.toList());

        String external = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String folderS = external + File.separator +"Indoor";
        File file;
        if(isDinamic){
            file = new File(folderS, "dinamic.txt");
        }
        else{
            file = new File(folderS, "static.txt");
        }

        try{
            streamDataStatic = new FileOutputStream(file, true);
            if(isDinamic){
                streamDataStatic.write("Online\n\n".getBytes());
            }
            else{
                streamDataStatic.write("Static\n\n".getBytes());
            }

            streamDataStatic.write((xCoord.toString() + " " + yCoord.toString() + "\n\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialogOnline.dismiss();
                mcanStartTakeMeditions = true;
                circle.setVisible(true);
            }
        }, 5000);
    }

    @Override
    public void stopStaticPositionEstimation(){

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try{
                    streamDataStatic.write("KNN Original\n\n".getBytes());
                    streamDataStatic.write((StreamSupport.stream(KNN_ORIGINAL_LIST_X).collect(Collectors.joining(" ")) + "\n\n").getBytes());
                    streamDataStatic.write((StreamSupport.stream(KNN_ORIGINAL_LIST_Y).collect(Collectors.joining(" "))+ "\n\n").getBytes());
                    KNN_ORIGINAL_LIST_X.clear();
                    KNN_ORIGINAL_LIST_Y.clear();
                    Double knnResult = (double) mTimers.get(0)/counterTimers;
                    streamDataStatic.write((knnResult.toString() + "\n\n").getBytes());

                    streamDataStatic.write("KNN PCA\n\n".getBytes());
                    streamDataStatic.write((StreamSupport.stream(KNN_PCA_LIST_X).collect(Collectors.joining(" ")) + "\n\n").getBytes());
                    streamDataStatic.write((StreamSupport.stream(KNN_PCA_LIST_Y).collect(Collectors.joining(" "))+ "\n\n").getBytes());
                    KNN_PCA_LIST_X.clear();
                    KNN_PCA_LIST_Y.clear();
                    Double knnPCAResult = (double) mTimers.get(1)/counterTimers;
                    streamDataStatic.write((knnPCAResult.toString() + "\n\n").getBytes());

                    streamDataStatic.write("SVM ORIGINAL\n\n".getBytes());
                    streamDataStatic.write((StreamSupport.stream(SVM_ORIGINAL_LIST_X).collect(Collectors.joining(" ")) + "\n\n").getBytes());
                    streamDataStatic.write((StreamSupport.stream(SVM_ORIGINAL_LIST_Y).collect(Collectors.joining(" "))+ "\n\n").getBytes());
                    SVM_ORIGINAL_LIST_X.clear();
                    SVM_ORIGINAL_LIST_Y.clear();
                    Double svmResult = (double) mTimers.get(2)/counterTimers;
                    streamDataStatic.write((svmResult.toString() + "\n\n").getBytes());

                    streamDataStatic.write("SVM PCA\n\n".getBytes());
                    streamDataStatic.write((StreamSupport.stream(SVM_PCA_LIST_X).collect(Collectors.joining(" ")) + "\n\n").getBytes());
                    streamDataStatic.write((StreamSupport.stream(SVM_PCA_LIST_Y).collect(Collectors.joining(" "))+ "\n\n").getBytes());
                    SVM_PCA_LIST_X.clear();
                    SVM_PCA_LIST_Y.clear();
                    Double svmPCAResult = (double) mTimers.get(3)/counterTimers;
                    streamDataStatic.write((svmPCAResult.toString() + "\n\n").getBytes());

                    streamDataStatic.write("NN ORIGINAL\n\n".getBytes());
                    streamDataStatic.write((StreamSupport.stream(NN_ORIGINAL_LIST_X).collect(Collectors.joining(" ")) + "\n\n").getBytes());
                    streamDataStatic.write((StreamSupport.stream(NN_ORIGINAL_LIST_Y).collect(Collectors.joining(" "))+ "\n\n").getBytes());
                    NN_ORIGINAL_LIST_X.clear();
                    NN_ORIGINAL_LIST_Y.clear();
                    Double nnResult = (double) mTimers.get(4)/counterTimers;
                    streamDataStatic.write((nnResult.toString() + "\n\n").getBytes());

                    streamDataStatic.write("NN PCA\n\n".getBytes());
                    streamDataStatic.write((StreamSupport.stream(NN_PCA_LIST_X).collect(Collectors.joining(" ")) + "\n\n").getBytes());
                    streamDataStatic.write((StreamSupport.stream(NN_PCA_LIST_Y).collect(Collectors.joining(" "))+ "\n\n").getBytes());
                    NN_PCA_LIST_X.clear();
                    NN_PCA_LIST_Y.clear();
                    Double nnPCAResult = (double) mTimers.get(5)/counterTimers;
                    streamDataStatic.write((nnPCAResult.toString() + "\n\n").getBytes());

                    mTimers = new ArrayList<Long>(Collections.nCopies(6, 0L));
                    counterTimers = 0;
                    streamDataStatic.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                {
                    public void run() {
                        mProgressDialogOnline.dismiss();
                        stopScanning();
                        circle.setVisible(false);
                    }
                });

                mcanStartTakeMeditions = false;
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    @Override
    public void scanBeaconByName(String name){
        mActualBeaconNameScaned = name;
        mProgressDialogOnline.setTitle("Iniciando Online");
        mProgressDialogOnline.setMessage("Conectando...");
        mProgressDialogOnline.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialogOnline.show();

        startScanningName();

        String external = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String folderS = external + File.separator +"Indoor";
        File file;

        file = new File(folderS, "BeaconName.txt");

        try{
            streamDataStatic = new FileOutputStream(file, true);
            streamDataStatic.write((name + "\n\n").getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialogOnline.dismiss();
                mcanStartTakeMeditions = true;
            }
        }, 5000);
    }

    @Override
    public void stopScanBeaconByName(){

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try{

                    streamDataStatic.write((StreamSupport.stream(actualRssiBeaconByName).collect(Collectors.joining(" ")) + "\n\n").getBytes());
                    actualRssiBeaconByName.clear();
                    streamDataStatic.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                {
                    public void run() {
                        mProgressDialogOnline.dismiss();
                        stopScanning();
                    }
                });

                mcanStartTakeMeditions = false;
            }
        };

        Thread t = new Thread(r);
        t.start();
    }
}
