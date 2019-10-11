package com.muhtasim.fuadrafid.smartlens.storagemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Fuad Rafid on 9/4/2017.
 */

public class SQLmanager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="Profiles.db";
    public static final String TABLE_NAME = "profile_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "FIELD";
    public static int entryNo=10;
    public SQLmanager(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query="create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT";
        for(int i=0;i<entryNo;i++)
        {
            query=query+","+COL_2+Integer.toString(i);
        }
        query=query+")";
        Log.e("DBMS",query);
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public boolean insertData(String fields[])
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues  contentValues=new ContentValues();
        for(int i=0;i<fields.length;i++)
        {
            contentValues.put(COL_2+Integer.toString(i),fields[i]);
        }
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;

    }
    public boolean updateData(String id,String fields[])
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues  contentValues=new ContentValues();
        for(int i=0;i<fields.length;i++)
        {
            contentValues.put(COL_2+Integer.toString(i),fields[i]);
        }
        long result = db.update(TABLE_NAME,contentValues,"ID = ?",new String[]{id} );
        if(result < 1)
            return false;
        else
            return true;

    }
    public void deleteData(String id)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_NAME,"ID = ?",new String[]{id});
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }
}
