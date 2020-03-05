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

public class ShowEnvList extends AppCompatActivity {

    //変数宣言
    private ListView listView;
    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    private SQLiteDatabase db;
    private String category;
    private String  delId;
    private Intent intent;
    SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

        //レイアウト取得

        listView = findViewById(R.id.list_view);

        //データベース取得
        helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(this);
        db = helper.getReadableDatabase();

        //画面遷移時に表示するために、onCreate内でデータベースの表示を行う
        readData();



    }

    private View.OnClickListener btnTap = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            //ボタン押下時の処理
            switch (view.getId()) {
                //戻るボタン
                case R.id.btn_return:
                    finish();
                    break;

            }
        }
    };

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

        //カーソルを一番上に戻す
        cursor.moveToFirst();

        //StringBuilder sbuilder = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        //ArrayList data = new ArrayList<>();
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        //抽出したデータのも実を表示するための変数に格納
        //表示形式↓↓↓
        // ID: 年/日/月 時:分:秒: カテゴリ名: 値
        for (int i = 0; i < cursor.getCount(); i++) {
            Map<String, String> item = new HashMap<String, String>();
            sb1.append("気温");
            sb1.append(": ");
            sb1.append(cursor.getString(4).substring(0,4));
            sb1.append("[℃]");
            sb1.append("\n");
            sb1.append("湿度");
            sb1.append(": ");
            sb1.append(cursor.getString(5).substring(0,4));
            sb1.append("[%]");
            sb1.append("\n");
            sb1.append("気圧");
            sb1.append(": ");
            sb1.append(cursor.getString(6).substring(0,6));
            sb1.append("[hPa]");

            sb2.append(cursor.getInt(0)) ;
            sb2.append("/");
            sb2.append(cursor.getInt(1));
            sb2.append("/");
            sb2.append(cursor.getInt(2));
            sb2.append(" ");
            sb2.append(cursor.getString(3));
            item.put("Subject", sb1.toString());
            item.put("Comment", sb2.toString());
            data.add(item);
            sb1.delete(0, sb1.length());
            sb2.delete(0, sb2.length());
            cursor.moveToNext();
        }

        // 忘れずに！
        cursor.close();

        /*
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        Log.d("debug","**********"+sbuilder.toString());
        */
        // リスト項目とListViewを対応付けるArrayAdapterを用意する
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] { "Subject", "Comment" },
                new int[] { android.R.id.text1, android.R.id.text2});

        //リストビューに文字列代入
        listView.setAdapter(adapter);
        //listView.setText(sbuilder.toString());
    }





    //画面を再読み込みする関数
    private void reload() {
        intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}
