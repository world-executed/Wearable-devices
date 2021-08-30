package com.zenchn.bletester.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.zenchn.bletester.R;

import java.util.List;

public class LineChartUtils  {
    private static final String TAG = "LineChartUtils";

    private LineChart lineChart;
    private Handler mHandler = new Handler();
    private Context mContext;
    Runnable hideHighLight = new Runnable() {
        @Override
        public void run() {
            lineChart.highlightValue(null);
        }
    };

    public LineChartUtils(LineChart lineChart,Context context){
        this.lineChart = lineChart;
        this.mContext = context;

        initSetting();
    }

    /**
     * 常用设置
     */
    private void initSetting() {
        lineChart.getDescription().setText("");
        lineChart.getDescription().setTextColor(Color.RED);
        lineChart.getDescription().setTextSize(16);//设置描述的文字 ,颜色 大小
        lineChart.setNoDataText("无数据噢"); //没数据的时候显示
        lineChart.setDrawBorders(false);//是否显示边框
        lineChart.animateX(500);//x轴动画
        lineChart.setTouchEnabled(true); // 设置是否可以触摸
        lineChart.setDragEnabled(true);// 是否可以拖拽
        lineChart.setScaleEnabled(false);// 是否可以缩放 x和y轴, 默认是true
        lineChart.setScaleXEnabled(true); //是否可以缩放 仅x轴
        lineChart.setScaleYEnabled(true); //是否可以缩放 仅y轴
        lineChart.setPinchZoom(true);  //设置x轴和y轴能否同时缩放。默认是否
        lineChart.setDoubleTapToZoomEnabled(true);//设置是否可以通过双击屏幕放大图表。默认是true
        lineChart.setHighlightPerDragEnabled(true);//能否拖拽高亮线(数据点与坐标的提示线)，默认是true
        lineChart.setDragDecelerationEnabled(true);//拖拽滚动时，手放开是否会持续滚动，默认是true（false是拖到哪是哪，true拖拽之后还会有缓冲）
        lineChart.setDragDecelerationFrictionCoef(0.99f);//与上面那个属性配合，持续滚动时的速度快慢，[0,1) 0代表立即停止

        //通过选中监听,来实现不点击图表后3秒,定位线自动消失
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                mHandler.removeCallbacks(hideHighLight);
                mHandler.postDelayed(hideHighLight,3000);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        //设置markerview
        MyMarkerView myMarkerView=new MyMarkerView(mContext, R.layout.custom_marker_view);
        myMarkerView.setChartView(lineChart);
        lineChart.setMarker(myMarkerView);

        //设置X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴位置
        xAxis.setAxisMinimum(1);//设置x轴最小
//        xAxis.setAxisMaximum(12);//设置x轴最大值
        xAxis.setTextSize(14);
        xAxis.setTextColor(Color.RED);
        xAxis.setEnabled(true);//是否显示x轴是否禁用
        xAxis.setDrawLabels(true); //设置x轴标签 即x轴上显示的数值
        xAxis.setDrawGridLines(true);//是否设置x轴上每个点对应的线 即 竖向的网格线
        xAxis.enableGridDashedLine(2,2,2); //竖线 虚线样式  lineLength控制虚线段的长度 spaceLength控制线之间的空间
        xAxis.setLabelRotationAngle(30f);//设置x轴标签的旋转角度
        xAxis.setValueFormatter(new IAxisValueFormatter() { //设置x轴文字样式
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                //x轴上显示时间
                String numInt = "time";
                return numInt;

            }
        });

        //设置Y轴
        YAxis yAxisLef = lineChart.getAxisLeft();
        yAxisLef.setTextSize(14);
        yAxisLef.setAxisMinimum(0);
        YAxis yAxisRight = lineChart.getAxisRight();//获取右侧y轴
        yAxisRight.setEnabled(false);//设置是否禁止
    }

    public void setLineChartData(List<Entry> yValue,String label){

        LineDataSet lineDataSet = new LineDataSet(yValue,label);
        lineDataSet.setHighLightColor(Color.RED); //设置高亮线的颜色
        lineDataSet.setColor(Color.BLACK);//设置折线颜色
        lineDataSet.setCircleColor(Color.BLUE);//设置交点的圆圈的颜色
        lineDataSet.setDrawCircles(false);//设置是否显示交点
        lineDataSet.setDrawValues(true); //设置是否显示交点处的数值
        lineDataSet.setValueTextColor(Color.RED); //设置交点上值的颜色
        lineDataSet.setValueTextSize(14);//设置交点上值的字体大小
//        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);// 设置平滑曲线

        //设置折线上显示数据的格式
//        lineDataSet.setValueFormatter(new IValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                String numOfTwo = "not";
//                return numOfTwo;
//            }
//        });
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }

    class MyMarkerView extends MarkerView {
        TextView marker_view_tv;
        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            marker_view_tv = (TextView) findViewById(R.id.marker_view_tv);

        }

        //获取最新点击坐标点的值的回调
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            String val = e.getY()+"\n"+e.getX();
            marker_view_tv.setText(val);
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
            return super.getOffsetForDrawingAtPoint(posX, posY);
        }

        //设置显示的偏移量,显示在定位线的正上方
        @Override
        public MPPointF getOffset() {
            int measuredHeight = marker_view_tv.getMeasuredHeight();
            int measuredWidth = marker_view_tv.getMeasuredWidth();
            return new MPPointF(-measuredWidth/2,-measuredHeight);
        }
    }
}