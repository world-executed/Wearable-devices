package com.zenchn.bletester.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.zenchn.bletester.R;
import com.zenchn.bletester.service.BleService;
import com.zenchn.bletester.utils.DBHelper;
import com.zenchn.bletester.utils.DateUtil;
import com.zenchn.bletester.utils.LineChartManager;
import com.zenchn.bletester.utils.LineChartUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataChartActivity extends AppCompatActivity {

    private TextView tv_title;
    private TextView tv_max;
    private TextView tv_min;
    private TextView tv_avg;
    private String title;
    private float[] vList= new float[] {-1,-1,-1};//max,min,avg的值
    private int count=0;
    private Handler handler = new Handler();
    DecimalFormat format = new DecimalFormat("#.00");

    Random random = new Random(10);
    LineChart mLineChart;

    LineChartManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_chart);
        Intent intent = getIntent();
        title = intent.getStringExtra("type");
        tv_title = findViewById(R.id.chart_title);
        tv_max=findViewById(R.id.max_val);
        tv_min=findViewById(R.id.min_val);
        tv_avg=findViewById(R.id.avg_val);
        mLineChart=findViewById(R.id.mLineChar);
        tv_title.setText(title);

//        bindService(new Intent(this, BleService.class), conn, BIND_AUTO_CREATE);
//        registerReceiver(mBroadcastReceiver, makeIntentFilter());
        lm = new LineChartManager(mLineChart,title, Color.RED);
        switch (title){
            case "温度":
                lm.setYAxis(50, 35, 10);break;
            case "湿度":
            case "血氧":
                lm.setYAxis(100, 0, 10);break;
            case "心率":
                lm.setYAxis(150, 0, 10);break;
            default:

        }


        //死循环添加数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            float f=(float) (Math.random() * 5 + 40);
                            updateValues(f);


                        }
                    });
                }
            }
        }).start();


    }


    private void updateValues(float value){
        if(vList[0]==-1){//初始化
            vList[0]=vList[1]=vList[2]=value;
        }
        if(vList[0]<value){
            vList[0]=value;
            tv_max.setText(format.format(vList[0]));
        }
        if (vList[1]>value){
            vList[1]=value;
            tv_min.setText(format.format(vList[1]));
        }
        vList[2]=(vList[2]*count+value)/(count+1);
        tv_avg.setText(format.format(vList[2]));
        count++;

        lm.addEntry(value);



    }

//    public void initData() {
//        ArrayList<Entry> entries = new ArrayList<Entry>();
//        Random random = new Random(10);
//        LineChart mLineChart ;
//        mLineChart=findViewById(R.id.mLineChar);
//        LineChartUtils lc = new LineChartUtils(mLineChart,this);
//
//        for (int i =1;i<60;i++){
//            float v = random.nextFloat();
//            entries.add(new Entry(i,v));
//            lc.setLineChartData(entries,title);
//        }
//
//
//    }

    public void exportData(){
        //TODO:导出数据库中的数据到excel
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from SAMPLE",null);
        ExportToCSV(cursor, DateUtil.getCurrentDatatime()+".csv");
        Toast.makeText(this,"导出成功",Toast.LENGTH_LONG).show();

    }

    public void ExportToCSV(Cursor c, String fileName) {

        int rowCount = 0;
        int colCount = 0;
        FileWriter fw;
        BufferedWriter bfw;
        File sdCardDir = Environment.getExternalStorageDirectory();
        File dir = new File(sdCardDir.getAbsolutePath() + "/ttttt");
        Log.i("DATABASE",dir.toString());
        File saveFile = new File(dir, fileName);
        try {

            rowCount = c.getCount();
            colCount = c.getColumnCount();
            fw = new FileWriter(saveFile);
            bfw = new BufferedWriter(fw);
            if (rowCount > 0) {
                c.moveToFirst();
                // 写入表头
                for (int i = 0; i < colCount; i++) {
                    if (i != colCount - 1) {
                        bfw.write(c.getColumnName(i) + ',');
                    } else {
                        bfw.write(c.getColumnName(i));
                    }
                }
                // 写好表头后换行
                bfw.newLine();
                // 写入数据
                for (int i = 0; i < rowCount; i++) {
                    c.moveToPosition(i);
                    // Toast.makeText(mContext, "正在导出第"+(i+1)+"条",
                    // Toast.LENGTH_SHORT).show();
                    Log.v("导出数据", "正在导出第" + (i + 1) + "条");
                    for (int j = 0; j < colCount; j++) {
                        if (j != colCount - 1) {
                            bfw.write(c.getString(j) + ',');
                        } else {
                            bfw.write(c.getString(j));
                        }
                    }
                    // 写好每条记录后换行
                    bfw.newLine();
                }
            }
            // 将缓存数据写入文件
            bfw.flush();
            // 释放缓存
            bfw.close();
            // Toast.makeText(mContext, "导出完毕！", Toast.LENGTH_SHORT).show();
            Log.v("导出数据", "导出完毕！");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            c.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,"导出数据");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            exportData();
        }
        return super.onOptionsItemSelected(item);
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
}