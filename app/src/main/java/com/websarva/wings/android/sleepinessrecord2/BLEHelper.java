package com.websarva.wings.android.sleepinessrecord2;

import android.app.Activity;
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
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class BLEHelper {

    //定数
    private final static int DEVICES_DIALOG = 1;
    private final static int ERROR_DIALOG =   2;
    private  final static String TAG = "BluetoothTask";

    //UUID
    //サービスの下に複数のキャラクタリスティックがぶら下がっている 今回は最新データのUUID
    private static final String SERVICE_UUID =          "0C4C3000-7700-46F4-AA96-D5E974E32A54";
    private static final String CHAR_LATEST_DATA_UUID = "0C4C3001-7700-46F4-AA96-D5E974E32A54";

    //変数宣言
    private String           errorMessage = "";
    protected BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice  bluetoothDevice = null;
    private InputStream btIn;
    private OutputStream btOut;
    protected BluetoothGatt    bluetoothGatt;
    private BluetoothGattCharacteristic gattCharacteristic;
    private boolean isBluetoothEnable = false;
    private BluetoothServerSocket btserverSocket;
    private int                   mStatus;
    private Date date;
    private String stdate;

    private  String strtemp;
    private  String strhumid;
    private  String strpress;


    private  String decimalTemp;
    private  String decimalHumid;
    private  String decimalPress;

    Calendar calendar;
    String strTime;
    int iYear, iMonth, iDate, lat, lng;

    private BluetoothSocket bluetoothSocket;

    protected Activity activitymain;
    protected Context contextmain;


    //レイアウト関係
    private ProgressDialog waitDialog;
    com.websarva.wings.android.sleepinessrecord2.DataBaseHelper helper;
    private SQLiteDatabase db;

    public BLEHelper(){

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
                    MainActivity.btnRecord.setTag(service.getCharacteristic(UUID.fromString(CHAR_LATEST_DATA_UUID)));
                }
            }else{
                //finish();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (CHAR_LATEST_DATA_UUID.equalsIgnoreCase(characteristic.getUuid().toString())) {

                helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(ContextAct.getmContext());
                db = helper.getReadableDatabase();

                if (helper == null) {
                    helper = new com.websarva.wings.android.sleepinessrecord2.DataBaseHelper(ContextAct.getmContext());
                }

                if (db == null) {
                    db = helper.getWritableDatabase();
                }


                final byte bytename[] = characteristic.getValue();
                //final String value = new String(bytename, StandardCharsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                for (byte d : bytename) {
                    sb.append(String.format("%02X", d));
                }
                final String value = sb.toString();
                Log.e("value", value);
                calendar = Calendar.getInstance();
                iYear = calendar.get(Calendar.YEAR);
                iMonth = calendar.get(Calendar.MONTH);
                iDate = calendar.get(Calendar.DATE);
                int iHour = calendar.get(Calendar.HOUR);
                int iMinute = calendar.get(Calendar.MINUTE);
                int iSecond = calendar.get(Calendar.SECOND);
                strTime = iHour+":"+iMinute+":"+iSecond;
                strtemp = value.substring(2, 6);
                strhumid = value.substring(6, 10);
                strpress = value.substring(18, 22);

                decimalTemp = convert16to10(strtemp, 0.01);
                decimalHumid = convert16to10(strhumid, 0.01);
                decimalPress = convert16to10(strpress, 0.1);

                insertData(db, iYear, iMonth, iDate, strTime, 0, 0, 0, 0, 0, "", 1, 4, lng, lat, decimalTemp, decimalHumid, decimalPress);

                //standardcharsets: UnsupportedEncodingExceptionを避ける
            }
        }
    };

    /**
     * 非同期でBluetoothの接続を閉じる。
     */


    /**
     * Bluetoothと接続を開始する非同期タスク。
     * - 時間がかかる場合があるのでProcessDialogを表示する。
     * - 双方向のストリームを開くところまで。
     */


    private String convert16to10(String value, double unit){
        String privalue = value.substring(0,2);
        String latvalue = value.substring(2,4);

        String truevalue = latvalue + privalue;
        int decvalue = Integer.parseInt(truevalue,16);
        double floatvalue =decvalue * unit;
        String strtruevalue =  String.valueOf(floatvalue);

        return strtruevalue;
    }
    public void insertData(SQLiteDatabase db, int year, int month, int date, String time, int value,
                           int loud, int high, int low, int glare, String others, int behavior,
                           int sleep, int lng, int lat, String temperature, String humidity, String pressure) {

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
