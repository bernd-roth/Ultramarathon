package de.tadris.fitness.util.statistics;

import android.graphics.Matrix;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;

public class ChartSynchronizer {

    List<Chart> synchronyCharts;

    public ChartSynchronizer() {
        synchronyCharts = new ArrayList<>();
    }

    public OnChartGestureListener addChart(Chart chart) {
        synchronyCharts.add(chart);
        return gestureListener(chart);
    }

    protected void syncCharts(Chart sender, List<Chart> retrievers) {
        Matrix senderMatrix;
        float[] senderValues = new float[9];
        senderMatrix = sender.getViewPortHandler().getMatrixTouch();
        senderMatrix.getValues(senderValues);

        Matrix retrieverMatrix;
        float[] retrieverValues = new float[9];

        for (Chart retriever : retrievers) {
            if (retriever != sender) {
                retrieverMatrix = retriever.getViewPortHandler().getMatrixTouch();
                retrieverMatrix.getValues(retrieverValues);
                retrieverValues[Matrix.MSCALE_X] = senderValues[Matrix.MSCALE_X];
                retrieverValues[Matrix.MTRANS_X] = senderValues[Matrix.MTRANS_X];
                retrieverMatrix.setValues(retrieverValues);
                retriever.getViewPortHandler().refresh(retrieverMatrix, retriever, true);
            }
        }
    }

    public OnChartGestureListener gestureListener(Chart chart) {
        return new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                syncCharts(chart, synchronyCharts);
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                syncCharts(chart, synchronyCharts);
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                syncCharts(chart, synchronyCharts);
            }
        };
    }
}
