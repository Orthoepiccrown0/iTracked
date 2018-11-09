package com.epiccrown.map.minimap.databaseStuff;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.epiccrown.map.minimap.Preferences;

import java.util.ArrayList;
import java.util.Collections;

public class DatabaseDataGetter {
    private DatabaseOpenHelper dbhelper;
    private Context context;

    public DatabaseDataGetter(DatabaseOpenHelper dbhelper, Context context) {
        this.dbhelper = dbhelper;
        this.context = context;
    }

    public DatabaseDataGetter(DatabaseOpenHelper dbhelper) {
        this.dbhelper = dbhelper;
    }

    public ArrayList<String> getFavs() {
        ArrayList<String> favs = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(DatabaseScheme.FavsTable.NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        while (cursor.moveToNext()) {
            String username = cursor.getString(1);
            favs.add(username);
        }

        sqLiteDatabase.close();
        return favs;
    }

    public ArrayList<String> getHistory() {
        ArrayList<String> history = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(DatabaseScheme.HistoryTable.NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            history = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                String username = cursor.getString(1);
                history.add(username);
                cursor.moveToNext();
            }
        }
        sqLiteDatabase.close();
        return history;
    }

    public void insertFav(String username) {
        SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseScheme.FavsTable.Cols.Username, username);

        sqLiteDatabase.insert(DatabaseScheme.FavsTable.NAME, null, contentValues);
        sqLiteDatabase.close();
    }

    public void insertNewHistoryMember(String username) {
        ArrayList<String> users = getHistory();
        if (users.size() < Preferences.numberOfSaves(context)) {
            SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseScheme.HistoryTable.Cols.Username, username);
            sqLiteDatabase.insert(DatabaseScheme.HistoryTable.NAME, null, contentValues);
            sqLiteDatabase.close();
        }else{
            int index = users.size();
            deleteHistoryMember(users.get(index-1));

            SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseScheme.HistoryTable.Cols.Username, username);
            sqLiteDatabase.insert(DatabaseScheme.HistoryTable.NAME, null, contentValues);
            sqLiteDatabase.close();
        }
    }

    public void deleteIfPresent(String username){
        ArrayList<String> users = getHistory();
        for(String user : users){
            if(user.equals(username))
                deleteHistoryMember(username);
        }
    }

    public void deleteFav(String username) {
        SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();
        String selection = DatabaseScheme.FavsTable.Cols.Username + " = ?";
        String[] selectionArgs = new String[]{username};

        sqLiteDatabase.delete(DatabaseScheme.FavsTable.NAME, selection, selectionArgs);
        sqLiteDatabase.close();
    }

    public void deleteHistoryMember(String username) {
        SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();
        String selection = DatabaseScheme.HistoryTable.Cols.Username + " = ?";
        String[] selectionArgs = new String[]{username};
        sqLiteDatabase.delete(DatabaseScheme.HistoryTable.NAME, selection, selectionArgs);

    }

    public void deleteHistory() {
        SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();

        sqLiteDatabase.delete(DatabaseScheme.HistoryTable.NAME, null, null);
        sqLiteDatabase.close();
    }

    public void deleteFavs(){
        SQLiteDatabase sqLiteDatabase = dbhelper.getReadableDatabase();

        sqLiteDatabase.delete(DatabaseScheme.FavsTable.NAME, null, null);
        sqLiteDatabase.close();
    }

    public void deleteExcessHistory(Context context){
        ArrayList<String> users = getHistory();
        int usersSize = users.size();
        int prefSize = Preferences.numberOfSaves(context);
        if(usersSize>prefSize){
            int diff = usersSize-prefSize;
            for(int i=0;i<diff;i++)
                deleteHistoryMember(users.get(i));
        }

    }
}
