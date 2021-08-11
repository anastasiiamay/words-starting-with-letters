package ru.stacymay.wordsstartingwithlettergame;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBFriendsHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "friendsDb";
    public static final String TABLE_FRIENDS = "friends";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHOTO_URL = "photoUrl";

    public DBFriendsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_FRIENDS + "(" + KEY_ID +" text primary key,"
                + KEY_NAME + " text,"
                + KEY_PHOTO_URL + " text"
                +")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+ TABLE_FRIENDS);
        onCreate(db);
    }

    public Cursor viewData(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select * from "+TABLE_FRIENDS;
        return db.rawQuery(query,null);
    }
}

