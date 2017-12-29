package com.memoria.felipe.indoorlocation.Utils;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by felip on 23-12-2017.
 */

public class MyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "indoor-db.db";
    private static final int DATABASE_VERSION = 14;

    public MyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
