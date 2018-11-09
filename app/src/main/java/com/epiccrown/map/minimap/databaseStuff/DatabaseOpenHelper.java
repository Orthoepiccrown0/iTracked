package com.epiccrown.map.minimap.databaseStuff;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private final static int DB_VERSION = 1;
    private final static String DB_NAME = "iTracked.db";


    public DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ DatabaseScheme.FavsTable.NAME+"("+
                "_id integer primary key autoincrement, "+
                DatabaseScheme.FavsTable.Cols.Username+")"
        );

        db.execSQL("create table "+ DatabaseScheme.HistoryTable.NAME+"("+
                "_id integer primary key autoincrement, "+
                DatabaseScheme.HistoryTable.Cols.Username+")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
