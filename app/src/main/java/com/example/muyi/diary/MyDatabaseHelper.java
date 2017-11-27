package com.example.muyi.diary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by muyi on 17-11-20.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_DIARY = "create table diary ("
            + "id integer primary key autoincrement,"
            + "title text,"
            + "content text,"
            + "time text)";

    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DIARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
