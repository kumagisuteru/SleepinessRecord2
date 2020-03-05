package com.websarva.wings.android.sleepinessrecord2;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.websarva.wings.android.sleepinessrecord2.R;

import java.util.ArrayList;
import java.util.Calendar;

import static com.websarva.wings.android.sleepinessrecord2.GraphSettings.clearChart;


public class ShowGraph extends AppCompatActivity {

    protected static  LineChart mpLineChart;
    protected static  com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    protected static  SQLiteDatabase db;
    protected static  PainData lastweek;
    protected int trend;
    protected double roc;
    protected static  ImageView imgTrend;
    protected   LineDataSet lineDataSet1;
    protected   LineDataSet lineDataSet2;
    protected static  Intent intent;
    protected static  boolean check= false;
    protected ArrayList<ILineDataSet> datasets;
    Switch swPain, swCount;
    GraphSettings     settings;
    Resources res;
    protected int drawableId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);

        // 戻るボタン・グラフ領域のオブジェクト取得
        Button btnSummary = findViewById(R.id.btn_summary);
        btnSummary.setOnClickListener(btnTap);
        Button btnWeekly = findViewById(R.id.btn_weekly);
        btnWeekly.setOnClickListener(btnTap);
        Button btnOneday = findViewById(R.id.btn_oneday);
        btnOneday.setOnClickListener(btnTap);
        swPain = findViewById(R.id.sw_pain);
        swCount = findViewById(R.id.sw_count);

        String subject = "過去1週間のデータ";


        res = getResources();
        int maincolor = res.getColor(R.color.colorMain);
        imgTrend = findViewById(R.id.img_trend);
        imgTrend.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                final AlertDialog.Builder TrendDialog = new AlertDialog.Builder(ShowGraph.this);


                if(swCount.isChecked()) {
                    if (drawableId == R.drawable.increse) {
                        // ダイアログの設定
                        TrendDialog.setTitle(R.string.trend_title);      //タイトル設定
                        TrendDialog.setMessage(R.string.trend_count_increse);  //内容(メッセージ)設定
                        TrendDialog.show();
                    } else if (drawableId == R.drawable.decrese) {
                        TrendDialog.setTitle(R.string.trend_title);      //タイトル設定
                        TrendDialog.setMessage(R.string.trend_count_decrese);  //内容(メッセージ)設定
                        TrendDialog.show();
                    } else {
                        TrendDialog.setTitle(R.string.trend_title);      //タイトル設定
                        TrendDialog.setMessage(R.string.trend_count_stagnant);  //内容(メッセージ)設定
                        TrendDialog.show();
                    }
                }else{
                    if (drawableId == R.drawable.increse) {
                        // ダイアログの設定
                        TrendDialog.setTitle(R.string.trend_title);      //タイトル設定
                        TrendDialog.setMessage(R.string.trend_pain_increse);  //内容(メッセージ)設定
                        TrendDialog.show();
                    } else if (drawableId == R.drawable.decrese) {
                        TrendDialog.setTitle(R.string.trend_title);      //タイトル設定
                        TrendDialog.setMessage(R.string.trend_pain_decrese);  //内容(メッセージ)設定
                        TrendDialog.show();
                    } else {
                        TrendDialog.setTitle(R.string.trend_title);      //タイトル設定
                        TrendDialog.setMessage(R.string.trend_pain_stagnant);  //内容(メッセージ)設定
                        TrendDialog.show();
                    }
                }

            }
        });

        mpLineChart = (LineChart) findViewById(R.id.line_chart);

        if (helper == null) {
            helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        settings = new GraphSettings(mpLineChart, maincolor, subject);

        settings.setXaxisDate(mpLineChart);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        //monthは0~11 目的の月にするには+1する必要あり
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int date = calendar.get(Calendar.DATE);
        check = checkLastWeekEmpty(year, month, date);

        datasets = createGraph(year, month, date, true, true);

        //痛みの強さのスイッチの状態が変化した際のリスナ
        swPain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (swPain.isChecked()) {        //mSwitch : Off -> On の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), swCount.isChecked());
                            createGraph(year, month, date, true, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), !(swCount.isChecked()));
                            createGraph(year, month, date, true, false);
                        }
                    } else {                         //mSwitch : On -> Off の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), swCount.isChecked());
                            createGraph(year, month, date, false, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), !(swCount.isChecked()));
                            createGraph(year, month, date, false, false);
                        }
                    }
            }
        });

        //記録回数のスイッチの状態が変化した際のリスナ
        swCount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (swPain.isChecked()) {        //mSwitch : Off -> On の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), swCount.isChecked());
                            createGraph(year, month, date, true, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), !(swCount.isChecked()));
                            createGraph(year, month, date, true, false);
                        }
                    } else {                         //mSwitch : On -> Off の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), swCount.isChecked());
                            createGraph(year, month, date, false, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), !(swCount.isChecked()));
                            createGraph(year, month, date, false, false);
                        }
                    }
            }
        });
    }

    private View.OnClickListener btnTap = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (helper == null) {
                helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(getApplicationContext());
            }
            if (db == null) {
                db = helper.getWritableDatabase();
            }

            switch (view.getId()) {
                case R.id.btn_summary:
                    intent = new Intent(getApplication(), ShowSummary.class);
                    startActivity(intent);
                    break;
                case R.id.btn_weekly:
                    intent = new Intent(getApplication(), ShowWeekly.class);
                    startActivity(intent);
                    break;
                case R.id.btn_oneday:
                    intent = new Intent(getApplication(), ShowOneday.class);
                    startActivity(intent);
                    break;
            }
        }
    };


    protected boolean checkLastWeekEmpty(int year, int month, int date){
        check= false;
        Cursor cursor = ListHandling.getLastWeekCursor(year,month,date);
        cursor.moveToFirst();
        Log.d("count", String.valueOf(cursor.getCount()));
        if (cursor.getCount() >= 1) {
            check = true;
        }
        return check;
    }

    private ArrayList<ILineDataSet> createGraph(int year, int month, int date, boolean painF, boolean countF){
        ArrayList<ILineDataSet> dataSets= new ArrayList<>();
        LineData data;
        if (check && (painF || countF)) {
            lastweek = ListHandling.getLastWeekList(year, month, date);

            if(countF){
                roc = TrendDefinition.calculateRateOfChange(lastweek.count, lastweek.count.length);
                trend = TrendDefinition.judgeTrend(roc, true);
            }else if(painF){
                roc = TrendDefinition.calculateRateOfChange(lastweek.value, lastweek.value.length);
                trend = TrendDefinition.judgeTrend(roc, true);
            }else{
                trend = TrendDefinition.judgeTrend(0, false);
            }

            drawableId = TrendDefinition.showTrend(trend, imgTrend);

            Log.d("roc", String.valueOf(roc));
            Log.d("trend", String.valueOf(trend));

            if(painF){
                lineDataSet1 = ListHandling.setList(lastweek.date, lastweek.value, false, mpLineChart);
            }
            if(countF){
                lineDataSet2 = ListHandling.setList(lastweek.date, lastweek.count, true, mpLineChart);
            }
            dataSets = new ArrayList<>();
            if(painF) {
                dataSets.add(lineDataSet1);
            }
            if(countF){
                dataSets.add(lineDataSet2);
            }
            datasets = dataSets;
            data = new LineData(dataSets);
            if(painF) {
                settings.setLinesAndPointsDetailsForValue(lineDataSet1, res);
            }
            if(countF){
                settings.setLinesAndPointsDetailsForCount(lineDataSet2, res);
            }
            mpLineChart.setData(data);
            mpLineChart.invalidate();

        } else {
            Toast.makeText(ShowGraph.this, R.string.toast, Toast.LENGTH_SHORT).show();
        }

        return dataSets;
    }

}
