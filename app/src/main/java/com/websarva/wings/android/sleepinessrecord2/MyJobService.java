package com.websarva.wings.android.sleepinessrecord2;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.PersistableBundle;
import android.util.Log;
import java.util.Calendar;


import static android.app.job.JobScheduler.RESULT_SUCCESS;

public class MyJobService extends JobService {

    int iValue;
    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    private SQLiteDatabase db;
    Calendar calendar;
    String strTime;
    int iYear, iMonth, iDate, lat, lng;

    BLEHelper bleHelper;
    Location gps;
    //ContextAct mConAct;

    public MyJobService() {

    }


    private final static int JOB_ID = 0x01;
    private final static ComponentName JOB_SERVICE_NAME =
            new ComponentName("com.websarva.wings.android.sleepinessrecord2",
                    "com.websarva.wings.android.sleepinessrecord2.MyJobService");


    public static void cancelJobs(Context context) {
        // TODO: schedule job
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        Log.d("Info--- ","canseljob" );
        scheduler.cancel(JOB_ID);
    }

    public static boolean isJobServiceOn( Context context ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;

        boolean hasBeenScheduled = false ;

        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == JOB_ID ) {
                hasBeenScheduled = true ;
                break ;
            }
        }

        return hasBeenScheduled ;
    }


    public static void schedule() {

        Context context = ContextAct.getmContext();

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, JOB_SERVICE_NAME);

        builder.setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR);

        PersistableBundle bundle = new PersistableBundle();
        builder.setExtras(bundle);
        builder.setPeriodic(15 * 60 * 1000);
        //builder.setPersisted(true);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        //builder.setRequiresCharging(true);

        //scheduler.schedule(builder.build());
        int ret = scheduler.schedule(builder.build());
        if (ret != RESULT_SUCCESS) {
            Log.v("scheduler ret:", String.valueOf(ret));
        }

        Log.d("Info--- ","startjob" );
    }

    private JobParameters mParams;


    // ---- scheduled job interface

    @Override
    //  @DebugLog
    public boolean onStartJob(JobParameters params) {

        //データベース取得

        helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(this);
        db = helper.getReadableDatabase();
        mParams = params;

        if (helper == null) {
            helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        Log.d("before ","before" );

        bleHelper = new BLEHelper();
        gps = new LocationService().getLocation();




        if ((MainActivity.btnRecord.getTag() != null) //センシングしたデータを格納したボタンのタグが空じゃなくて
                && (MainActivity.btnRecord.getTag() instanceof BluetoothGattCharacteristic)) { //gattcharactaristic型なら

            //タグ取得
            BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) MainActivity.btnRecord
                    .getTag();
            //値の読み込み
            //gattcallback内のOnCharacteristicRead関数が呼び出される
            MainActivity.bluetoothGatt.readCharacteristic(ch);
        }


        if(gps == null){
            lat = 0;
            lng = 0;
        }else{
            lat = (int)(gps.getLatitude()*100);
            lng = (int)(gps.getLongitude()*100);
        }

        Log.d("lat ",String.valueOf(lat) );
        Log.d("lng ",String.valueOf(lng) );
        // 緯度の表示
        /*
        String strlat = String.valueOf(gps.location.getLatitude());
        int lat = Integer.parseInt(strlat)  * 100;
        // 経度の表示
        String strlng = String.valueOf(gps.location.getLongitude());
        int lng = Integer.parseInt(strlng) * 100;
*/
        calendar = Calendar.getInstance();
        iYear = calendar.get(Calendar.YEAR);
        iMonth = calendar.get(Calendar.MONTH)+1;
        iDate = calendar.get(Calendar.DATE);
        int iHour = calendar.get(Calendar.HOUR_OF_DAY);
        int iMinute = calendar.get(Calendar.MINUTE);
        int iSecond = calendar.get(Calendar.SECOND);
        //string型に変換
        strTime = iHour + ":" + iMinute + ":" + iSecond;
insertData(db, iYear, iMonth, iDate, strTime, 0, 0, 0, 0, 0, "", 1, 4, lng, lat,"","","");
        //addInfo.insertData(db, iYear, iMonth, iDate, strTime, 0, 0, 0, 0, 0, "", 1, 4, 0, 0);
            /// locationActivity.locationStart();
            //Log.d("ivalue", "****" + iValue.toString());
/**        new Thread(new Runnable() {

@Override public void run() {
try {
Thread.sleep(10000);
Random random = new Random();
iValue = random.nextInt(100);
Log.d("ivalue","****"+iValue.toString());
insertData(db, iValue);
} catch (InterruptedException e) {
e.printStackTrace();
}
if (mParams != null) {
Log.i("stage", "jobFinished");
jobFinished(mParams, true);
}
}
}).start();
 **/
            return true;
        }


    @Override
    //  @DebugLog
    public boolean onStopJob(JobParameters params) {
        jobFinished(params, true);
        return false;
    }

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