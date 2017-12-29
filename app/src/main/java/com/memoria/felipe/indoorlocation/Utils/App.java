package com.memoria.felipe.indoorlocation.Utils;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;
import com.memoria.felipe.indoorlocation.Utils.Model.DaoMaster;
import com.memoria.felipe.indoorlocation.Utils.Model.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by felip on 18-07-2017.
 */

public class App extends Application {
    /** A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher. */
    public static final boolean ENCRYPTED = true;

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "indoor-db.db");
        //MyDatabase helper = new MyDatabase(this);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
