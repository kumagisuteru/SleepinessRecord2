package com.websarva.wings.android.sleepinessrecord2;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.websarva.wings.android.sleepinessrecord2.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowCurrentEnv extends AppCompatActivity {

    //変数宣言
    private TextView txtDate, txtTemp, txtHum, txtPres;
    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    private SQLiteDatabase db;
    private String category;
    private String  delId;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_current_env);

        //レイアウト取得
        Button btnList = findViewById(R.id.btn_envlist);

        txtDate = findViewById(R.id.txt_date);
        txtTemp = findViewById(R.id.txt_temp);
        txtHum = findViewById(R.id.txt_hum);
        txtPres = findViewById(R.id.txt_pres);

        //データベース取得
        helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(this);
        db = helper.getReadableDatabase();
        readData();


        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(getApplication(), ShowEnvList.class);
                startActivity(intent);
            }
        });
    }



    //データベースを読み込み文字列変換し表示する関数
    public void readData(){

        Log.d("debug","**********Cursor");

        //データの参照はカーソルCursorを用いる

        //query(テーブル名, 取得するレコード, WHERE句, WHERE句の指定の値,
        // GROUP BY句 同じ値を持つデータでグループ化,
        // HAVING句 WHERE句のグループ版, ORDER BY句 並び順)

        //引数のカテゴリ名を基にデータベース内のデータを抽出する
        Cursor cursor = db.query(
                "sleepinessdb",
                new String[] {"year", "month", "date", "time",
                        "temperature","humidity","pressure"},
                "temperature != ?",
                new String[]{""},
                //new String[]{"Satisfaction"},
                null,
                null,
                null
        );
        if(cursor.getCount()<1){
            txtDate.setText("");
            txtTemp.setText("");
            txtHum.setText("");
            txtPres.setText("");
        }else{

            cursor.moveToLast();
            StringBuilder sbDate = new StringBuilder();
            sbDate.append(cursor.getInt(0));
            sbDate.append("/");
            sbDate.append(cursor.getInt(1));
            sbDate.append("/");
            sbDate.append(cursor.getInt(2));
            sbDate.append(" ");
            sbDate.append(cursor.getString(3));
            String strTemp = cursor.getString(4).substring(0,4);
            String strHum = cursor.getString(5).substring(0,4);
            String strPres = cursor.getString(6).substring(0,6);
            // 忘れずに！
            cursor.close();

            txtDate.setText(sbDate);
            txtTemp.setText(strTemp);
            txtHum.setText(strHum);
            txtPres.setText(strPres);
        }
        }


}

