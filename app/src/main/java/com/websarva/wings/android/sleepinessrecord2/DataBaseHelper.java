package com.websarva.wings.android.sleepinessrecord2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//データベースの変更を行いたいときに編集

public class DataBaseHelper extends SQLiteOpenHelper {

    /*
    データベースファイル名の定数フィールド
     */

    private static final String DATABASE_NAME = "SleepinessRecord.db";
    private static final String TABLE_NAME = "sleepinessdb";

    private static final String _ID = "_id";
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DATE = "date";
    private static final String TIME = "time";

    private static final String VALUE = "value";
    private static final String LOUD = "loud";
    private static final String HIGH = "high";
    private static final String LOW = "low";
    private static final String GLARE = "glare";

    private static final String OTHERS = "others";
    private static final String BEHAVIOR = "behavior";
    private static final String SLEEP = "sleep";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";

    private static final String TEMPERATURE = "temperature";
    private static final String HUMIDITY = "humidity";
    private static final String PRESSURE = "pressure";

    /*
    バージョン情報の定数フィールド
     */

    private static final int DATABASE_VERSION = 3;

    /*
    コンストラクタ
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        db.execSQL(
                SQL_CREATE_ENTRIES
        );

        Log.d("debug", "onCreate(SQLiteDatabase db)");

    }
    public DataBaseHelper(Context context){
        //親クラスのコンストラクタの呼び出し
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +

                    _ID + " INTEGER PRIMARY KEY," +
                    YEAR + " INTEGER, " +
                    MONTH + " INTEGER, " +
                    DATE + " INTEGER, " +
                    TIME + " TEXT, " +

                    VALUE + " INTEGER, " +
                    LOUD + " INTEGER, " +
                    HIGH + " INTEGER, " +
                    LOW + " INTEGER, " +
                    GLARE + " INTEGER, " +

                    OTHERS + " TEXT, " +
                    BEHAVIOR + " INTEGER, " +
                    SLEEP + " INTEGER, " +
                    LONGITUDE + " INTEGER, " +
                    LATITUDE + " INTEGER, " +

                    TEMPERATURE + " TEXT, " +
                    HUMIDITY + " TEXT, " +
                    PRESSURE + " TEXT )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // アップデートの判別
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}