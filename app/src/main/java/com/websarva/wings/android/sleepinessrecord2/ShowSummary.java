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

import static com.websarva.wings.android.sleepinessrecord2.GraphSettings.clearChart;


public class ShowSummary extends AppCompatActivity {
    private LineChart mpLineChart;
    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    SQLiteDatabase db;
    PainData alldata;

    int trend;
    double roc;
    ImageView imgTrend;
    private boolean check= false;
    Resources res;
    LineDataSet lineDataSet1;
    LineDataSet lineDataSet2;
    protected ArrayList<ILineDataSet> datasets;
    Switch swPain, swCount;
    GraphSettings settings;
    private Intent intent;
    protected int drawableId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_summary);

        res = getResources();
        int maincolor = res.getColor(R.color.colorMain);
        swPain = findViewById(R.id.sw_pain);
        swCount = findViewById(R.id.sw_count);
        imgTrend = findViewById(R.id.img_trend);
        imgTrend.setOnClickListener(new View.OnClickListener() {

            String imgName = String.valueOf(imgTrend.getTag());

            @Override
            public void onClick(View v) {
                final AlertDialog.Builder TrendDialog = new AlertDialog.Builder(ShowSummary.this);

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

        mpLineChart =(LineChart)findViewById(R.id.line_chart);

        String subject = "全てのデータ";

        if (helper == null) {
            helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }


        settings= new GraphSettings(mpLineChart, maincolor, subject);

        settings.setXaxisDate(mpLineChart);


        if (helper == null) {
            helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        ArrayList<ILineDataSet> dataSets;
        LineData data;

        settings.setXaxisDate(mpLineChart);

        check = checkAllEmpty();


        datasets = createGraph(true, true);

        //痛みの強さのスイッチの状態が変化した際のリスナ
        swPain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (swPain.isChecked()) {        //mSwitch : Off -> On の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), swCount.isChecked());
                            createGraph(true, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), !(swCount.isChecked()));
                            createGraph(true, false);
                        }
                    } else {                         //mSwitch : On -> Off の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), swCount.isChecked());
                            createGraph(false, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), !(swCount.isChecked()));
                            createGraph(false, false);
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
                            createGraph(true, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), !(swCount.isChecked()));
                            createGraph(true, false);
                        }
                    } else {                         //mSwitch : On -> Off の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), swCount.isChecked());
                            createGraph(false, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), !(swCount.isChecked()));
                            createGraph(false, false);
                        }
                    }
            }
        });

    }



    private  boolean checkAllEmpty(){
        check= false;
        Cursor cursor = ListHandling.getAllCursor();
        Log.d("count", String.valueOf(cursor.getCount()));
        if (cursor.getCount() >= 1) {
            check = true;
        }
        return check;
    }

    private void reload() {
        intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private ArrayList<ILineDataSet> createGraph(boolean painF, boolean countF){
        ArrayList<ILineDataSet> dataSets= new ArrayList<>();
        LineData data;
        if (check && (painF || countF)) {
            alldata = ListHandling.getAllList();

            if(countF){
                roc = TrendDefinition.calculateRateOfChange(alldata.count, alldata.count.length);
                Log.d("roc---", String.valueOf(roc));
                trend = TrendDefinition.judgeTrend(roc, true);
            }else if(painF){
                roc = TrendDefinition.calculateRateOfChange(alldata.value, alldata.value.length);
                Log.d("roc---", String.valueOf(roc));
                trend = TrendDefinition.judgeTrend(roc, true);
            }else{
                trend = TrendDefinition.judgeTrend(0, false);
            }

            drawableId = TrendDefinition.showTrend(trend, imgTrend);

            if(painF){
                lineDataSet1 = ListHandling.setList(alldata.date, alldata.value, false, mpLineChart);
            }
            if(countF){
                lineDataSet2 = ListHandling.setList(alldata.date, alldata.count, true, mpLineChart);
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

        }else{
            Toast.makeText(ShowSummary.this,R.string.toast, Toast.LENGTH_SHORT).show();
        }
        return dataSets;
    }

}

