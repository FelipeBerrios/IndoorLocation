package com.memoria.felipe.indoorlocation;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import biz.laenger.android.vpbs.BottomSheetUtils;
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OfflineFragment.OnFragmentInteractionListener {

    private LinearLayout mLinearBotomSheet;
    private ViewPagerBottomSheetBehavior mBottomBehavior;
    //private Toolbar mToolbarBottom;
    private TabLayout mTabLayoutBottom;
    private ViewPager mViewPagerBottom;
    int heightDiff;
    final int MIN_PEAK_HEIGHT = 56;
    private GoogleMap mMap;
    // Where is the actual map used
    private LatLngBounds restrictions = new LatLngBounds(
            new LatLng(-1, -1), new LatLng(37, 145.75));
    private final static  String MAP_OVERLAY_FILENAME = "parking_origin_0-145_HD.mbtiles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLinearBotomSheet = (LinearLayout)findViewById(R.id.linear_botom_sheet);
        mBottomBehavior = ViewPagerBottomSheetBehavior.from(mLinearBotomSheet);
        mTabLayoutBottom = (TabLayout) findViewById(R.id.bottom_sheet_tabs);
        mViewPagerBottom = (ViewPager)findViewById(R.id.bottom_sheet_viewpager);
        //mToolbarBottom = (Toolbar)findViewById(R.id.bottom_sheet_toolbar);

        setupBottomSheet();

        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);*/
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    private void setupBottomSheet() {
        mViewPagerBottom.setOffscreenPageLimit(1);
        mViewPagerBottom.setAdapter(new FragmentAdapterIndoor(getSupportFragmentManager(),
                MainActivity.this));
        mTabLayoutBottom.setupWithViewPager(mViewPagerBottom);
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
        LatLng sydney = new LatLng(36, 144.75);
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
}
