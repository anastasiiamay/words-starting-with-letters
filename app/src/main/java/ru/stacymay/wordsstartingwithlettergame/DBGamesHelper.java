package ru.stacymay.wordsstartingwithlettergame;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBGamesHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "gamesDb";
    public static final String TABLE_GAMES = "games";

    public static final String KEY_ID = "_id";
    public static final String KEY_GAME_ID = "gameId";
    public static final String KEY_ORG_NAME = "orgName";
    public static final String KEY_ORG_PHOTO_URL = "orgPhotoUrl";
    public static final String KEY_PLAYED = "played";

    public DBGamesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_GAMES + "(" + KEY_ID +" integer primary key AUTOINCREMENT,"
                + KEY_GAME_ID + " text,"
                + KEY_ORG_NAME + " text,"
                + KEY_ORG_PHOTO_URL + " text,"
                + KEY_PLAYED + " text"
                +")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+ TABLE_GAMES);
        onCreate(db);
    }

    public Cursor viewData(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select * from "+TABLE_GAMES;
        return db.rawQuery(query,null);
    }
}

