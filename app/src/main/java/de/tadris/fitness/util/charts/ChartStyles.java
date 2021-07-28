package de.tadris.fitness.util.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.Icon;

import static de.tadris.fitness.util.charts.BitmapHelper.drawableToBitmap;

public class ChartStyles {
    public static void defaultBarChart(BarChart chart)
    {
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setFitBars(true);
    }

    public static void defaultLineChart(CombinedChart chart) {
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(true);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(true);
    }

    public static void defaultBarData(BarChart chart, BarData data) {

    }

    public static void formatValuesNoDecimals(BarData data)
    {
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return new DecimalFormat("###,##0").format(value);
            }});
        data.setValueTextSize(12);
    }


    public static void barChartIconLabel(BarChart chart, BarData data, Context ctx)
    {
        formatValuesNoDecimals(data);

        ArrayList<Bitmap> imageList = new ArrayList<>();
        for(int i = 0; i < data.getDataSets().get(0).getEntryCount(); i++)
        {
            try {
                WorkoutType w = (WorkoutType) data.getDataSets().get(0).getEntryForIndex(i).getData();
                Drawable d = ctx.getDrawable(Icon.getIcon(w.icon));
                d.mutate().setColorFilter(w.color, PorterDuff.Mode.SRC_IN);
                imageList.add(drawableToBitmap(d));
            }
            catch (Exception e)
            {
                return; // If drawable not available, its not possible...
            }
        }

        chart.setRenderer(new BarChartIconRenderer(chart, chart.getAnimator(), chart.getViewPortHandler(), imageList, ctx));
        chart.setScaleEnabled(false);
        chart.setExtraOffsets(0, 0, 0, 25);

        chart.setData(data);
    }

    public static void horizontalBarChartIconLabel(HorizontalBarChart chart, BarData data, Context ctx)
    {
        formatValuesNoDecimals(data);


        ArrayList<Bitmap> imageList = new ArrayList<>();
        for(int i = 0; i < data.getDataSets().get(0).getEntryCount(); i++)
        {
            try {
                WorkoutType w = (WorkoutType) data.getDataSets().get(0).getEntryForIndex(i).getData();
                Drawable d = ctx.getDrawable(Icon.getIcon(w.icon));
                d.mutate().setColorFilter(w.color, PorterDuff.Mode.SRC_IN);
                imageList.add(drawableToBitmap(d));
            }
            catch (Exception e)
            {
                return; // If drawable not available, its not possible... //Todo: But it should be possible to draw the other icons
            }
        }

        HorizontalBarChartIconRenderer renderer = new HorizontalBarChartIconRenderer(chart, chart.getAnimator(), chart.getViewPortHandler(), imageList, ctx);
        chart.setRenderer(renderer);
        chart.setScaleEnabled(false);
        chart.setExtraOffsets(0, 0, 0, 0);

        chart.setData(data);
    }
}
