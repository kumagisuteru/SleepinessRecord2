package com.websarva.wings.android.sleepinessrecord2;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.websarva.wings.android.sleepinessrecord2.R;

import java.util.ArrayList;

import static com.websarva.wings.android.sleepinessrecord2.GraphSettings.clearChart;


public class ShowWeekly extends AppCompatActivity {
    private LineChart mpLineChart;
    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    SQLiteDatabase db;
    PainData oneweek;

    int trend;
    double roc;
    ImageView imgTrend;
    private boolean check = false;

    LineDataSet lineDataSet1;
    LineDataSet lineDataSet2;

    GraphSettings settings;
    private Intent intent;
    private Spinner spYear;
    private Spinner spMonth;
    private Spinner spWeek;
    Resources res;
    protected static int iYear;
    protected static int iMonth;
    protected static int iWeek;
    protected ArrayList<ILineDataSet> datasets;
    Switch swPain, swCount;
    protected int drawableId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_weekly);

        res = getResources();
        int maincolor = res.getColor(R.color.colorMain);

        swPain = findViewById(R.id.sw_pain);
        swCount = findViewById(R.id.sw_count);

        imgTrend = findViewById(R.id.img_trend);
        imgTrend.setOnClickListener(new View.OnClickListener() {

            String imgName = String.valueOf(imgTrend.getTag());

            @Override
            public void onClick(View v) {
                final AlertDialog.Builder TrendDialog =new AlertDialog.Builder(ShowWeekly.this);

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
        Button btnShow = findViewById(R.id.btn_show);
        btnShow.setOnClickListener(btnTap);

        String subject = "指定した1週間のデータ";

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


        //痛みの強さのスイッチの状態が変化した際のリスナ
        swPain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Spinner spYear = findViewById(R.id.sp_year);
                    String strYear = (String) spYear.getSelectedItem();
                    int iYear = Integer.parseInt(strYear);
                    Spinner spMonth = findViewById(R.id.sp_month);
                    String strMonth = (String) spMonth.getSelectedItem();
                    int iMonth = Integer.parseInt(strMonth);
                    Spinner spWeek = findViewById(R.id.sp_week);
                    String strDate = (String) spWeek.getSelectedItem();
                    int iWeek = Integer.parseInt(strDate.substring(0, 1));

                    if (swPain.isChecked()) {        //mSwitch : Off -> On の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), swCount.isChecked());
                            createGraph(iYear, iMonth, iWeek, true, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), !(swCount.isChecked()));
                            createGraph(iYear, iMonth, iWeek, true, false);
                        }
                    } else {                         //mSwitch : On -> Off の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), swCount.isChecked());
                            createGraph(iYear, iMonth, iWeek, false, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), !(swCount.isChecked()));
                            createGraph(iYear, iMonth, iWeek, false, false);
                        }
                    }
            }

        });

        //記録回数のスイッチの状態が変化した際のリスナ
        swCount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Spinner spYear = findViewById(R.id.sp_year);
                    String strYear = (String) spYear.getSelectedItem();
                    int iYear = Integer.parseInt(strYear);
                    Spinner spMonth = findViewById(R.id.sp_month);
                    String strMonth = (String) spMonth.getSelectedItem();
                    int iMonth = Integer.parseInt(strMonth);
                    Spinner spWeek = findViewById(R.id.sp_week);
                    String strDate = (String) spWeek.getSelectedItem();
                    int iWeek = Integer.parseInt(strDate.substring(0, 1));
                    if (swPain.isChecked()) {        //mSwitch : Off -> On の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), swCount.isChecked());
                            createGraph(iYear, iMonth, iWeek, true, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, swPain.isChecked(), !(swCount.isChecked()));
                            createGraph(iYear, iMonth, iWeek, true, false);
                        }
                    } else {                         //mSwitch : On -> Off の時の処理
                        if (swCount.isChecked()) {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), swCount.isChecked());
                            createGraph(iYear, iMonth, iWeek, false, true);
                        } else {
                            clearChart(datasets, mpLineChart);
                            //createGraph(year, month, date, !(swPain.isChecked()), !(swCount.isChecked()));
                            createGraph(iYear, iMonth, iWeek, false, false);
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

            ArrayList<ILineDataSet> dataSets;
            LineData data;
            switch (view.getId()) {
                case R.id.btn_show:
                    settings.setXaxisDate(mpLineChart);
                    spYear = findViewById(R.id.sp_year);
                    String strYear = (String) spYear.getSelectedItem();
                    iYear = Integer.parseInt(strYear);
                    Log.d("year", String.valueOf(iYear));
                    spMonth = findViewById(R.id.sp_month);
                    String strMonth = (String) spMonth.getSelectedItem();
                    iMonth = Integer.parseInt(strMonth);
                    Log.d("month", String.valueOf(iMonth));
                    spWeek = findViewById(R.id.sp_week);
                    String strDate = (String) spWeek.getSelectedItem();
                    iWeek = Integer.parseInt(strDate.substring(0, 1));
                    Log.d("date", String.valueOf(iWeek));
                    check = checkOneWeekEmpty(iYear, iMonth, iWeek);
                    datasets = createGraph(iYear, iMonth, iWeek, true, true);
            }
        }
    };

    protected boolean checkOneWeekEmpty(int year, int month, int week) {
        check= false;
        Cursor cursor = ListHandling.getOneWeekCursor(year, month, week);
        Log.d("count", String.valueOf(cursor.getCount()));
        if (cursor.getCount() >= 1) {
            check = true;
        }
        return check;
    }

    private ArrayList<ILineDataSet> createGraph(int year, int month, int week, boolean painF, boolean countF) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        LineData data;
        if (check && (painF || countF)) {
            oneweek = ListHandling.getOneWeekList(year, month, week);

            if (countF) {
                roc = TrendDefinition.calculateRateOfChange(oneweek.count, oneweek.count.length);
                trend = TrendDefinition.judgeTrend(roc, true);
            } else if (painF) {
                roc = TrendDefinition.calculateRateOfChange(oneweek.value, oneweek.value.length);
                trend = TrendDefinition.judgeTrend(roc, true);
            } else {
                trend = TrendDefinition.judgeTrend(0, false);
            }

            drawableId = TrendDefinition.showTrend(trend, imgTrend);

            if (painF) {
                lineDataSet1 = ListHandling.setList(oneweek.date, oneweek.value, false, mpLineChart);
            }
            if (countF) {
                lineDataSet2 = ListHandling.setList(oneweek.date, oneweek.count, true, mpLineChart);
            }
            dataSets = new ArrayList<>();
            if (painF) {
                dataSets.add(lineDataSet1);
            }
            if (countF) {
                dataSets.add(lineDataSet2);
            }
            datasets = dataSets;
            data = new LineData(dataSets);
            if (painF) {
                settings.setLinesAndPointsDetailsForValue(lineDataSet1, res);
            }
            if (countF) {
                settings.setLinesAndPointsDetailsForCount(lineDataSet2, res);
            }
            mpLineChart.setData(data);
            mpLineChart.invalidate();

        } else {
            Toast.makeText(ShowWeekly.this, R.string.toast, Toast.LENGTH_SHORT).show();
        }

        return dataSets;
    }
}