package com.acistek.cls;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by greed on 5/14/2015.
 * This file is for creating table into SQLite Database.
 */
public class DBhelper extends SQLiteOpenHelper{
    //Table Name
    public static final String TABLE_NAME = "COOP";

    //Table Columns
    public static final String  KEY_CONTACTLISTID = "_id";
    public static final String  KEY_GROUPNAME = "groupname";
    public static final String  KEY_COOPNAME = "coopname";
    public static final String  KEY_CELLPHONE = "cellphone";
    public static final String  KEY_OFFICEPHONE = "officephone";

    // Database Information
    static final String DB_NAME = "CLS_ANDROID.DB";

    // database version
    static final int DB_VERSION = 2;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + KEY_CONTACTLISTID + " TEXT NOT NULL, "+ KEY_GROUPNAME + " TEXT, "+ KEY_COOPNAME + " TEXT, "+ KEY_CELLPHONE + " TEXT, " + KEY_OFFICEPHONE + " TEXT);";

    public DBhelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
