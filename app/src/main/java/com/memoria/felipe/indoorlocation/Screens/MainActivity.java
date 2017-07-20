package com.memoria.felipe.indoorlocation.Screens;

import android.Manifest;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;
import com.kontakt.sdk.android.common.profile.ISecureProfile;
import com.memoria.felipe.indoorlocation.Fragments.OnlineFragment;
import com.memoria.felipe.indoorlocation.Fragments.SettingsFragment;
import com.memoria.felipe.indoorlocation.Utils.CustomBeacon;
import com.memoria.felipe.indoorlocation.Utils.FragmentAdapterIndoor;
import com.memoria.felipe.indoorlocation.Utils.MapBoxOfflineTileProvider;
import com.memoria.felipe.indoorlocation.Fragments.OfflineFragment;
import com.memoria.felipe.indoorlocation.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.laenger.android.vpbs.BottomSheetUtils;
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;
import java8.util.stream.Collectors;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, OfflineFragment.OnFragmentOfflineListener,
        OnlineFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener{

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
    private CustomBeacon mCloseBeacon2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Init kontakt sdk
        KontaktSDK.initialize(this);
        checkPermissions();
        initProximityManager();
        setupBottomSheet();

        mCloseBeacon = new CustomBeacon();
        mCloseBeacon2 = new CustomBeacon();

        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);*/
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
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
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));

        proximityManager.filters().eddystoneFilter(createFilterBeaconPro());
        proximityManager.setEddystoneListener(createEddystoneListener(1));
        proximityManager.setSecureProfileListener(createSecureProfileListener(1));
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
                        mCloseBeacon.setInstanceId(inmediatly.getInstanceId());
                        mCloseBeacon.setMAC(inmediatly.getAddress());
                        mCloseBeacon.setName(inmediatly.getName());
                        mCloseBeacon.setNameSpace(inmediatly.getNamespace());
                        mCloseBeacon.setRssi(inmediatly.getRssi());
                        mCloseBeacon.setTxPower(inmediatly.getTxPower());
                        mCloseBeacon.setUniqueId(inmediatly.getUniqueId());
                    }

                }

            }
        };
    }

    private SecureProfileListener createSecureProfileListener(int mode) {
        return new SimpleSecureProfileListener() {
            @Override
            public void onProfilesUpdated(List<ISecureProfile> profiles) {
                if(mode==1){
                    Log.i(TAG, "onProfileUpdated: " + profiles.size());
                    for(int i= 0; i<profiles.size(); i++){
                        Log.i(TAG, "onProfilesUpdate: " + profiles.get(i).toString());
                    }
                }
                else{
                    Log.i(TAG, "onProfileUpdated: " + profiles.size());
                    for(int i= 0; i<profiles.size(); i++){
                        Log.i(TAG, "onProfilesUpdate: " + profiles.get(i).toString());
                    }

                    ISecureProfile inmediatly =  StreamSupport.stream(profiles)
                            .max((x1,x2)->Integer.compare(x1.getRssi(),x2.getRssi()))
                            .get();

                    if(inmediatly.getRssi()>mCloseBeacon2.getRssi()){
                        mCloseBeacon2.setInstanceId(inmediatly.getInstanceId());
                        mCloseBeacon2.setMAC(inmediatly.getMacAddress());
                        mCloseBeacon2.setName(inmediatly.getName());
                        mCloseBeacon2.setNameSpace(inmediatly.getNamespace());
                        mCloseBeacon2.setRssi(inmediatly.getRssi());
                        mCloseBeacon2.setTxPower(inmediatly.getTxPower());
                        mCloseBeacon2.setUniqueId(inmediatly.getUniqueId());
                    }
                }

            }
        };
    }

    public EddystoneFilter createFilterBeaconPro(){
        return new EddystoneFilter() {
            @Override
            public boolean apply(IEddystoneDevice device) {
                //Do your logic here. For example:
                return device.getUniqueId()!=null;
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

        // Sometime later when the map view is destroyed, close the provider.
        // This is important to prevent a leak of the backing SQLiteDatabase.
        //provider.close();
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(0, 0);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
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
            mMap.setLatLngBoundsForCameraTarget(restrictions);
            mMap.setMinZoomPreference(4);
            mMap.setMaxZoomPreference(5);
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
                proximityManager.setSecureProfileListener(createSecureProfileListener(mode));
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

    public void onServiceReadyBeacon(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("Termine", "Termine");
                stopScanning();
                proximityManager.setEddystoneListener(createEddystoneListener(1));
                proximityManager.setSecureProfileListener(createSecureProfileListener(1));
                OfflineFragment offFragment =  (OfflineFragment)mViewPagerBottom.getAdapter().instantiateItem(mViewPagerBottom,0);
                if(mCloseBeacon.getRssi()>mCloseBeacon2.getRssi()){
                    offFragment.captureNewBeacon(mCloseBeacon);
                }
                else{
                    offFragment.captureNewBeacon(mCloseBeacon2);
                }

                mCloseBeacon.clearFields();
                mCloseBeacon2.clearFields();
            }
        }, 30000);
    }
}
