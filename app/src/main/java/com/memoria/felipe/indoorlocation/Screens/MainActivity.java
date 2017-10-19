package com.memoria.felipe.indoorlocation.Screens;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Debug;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.device.BeaconDevice;
import com.kontakt.sdk.android.ble.device.EddystoneDevice;
import com.kontakt.sdk.android.ble.filter.eddystone.EddystoneFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.SecureProfileListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSecureProfileListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.Proximity;
import com.kontakt.sdk.android.common.model.Namespace;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;
import com.kontakt.sdk.android.common.profile.ISecureProfile;
import com.kontakt.sdk.android.common.util.Constants;
import com.memoria.felipe.indoorlocation.Fragments.OnlineFragment;
import com.memoria.felipe.indoorlocation.Fragments.SettingsFragment;
import com.memoria.felipe.indoorlocation.Utils.App;
import com.memoria.felipe.indoorlocation.Utils.CustomBeacon;
import com.memoria.felipe.indoorlocation.Utils.FragmentAdapterIndoor;
import com.memoria.felipe.indoorlocation.Utils.MapBoxOfflineTileProvider;
import com.memoria.felipe.indoorlocation.Fragments.OfflineFragment;
import com.memoria.felipe.indoorlocation.R;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacon_RSSI;
import com.memoria.felipe.indoorlocation.Utils.Model.Beacons;
import com.memoria.felipe.indoorlocation.Utils.Model.BeaconsDao;
import com.memoria.felipe.indoorlocation.Utils.Model.DaoSession;
import com.memoria.felipe.indoorlocation.Utils.Model.Fingerprint;
import com.memoria.felipe.indoorlocation.Utils.Model.FingerprintDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import biz.laenger.android.vpbs.BottomSheetUtils;
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;
import java8.util.stream.Collectors;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, OfflineFragment.OnFragmentOfflineListener,
        OnlineFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener{


    // Usados por libreria tensorflow
    static {
        System.loadLibrary("tensorflow_inference");
    }
    private static final String MODEL_FILE = "file:///android_asset/optimized_svmX.pb";
    private static final String INPUT_NODE = "I";
    private static final String OUTPUT_NODE = "O";
    private TensorFlowInferenceInterface inferenceInterface;
    private SensorManager mSensorManager;
    private HashMap<Marker,LatLng> markers = new HashMap<Marker,LatLng>();

    private static final int[] INPUT_SIZE = {1,4};

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

    private ProgressDialog mProgressDialogScan;
    private int counter=0;
    private Double ActualXPosition;
    private Double ActualYPosition;
    //private Integer ActualOrientation;
    private Boolean mcanStartTakeMeditions = false;
    private static Integer NUMBER_OF_MEDITIONS = 10;
    private static Integer INTERVAL_MEDITIONS = 350;


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

        // Shared preferences
        SharedPreferences sharedPref = this.getSharedPreferences(
                "Preferencias_Scan", Context.MODE_PRIVATE);

        NUMBER_OF_MEDITIONS = sharedPref.getInt("Numero_Mediciones", 10);
        INTERVAL_MEDITIONS = sharedPref.getInt("Intervalo_Mediciones", 350);

        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE);
        float[] inputFloats = {-1.14105447f, -0.28410435f, -0.58953805f,  0.67933756f};

        inferenceInterface.feed(INPUT_NODE, inputFloats, 1,4);
        inferenceInterface.run(new String[] {OUTPUT_NODE});
        int[] resu = new int[2];
        inferenceInterface.fetch(OUTPUT_NODE,resu);
        Log.e("Resultado", Integer.toString(resu.length));

        daoSession = ((App) getApplication()).getDaoSession();
        beaconsDao = daoSession.getBeaconsDao();
        mProgressDialogScan = new ProgressDialog(this);
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
                List<Fingerprint> fingerprints = fingerprintDao.queryBuilder()
                        .where(FingerprintDao.Properties.XPosition.eq(fingerprint.getXPosition()),
                                FingerprintDao.Properties.YPosition.eq(fingerprint.getYPosition()))
                        .list();
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

        mViewPagerBottom.setOffscreenPageLimit(1);
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

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

    private void stopScanning() {
        //Stop scanning if scanning is in progress
        if (proximityManager.isScanning()) {
            proximityManager.stopScanning();
            Toast.makeText(this, "Escaneo detenido", Toast.LENGTH_SHORT).show();
        }
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
}
