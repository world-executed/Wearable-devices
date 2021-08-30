package com.zenchn.bletester.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "datainfo.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE SAMPLE (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TEMPREATURE REAL," +
                "HUMIDITY REAL," +
                "HEARTREAT REAL," +
                "SPO2 REAL," +
//                "REDSAMPLE REAL," +
//                "IRSAMPLE REAL," +
                "TIME TEXT);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
