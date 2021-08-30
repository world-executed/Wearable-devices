package com.zenchn.bletester.ui;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zenchn.bletester.R;
import com.zenchn.bletester.service.BleService;
import com.zenchn.bletester.utils.DBHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;


public class DataActivity extends AppCompatActivity implements View.OnClickListener{
    final String TAG = "DataActivity";

    private ConstraintLayout tempLayout;
    private ConstraintLayout humidLayout;
    private ConstraintLayout heartLayout;
    private ConstraintLayout SpO2Layout;
    TextView tv_temp;
    TextView tv_humid;
    TextView tv_heart;
    TextView tv_spo2;

    UUID charUuid;
    UUID serUuid;
    BleService bleService;
    BluetoothGattCharacteristic gattChar;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BleService.LocalBinder) service).getService();
            gattChar = bleService.mBluetoothGatt.getService(serUuid)
                    .getCharacteristic(charUuid);
            bleService.mBluetoothGatt.readCharacteristic(gattChar);
            if (gattChar.getDescriptors().size() != 0) {
                BluetoothGattDescriptor des = gattChar.getDescriptors().get(0);
                des.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bleService.mBluetoothGatt.writeDescriptor(des);
            }
            int prop = gattChar.getProperties();
            if ((prop & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                bleService.mBluetoothGatt.setCharacteristicNotification(
                        gattChar, false);
            }
            if ((prop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                bleService.mBluetoothGatt.setCharacteristicNotification(
                        gattChar, false);
            }
            if ((prop & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                bleService.mBluetoothGatt.setCharacteristicNotification(
                        gattChar, false);
            }
            if ((prop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                bleService.mBluetoothGatt.setCharacteristicNotification(
                        gattChar, true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //接收到可读数据
            if (BleService.ACTION_DATA_AVAILABLE.equals(action)){
               float[] record = intent.getFloatArrayExtra("floatValue");
               if(record!=null) {
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           DecimalFormat format = new DecimalFormat("#.0");
                           tv_temp.setText(format.format(record[0])+"°C");
                           tv_humid.setText(format.format(record[1])+"%");
                           tv_heart.setText(format.format(record[2])+" rpm");
                           tv_spo2.setText(format.format(record[3])+"%");
                       }
                   });
                   Log.i(TAG,"reocrd" + record[0]);
               }
               Log.i(TAG,"received one notification");


            }

            if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(DataActivity.this, "设备连接断开",
Toast.LENGTH_SHORT).show();}
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        init();
        bindService(new Intent(this, BleService.class), conn, BIND_AUTO_CREATE);
        registerReceiver(mBroadcastReceiver, makeIntentFilter());

    }

    private void init() {
        tempLayout = findViewById(R.id.temp_layout);
        humidLayout = findViewById(R.id.humid_layout);
        heartLayout = findViewById(R.id.heart_layout);
        SpO2Layout = findViewById(R.id.SpO2_layout);

        tv_temp = findViewById(R.id.temp);
        tv_humid = findViewById(R.id.humid);
        tv_heart = findViewById(R.id.heart);
        tv_spo2 = findViewById(R.id.SpO2);

        tempLayout.setOnClickListener(this);
        humidLayout.setOnClickListener(this);
        heartLayout.setOnClickListener(this);
        SpO2Layout.setOnClickListener(this);

        charUuid = UUID.fromString(getIntent().getExtras().get("charUUID")
                .toString());
        serUuid = UUID.fromString(getIntent().getExtras().get("serUUID")
                .toString());
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.temp_layout:
                intent.putExtra("type","温度");break;
            case R.id.humid_layout:
                intent.putExtra("type","湿度");break;
            case R.id.heart_layout:
                intent.putExtra("type","心率");break;
            case R.id.SpO2_layout:
                intent.putExtra("type","血氧");break;
            default:
                break;
        }
        intent.setClass(DataActivity.this,DataChartActivity.class);
        startActivity(intent);
    }



    private IntentFilter makeIntentFilter() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_CHAR_READED);
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleService.BATTERY_LEVEL_AVAILABLE);
        intentFilter.addAction(BleService.ACTION_GATT_RSSI);
        return intentFilter;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unbindService(conn);
        unregisterReceiver(mBroadcastReceiver);
    }




}


