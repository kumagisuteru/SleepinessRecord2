package com.websarva.wings.android.sleepinessrecord2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.websarva.wings.android.sleepinessrecord2.R;

import java.util.Calendar;


public class AdditionalInfo extends AppCompatActivity {

    CheckBox cbLoud;
    CheckBox cbHigh;
    CheckBox cbLow;
    CheckBox cbGlare;
    EditText etOthers;
    Button button, button1, button2, button3, button4, button5;

    RadioGroup radioGroup;

    Spinner spSleep;

    int loud=0;
    int high=0;
    int low=0;
    int glare=0;
    String others;
    Intent intent;
    int value;
    int behavior;
    int sleep;
    int lat;
    int lng;

    private Calendar calendar;

    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    private SQLiteDatabase db;
    private int iYear;
    private int iMonth;
    private int iDate;
    private int iValue;
    private String strTime;

    Location gps;

    AlertDialog.Builder help1, help2, help3, help4, help5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_info);

        cbLoud = findViewById(R.id.checkbox_loud);
        cbHigh = findViewById(R.id.checkbox_high);
        cbLow = findViewById(R.id.checkbox_low);
        cbGlare = findViewById(R.id.checkbox_glare);
        etOthers = findViewById(R.id.edittext_others);

        button = (Button) findViewById(R.id.btn_decision);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button.setOnClickListener(btnTap);
        button1.setOnClickListener(btnTap);
        button2.setOnClickListener(btnTap);
        button3.setOnClickListener(btnTap);
        button4.setOnClickListener(btnTap);
        button5.setOnClickListener(btnTap);

        radioGroup = (RadioGroup)findViewById(R.id.RadioGroup);
        //radioGroup.setOnCheckedChangeListener();

        spSleep =(Spinner) findViewById(R.id.spin_sleep);

        intent = getIntent();
        value = intent.getIntExtra("score", 0);

        //データベース取得
        helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(this);
        db = helper.getReadableDatabase();

        help1 =new AlertDialog.Builder(this);
        // ダイアログの設定
        help1.setTitle(R.string.dialog_title_help1);      //タイトル設定
        help1.setMessage(R.string.dialog_message_help1);  //内容(メッセージ)設定

        help2 =new AlertDialog.Builder(this);
        // ダイアログの設定
        help2.setTitle(R.string.dialog_title_help2);      //タイトル設定
        help2.setMessage(R.string.dialog_message_help2);  //内容(メッセージ)設定

        help3 =new AlertDialog.Builder(this);
        // ダイアログの設定
        help3.setTitle(R.string.dialog_title_help3);      //タイトル設定
        help3.setMessage(R.string.dialog_message_help3);  //内容(メッセージ)設定

        help4 =new AlertDialog.Builder(this);
        // ダイアログの設定
        help4.setTitle(R.string.dialog_title_help4);      //タイトル設定
        help4.setMessage(R.string.dialog_message_help4);  //内容(メッセージ)設定

        help5 =new AlertDialog.Builder(this);
        // ダイアログの設定
        help5.setTitle(R.string.dialog_title_help5);      //タイトル設定
        help5.setMessage(R.string.dialog_message_help5);  //内容(メッセージ)設定
    }

    private View.OnClickListener btnTap = new View.OnClickListener() {

        // ボタンAクリック時に呼ばれるメソッド
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.btn_decision:
                    if(cbLoud.isChecked()){
                        loud = 1;
                    }else{
                        loud=0;
                    }
                    if(cbHigh.isChecked()){
                        high = 1;
                    }else{
                        high=0;
                    }
                    if(cbLow.isChecked()){
                        low = 1;
                    }else{
                        low=0;
                    }
                    if(cbGlare.isChecked()){
                        glare = 1;
                    }else{
                        glare=0;
                    }
                    if(etOthers.getText().toString().equals("")){
                        others = "";
                    }else{
                        others = etOthers.getText().toString();
                    }

                    String strSleep = (String) spSleep.getSelectedItem();
                    String[] slp = strSleep.split("時間", 2);
                    if(slp[1].equals("")){

                        sleep = Integer.parseInt(slp[0].substring(0,1));
                    }else if(slp[1].equals("未満")){
                        sleep = 2;
                    }else{
                        sleep = 10;
                    }

                    calendar = Calendar.getInstance();
                    iYear = calendar.get(Calendar.YEAR);
                    iMonth = calendar.get(Calendar.MONTH)+1;
                    iDate = calendar.get(Calendar.DATE);
                    int iHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int iMinute = calendar.get(Calendar.MINUTE);
                    int iSecond = calendar.get(Calendar.SECOND);
                    //string型に変換
                    strTime = iHour +":"+iMinute +":"+iSecond;

                    gps = new LocationService().getLocation();

                    if(gps == null){
                        lat=0;
                        lng=0;
                    }else{
                        lat = (int)(gps.getLatitude()*100);
                        lng = (int)(gps.getLongitude()*100);
                    }
                    Log.d("lat ",String.valueOf(lat) );
                    Log.d("lng ",String.valueOf(lng) );

                    int checkedId = radioGroup.getCheckedRadioButtonId();
                    if (-1 != checkedId) {
                        if(((RadioButton)findViewById(checkedId)).getText().equals(getResources().getString(R.string.radiobtn_sitting))){
                            behavior = 1;
                        }else if(((RadioButton)findViewById(checkedId)).getText().equals(getResources().getString(R.string.radiobtn_standing))){
                            behavior = 2;
                        }else if(((RadioButton)findViewById(checkedId)).getText().equals(getResources().getString(R.string.radiobtn_lightexercise))) {
                            behavior = 3;
                        }else if(((RadioButton)findViewById(checkedId)).getText().equals(getResources().getString(R.string.radiobtn_middleexercise))) {
                            behavior = 4;
                        }else {
                            behavior = 5;
                        }

                    } else {
                        behavior=-1;

                    }

                    insertData(db, iYear, iMonth, iDate, strTime, value, loud, high, low, glare, others, behavior, sleep, lng,lat, "","","");
                    Log.d("time", strTime);
                    finish();
                    break;
                case R.id.button1:
                    help1.show();
                    break;
                case R.id.button2:
                    help2.show();
                    break;
                case R.id.button3:
                    help3.show();
                    break;
                case R.id.button4:
                    help4.show();
                    break;
                case R.id.button5:
                    help5.show();
                    break;
            }

        }

    };

    //データベースに記録する関数
    //引数は データベース,日付文字列,カテゴリ,数値
    public void insertData(SQLiteDatabase db, int year, int month, int date, String time, int value,
                           int loud, int high, int low, int glare, String others, int behavior, int sleep, int lng, int lat
            ,String temperature, String humidity, String pressure) {

        ContentValues values = new ContentValues();

        values.put("year", year);
        values.put("month", month);
        values.put("date", date);
        values.put("time", time);

        values.put("value", value);
        values.put("loud", loud);
        values.put("high", high);
        values.put("low", low);
        values.put("glare", glare);

        values.put("others", others);
        values.put("behavior", behavior);
        values.put("sleep", sleep);
        values.put("longitude", lng);
        values.put("latitude", lat);

        values.put("temperature", temperature);
        values.put("humidity", humidity);
        values.put("pressure", pressure);


        db.insert("sleepinessdb", null, values);
    }
}
