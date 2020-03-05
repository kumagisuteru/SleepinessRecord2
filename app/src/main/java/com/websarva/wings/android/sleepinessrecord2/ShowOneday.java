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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.websarva.wings.android.sleepinessrecord2.R;

import java.util.ArrayList;

public class ShowOneday extends AppCompatActivity {

    private LineChart mpLineChart;
    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    SQLiteDatabase db;
    PainData oneday;

    int trend;
    double roc;
    ImageView imgTrend;
    private boolean check = false;

    LineDataSet lineDataSet1;

    GraphSettings settings;
    private Intent intent;
    private Spinner spYear;
    private Spinner spMonth;
    private Spinner spDate;
    Resources res;
    protected static int iYear;
    protected static int iMonth;
    protected static int iDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_oneday);

        res = getResources();
        int maincolor = res.getColor(R.color.colorMain);
        imgTrend = findViewById(R.id.img_trend);
        imgTrend.setOnClickListener(new View.OnClickListener() {

            String imgName = String.valueOf(imgTrend.getTag());

            @Override
            public void onClick(View v) {
                final AlertDialog.Builder TrendDialog =new AlertDialog.Builder(ShowOneday.this);

                if(imgName.equals("increse.png")){
                    // ダイアログの設定
                    TrendDialog.setTitle(R.string.dialog_title);      //タイトル設定
                    TrendDialog.setMessage(R.string.dialog_msg);  //内容(メッセージ)設定
                    TrendDialog.show();
                }else if(imgName.equals("decrese.png")){
                    TrendDialog.setTitle(R.string.dialog_title);      //タイトル設定
                    TrendDialog.setMessage(R.string.dialog_msg);  //内容(メッセージ)設定
                    TrendDialog.show();
                }else{
                    TrendDialog.setTitle(R.string.dialog_title);      //タイトル設定
                    TrendDialog.setMessage(R.string.dialog_msg);  //内容(メッセージ)設定
                    TrendDialog.show();
                }

            }
        });
        mpLineChart = (LineChart) findViewById(R.id.line_chart);
        Button btnShow = findViewById(R.id.btn_show);
        btnShow.setOnClickListener(btnTap);

        String subject = "指定した1日のデータ";

        if (helper == null) {
            helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        settings = new GraphSettings(mpLineChart, maincolor, subject);

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
                    settings.setXaxisMinute(mpLineChart);
                    spYear = findViewById(R.id.sp_year);
                    String strYear = (String) spYear.getSelectedItem();
                    iYear = Integer.parseInt(strYear);
                    Log.d("year", String.valueOf(iYear));
                    spMonth = findViewById(R.id.sp_month);
                    String strMonth = (String) spMonth.getSelectedItem();
                    iMonth = Integer.parseInt(strMonth);
                    Log.d("month", String.valueOf(iMonth));
                    spDate = findViewById(R.id.sp_date);
                    String strDate = (String) spDate.getSelectedItem();
                    iDate = Integer.parseInt(strDate);
                    Log.d("date", String.valueOf(iDate));
                    check = checkOnedayEmpty(iYear, iMonth, iDate);

                    if (check) {
                        oneday = ListHandling.getOnedayList(iYear, iMonth, iDate);
                        if (oneday.value.length < 1) {
                            Toast.makeText(ShowOneday.this, R.string.toast, Toast.LENGTH_SHORT).show();
                            reload();
                            //break;
                        } else {
                            roc = TrendDefinition.calculateRateOfChange(oneday.value, oneday.value.length);
                            trend = TrendDefinition.judgeTrend(roc, true);
                        }
                        TrendDefinition.showTrend(trend,imgTrend);

                        Log.d("roc", String.valueOf(roc));
                        Log.d("trend", String.valueOf(trend));

                        lineDataSet1 = ListHandling.setList(oneday.date, oneday.value, false, mpLineChart);
                        dataSets = new ArrayList<>();
                        dataSets.add(lineDataSet1);
                        data = new LineData(dataSets);
                        settings.setLinesAndPointsDetailsForValue(lineDataSet1, res);
                        mpLineChart.setData(data);
                        mpLineChart.invalidate();

                    } else {
                        Toast.makeText(ShowOneday.this, R.string.toast, Toast.LENGTH_SHORT).show();
                        reload();
                    }
            }
        }
    };

    protected boolean checkOnedayEmpty(int year, int month, int date) {
        check= false;
        Cursor cursor = ListHandling.getOnedayCursor(year, month, date);
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
}