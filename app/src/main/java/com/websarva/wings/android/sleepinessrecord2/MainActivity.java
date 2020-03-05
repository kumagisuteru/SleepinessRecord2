package com.websarva.wings.android.sleepinessrecord2;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.websarva.wings.android.sleepinessrecord2.R;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //変数宣言
    private int score;
    Intent intent;
    ContextAct mConAct;
    BLEHelper bleHelper;
    BluetoothAdapter bluetoothAdapter;
    static Button btnRecord;

    private int iYear;
    private int iMonth;
    private int iDate;
    private String strTime;

    //定数
    private final static int DEVICES_DIALOG = 1;
    private final static int ERROR_DIALOG =   2;
    private  final static String TAG = "BluetoothTask";
    private Calendar calendar;
    //UUID
    //サービスの下に複数のキャラクタリスティックがぶら下がっている 今回は最新データのUUID
    private static final String SERVICE_UUID =          "0C4C3000-7700-46F4-AA96-D5E974E32A54";
    private static final String CHAR_LATEST_DATA_UUID = "0C4C3001-7700-46F4-AA96-D5E974E32A54";

    //データベース系
    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    private SQLiteDatabase db;

    //変数宣言
    private String           errorMessage = "";
    private BluetoothDevice  bluetoothDevice = null;
    private InputStream btIn;
    private OutputStream btOut;
    protected static BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic gattCharacteristic;
    private boolean isBluetoothEnable = false;
    private BluetoothServerSocket btserverSocket;
    private int                   mStatus;
    private int date=0;
    private String stdate;

    private  String strtemp;
    private  String strhumid;
    private  String strpress;


    private  String decimalTemp;
    private  String decimalHumid;
    private  String decimalPress;
    SharedPreferences pref;
    private int bootCount = 0;
    private BluetoothSocket bluetoothSocket;

    //レイアウト関係
    private ProgressDialog waitDialog;
    private Button         btnShow;
    private TextView       textView, txtBG;
    private MainActivity   activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //レイアウトアイテム取得
        final SeekBar seekM = (SeekBar) findViewById(R.id.seekBar_Meal);
        final TextView Mscoretex = (TextView) findViewById(R.id.Meal_score);
        final ImageView faceScaleImageView = (ImageView) findViewById(R.id.Face_image);

        btnRecord =findViewById(R.id.btn_record);
        Button btnList =findViewById(R.id.btn_list);
        Button btGraph= findViewById(R.id.btn_graph);
        Button btnEnv = findViewById(R.id.btn_env);
        Button btnFitbit = findViewById(R.id.btn_fitbit);
        textView= (TextView)findViewById(R.id.txt_count);
        txtBG= (TextView)findViewById(R.id.txt_background);

        calendar = Calendar.getInstance();

        textView.setText(R.string.ble_status_disconnect);


        //MainActivityのactivityとcontextを保持するクラスのインスタンス生成
        mConAct = new ContextAct(this, getApplicationContext());
        bleHelper= new BLEHelper();

        init();
        onCreateDialog(DEVICES_DIALOG);

        //データベース取得
        helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(this);
        db = helper.getReadableDatabase();

//シークバー
        Mscoretex.setText("眠気:"+seekM.getProgress());

        //シークバーのリスナー
        seekM.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //ツマミをドラッグしたときに呼ばれる
                        int Mscore;
                        Mscore = seekM.getProgress();

                        //シークバーのスコアに応じて画像を変える
                        if(Mscore >= 0 && Mscore < 20){
                            faceScaleImageView.setImageResource(R.drawable.sleep1);

                        }else if(Mscore >= 20 && Mscore < 40){
                            faceScaleImageView.setImageResource(R.drawable.sleep2);

                        }else if(Mscore >= 40 && Mscore < 60){
                            faceScaleImageView.setImageResource(R.drawable.sleep3);

                        }else if(Mscore >= 60 && Mscore < 80){
                            faceScaleImageView.setImageResource(R.drawable.sleep42);

                        }else if(Mscore >= 80 && Mscore <= 99){
                            faceScaleImageView.setImageResource(R.drawable.sleep52);

                        }else if(Mscore <= 100){
                            faceScaleImageView.setImageResource(R.drawable.sleep5);

                        }

                        //シークバーの数値をテキストに表示する
                        Mscoretex.setText("眠気:"+seekM.getProgress());
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる

                    }

                }
        );
        //記録完了ボタン
        final AlertDialog.Builder RecordconfirmDialog=new AlertDialog.Builder(this);

        // ダイアログの設定
        RecordconfirmDialog.setTitle(R.string.dialog_title);      //タイトル設定
        RecordconfirmDialog.setMessage(R.string.dialog_msg);  //内容(メッセージ)設定

        // OK(肯定的な)ボタンの設定
        RecordconfirmDialog.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // OKボタン押下時の処理
                // Log.d("AlertDialog", "Positive which :" + which);
                //シークバーの数値を取ってくる
                //OKボタン押下時の日付取得

                score = seekM.getProgress();

                /**
                 * バックグラウンド処理開始
                        */
                MyJobService.schedule();

                //追加情報入力画面へ
                intent = new Intent(getApplication(), AdditionalInfo.class);
                intent.putExtra("score", score);
                startActivity(intent);
            }
        });

        // NG(否定的な)ボタンの設定
        RecordconfirmDialog.setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // NGボタン押下時の処理
                // Log.d("AlertDialog", "Negative which :" + which);
                // 何もしない
            }
        });


        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 確認のダイアログを表示
                RecordconfirmDialog.show();

            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(getApplication(), ShowList.class);
                startActivity(intent);
            }
        });
        btGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(getApplication(), ShowGraph.class);
                startActivity(intent);
            }
        });

        btnEnv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(getApplication(), ShowCurrentEnv.class);
                startActivity(intent);
            }
        });

        btnFitbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = "com.fitbit.FitbitMobile";
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(packageName);
                startActivity(intent);
            }
        });

        txtBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyJobService.cancelJobs(getApplicationContext());
                if(MyJobService.isJobServiceOn(getApplicationContext())){
                    txtBG.setText(R.string.txt_bg_working);
                }else{
                    txtBG.setText(R.string.txt_bg_not_working);
                }
            }
        });

    }

    BLEHelper getBLEInstance(){
        return this.bleHelper;
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume","ok");
        textView.setText(R.string.ble_status_disconnect);
        // Bluetooth初期化
        init();
        // ペアリング済みデバイスの一覧を表示してユーザに選ばせる。

        showDialog(DEVICES_DIALOG);
        if(MyJobService.isJobServiceOn(getApplicationContext())){
            txtBG.setText(R.string.txt_bg_working);
        }else{
            txtBG.setText(R.string.txt_bg_not_working);
        }

    }

    // 以下、ダイアログ関連
    @Override
    protected Dialog onCreateDialog(int id) {
        Log.d("onCreateDialog","ok");
        if (id == DEVICES_DIALOG) return createDevicesDialog();
        if (id == ERROR_DIALOG) return createErrorDialog();
        return null;
    }
    @SuppressWarnings("deprecation")
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == ERROR_DIALOG) {
            ((android.app.AlertDialog) dialog).setMessage(errorMessage);
        }
        super.onPrepareDialog(id, dialog);
    }

    public Dialog createDevicesDialog() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Select device");
        Log.d("createDevicesDialog","ok");

        // ペアリング済みデバイスをダイアログのリストに設定する。
        Set<BluetoothDevice> pairedDevices =getPairedDevices();
        final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
        //String[] items = new String[devices.length];
        String[] items = new String[1];
        String blesensor = "EP";
        for (int i=0;i<devices.length;i++) {
           if(devices[i].getName().equals(blesensor)){
                items[i] = devices[i].getName();
            }
        }

        alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

            //デバイスを選択した後の処理
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 選択されたデバイスを通知する。そのまま接続開始。
                //gattcallback関数の中の処理に移る
                bluetoothGatt = devices[which].connectGatt(getApplicationContext(),false, gattCallback);
                // doConnect(devices[which], getApplicationContext());

            }
        });
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder.create();
    }


    @SuppressWarnings("deprecation")
    public void errorDialog(String msg) {
        if (this.isFinishing()) return;
        this.errorMessage = msg;
        this.showDialog(ERROR_DIALOG);
    }
    public Dialog createErrorDialog() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Error");
        alertDialogBuilder.setMessage("");
        alertDialogBuilder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        return alertDialogBuilder.create();
    }

    public void showWaitDialog(String msg) {
        if (waitDialog == null) {
            waitDialog = new ProgressDialog(this);
        }
        waitDialog.setMessage(msg);
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.show();
    }
    public void hideWaitDialog() {
        waitDialog.dismiss();
    }



    /**
     * Bluetoothの初期化。
     */
    public void init() {
        // BTアダプタ取得。取れなければBT未実装デバイス。
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            activity.errorDialog("This device is not implement Bluetooth.");
            return;
        }
        // BTが設定で有効になっているかチェック。
        if (!bluetoothAdapter.isEnabled()) {
            // TODO: ユーザに許可を求める処理。
            activity.errorDialog("This device is disabled Bluetooth.");
            return;
        }
    }
    /**
     * @return ペアリング済みのデバイス一覧を返す。デバイス選択ダイアログ用。
     */
    public Set<BluetoothDevice> getPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    /**
     * 非同期で指定されたデバイスの接続を開始する。
     * - 選択ダイアログから選択されたデバイスを設定される。
     * @param device 選択デバイス
     */


    public final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // 接続状況が変化したら実行.
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 接続に成功したらサービスを検索する.
                Log.d("onConnStateChange","ok");
                mStatus = newState;
                gatt.connect();
                //gattcallbackのOnServicesDiscoveredに移る
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mStatus = newState;
                // 接続が切れたらGATTを空にする.
                if (bluetoothGatt != null) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
                isBluetoothEnable = false;
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // Serviceが見つかったら実行.
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // UUIDが同じかどうかを確認する.
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    // 指定したUUIDを持つCharacteristicを確認する.
                    //ボタンにセンシングしたデータをタグ付けする
                    Log.d("onServiceDiscovered","ok");
                    textView.setText(R.string.ble_status_connect);
                    btnRecord.setTag(service.getCharacteristic(UUID.fromString(CHAR_LATEST_DATA_UUID)));
                }
            }else{
                finish();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (CHAR_LATEST_DATA_UUID.equalsIgnoreCase(characteristic.getUuid().toString())) {

                Log.d("onCharacteristicRead","ok");
                final byte bytename[] = characteristic.getValue();
                //final String value = new String(bytename, StandardCharsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                for (byte d : bytename) {
                    sb.append(String.format("%02X", d));
                }
                final String value = sb.toString();
                Log.e("value aaa", value);

                calendar = Calendar.getInstance();
                iYear = calendar.get(Calendar.YEAR);
                iMonth = calendar.get(Calendar.MONTH)+1;
                iDate = calendar.get(Calendar.DATE);
                int iHour = calendar.get(Calendar.HOUR_OF_DAY);
                int iMinute = calendar.get(Calendar.MINUTE);
                int iSecond = calendar.get(Calendar.SECOND);
                //string型に変換
                strTime = iHour +":"+iMinute +":"+iSecond;
                strtemp = value.substring(2, 6);
                strhumid = value.substring(6, 10);
                strpress = value.substring(18, 22);

                decimalTemp = convert16to10(strtemp, 0.01);
                Log.d("temp",decimalTemp);
                decimalHumid = convert16to10(strhumid, 0.01);
                Log.d("humid",decimalHumid);
                decimalPress = convert16to10(strpress, 0.1);
                Log.d("press",decimalPress);


                insertData(db, iYear, iMonth, iDate, strTime, 0, 0, 0, 0, 0, "", 1, 4, 0, 0,decimalTemp,decimalHumid,decimalPress);
                //standardcharsets: UnsupportedEncodingExceptionを避ける
                runOnUiThread(new Runnable() {
                    public void run() {
                       // textView.setText(value);
                        setProgressBarIndeterminateVisibility(false);
                    }

                    ;
                });

            }
        }
    };

    /**
     * 非同期でBluetoothの接続を閉じる。
     */
    public void doClose() {
        new CloseTask().execute();
    }

    /**
     * Bluetoothと接続を開始する非同期タスク。
     * - 時間がかかる場合があるのでProcessDialogを表示する。
     * - 双方向のストリームを開くところまで。
     */
    private class ConnectTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected void onPreExecute() {
            activity.showWaitDialog("Connect Bluetooth Device.");
        }

        @Override
        protected Object doInBackground(Void... params) {
            try {
                Log.d("ConnectdoInBackground","ok");
                bluetoothSocket.connect();
                btIn = bluetoothSocket.getInputStream();
                btOut = bluetoothSocket.getOutputStream();
            } catch (Throwable t) {
                doClose();
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                Log.e(TAG,result.toString(),(Throwable)result);
                activity.errorDialog(result.toString());
            } else {
                activity.hideWaitDialog();
            }
        }
    }

    /**
     * Bluetoothと接続を終了する非同期タスク。
     * - 不要かも知れないが念のため非同期にしている。
     */
    private class CloseTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                Log.d("ClosedoInBack","ok");
                try{btOut.close();}catch(Throwable t){/*ignore*/}
                try{btIn.close();}catch(Throwable t){/*ignore*/}
                bluetoothSocket.close();
            } catch (Throwable t) {
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                Log.e(TAG,result.toString(),(Throwable)result);
                activity.errorDialog(result.toString());
            }
        }
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


    protected String convert16to10(String value, double unit){
        String privalue = value.substring(0,2);
        String latvalue = value.substring(2,4);

        String truevalue = latvalue + privalue;
        int decvalue = Integer.parseInt(truevalue,16);
        double floatvalue =decvalue * unit;
        String strtruevalue =  String.valueOf(floatvalue);

        return strtruevalue;
    }


}


