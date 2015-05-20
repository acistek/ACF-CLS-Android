package com.acistek.cls;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

/**
 * Created by greed on 5/14/2015.
 * This file is performing all database related operations like add, update, delete records into table.
 */
public class SQLController {
    private DBhelper dbHelper;
    private Context ourcontext;
    private SQLiteDatabase database;

    public SQLController(Context c){
        ourcontext = c;
    }

    public SQLController open() throws SQLException{
        dbHelper = new DBhelper(ourcontext);
        //open the database
        database = dbHelper.getWritableDatabase();
        return this;
    }

    //adding new coop record
    public void insertCOOP(String contactlistid, String groupname, String coopname, String cellphone, String officephone){
        ContentValues coopContentValue = new ContentValues();
        coopContentValue.put(DBhelper.KEY_CONTACTLISTID, contactlistid);
        coopContentValue.put(DBhelper.KEY_GROUPNAME, groupname);
        coopContentValue.put(DBhelper.KEY_COOPNAME, coopname);
        coopContentValue.put(DBhelper.KEY_CELLPHONE, cellphone);
        coopContentValue.put(DBhelper.KEY_OFFICEPHONE, officephone);
        database.insert(DBhelper.TABLE_NAME, null, coopContentValue);
    }

    //fetching all coop records
    public Cursor fetchCOOP(){
        String[] coopColumns = new String[] {DBhelper.KEY_CONTACTLISTID, DBhelper.KEY_GROUPNAME, DBhelper.KEY_COOPNAME, DBhelper.KEY_CELLPHONE, DBhelper.KEY_OFFICEPHONE};
        Cursor cursor = database.query(DBhelper.TABLE_NAME, coopColumns, null, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        database.close();
        return cursor;
    }

    //delete coop record
    public void deleteCOOP(){
        database.delete(DBhelper.TABLE_NAME, null, null);
    }

    //close the database
    public void closeCOOP() {
        database.close();
    }
}
