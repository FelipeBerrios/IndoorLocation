package com.memoria.felipe.indoorlocation.Utils;

/**
 * Created by felip on 04-07-2017.
 */


import android.app.Application;
import com.kontakt.sdk.android.common.KontaktSDK;

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        initializeDependencies();
    }

    //Initializing Kontakt SDK. Insert your API key to allow all samples to work correctly
    private void initializeDependencies() {
        KontaktSDK.initialize(this);
    }
}