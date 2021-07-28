package de.tadris.fitness.util.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;

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
        chart.setDrawGridBackground(false);
    }


    public static void setXAxisLabel(Chart chart, String label)
    {
        Description description = new Description();
        description.setText(label);
        description.setTextSize(10);
        description.setEnabled(true);
        chart.setDescription(description);
    }

    public static void setYAxisLabel(Chart chart, String label)
    {
        LegendEntry legend = new LegendEntry();
        legend.label = label;
        chart.getLegend().setCustom(Arrays.asList(new LegendEntry[]{legend}));
        chart.getLegend().setEnabled(true);
        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        chart.getLegend().setFormSize(0);
        chart.getLegend().setFormToTextSpace(-16);
        chart.getLegend().setForm(Legend.LegendForm.NONE);
    }

    public static void defaultHistogram(BarChart chart, Context ctx, ValueFormatter xValueFormatter, ValueFormatter yValueFormatter)
    {
        float min = chart.getBarData().getDataSets().get(0).getEntryForIndex(0).getX();
        int nBins = chart.getBarData().getDataSets().get(0).getEntryCount();
        float max = chart.getBarData().getDataSets().get(0).getEntryForIndex(nBins-1).getX();

        // now that we know the meta inf, set the bars to positions between indices, so we can use an IndexAxisValueFormatter
        // simultaneously construct the new labels
        String[] labels = new String[nBins+1];
        float barWidth= (float) ((max-min)/(nBins-1));
        for(int i=0; i<nBins; i++)
        {
            chart.getBarData().getDataSets().get(0).getEntryForIndex(i).setX(i+0.5f);
            labels[i] = xValueFormatter.getFormattedValue((float) (min+(i+0.5)*barWidth));//distanceUnitUtils.getPace(1/x/60, false, false);
        }
        labels[nBins] = xValueFormatter.getFormattedValue((float) (min+(nBins+0.5)*barWidth));


        chart.getBarData().setBarWidth(1);
        chart.getBarData().setDrawValues(false);
        ChartStyles.defaultBarChart(chart);
        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setLabelRotationAngle(-90);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setGranularity(1);
        chart.getXAxis().setLabelCount(labels.length);
        float shiftY = chart.getData().getYMax()/6;
        chart.getAxisLeft().setAxisMinimum(0-shiftY+0.1f*shiftY);
        chart.getAxisLeft().setGranularity(shiftY);
        chart.setScaleEnabled(false);
        chart.setNestedScrollingEnabled(false);
        chart.getXAxis().setAxisMinimum(-0.5f);
        chart.getXAxis().setAxisMaximum(nBins+0.5f);
        chart.getAxisLeft().setValueFormatter(yValueFormatter);

        LegendEntry[] entries = chart.getLegend().getEntries();
        String unit = entries.length > 0 ? entries[0].label:"";
        chart.setMarker(new DisplayValueMarker(ctx, chart.getAxisLeft().getValueFormatter()," "+unit));
    }

    public static void defaultBarData(BarChart chart, BarData data) {

    }

    public static void setTextAppearance(BarData data)
    {
        data.setValueTextSize(12);
    }


    public static void barChartIconLabel(BarChart chart, BarData data, Context ctx)
    {
        setTextAppearance(data);
        chart.setData(data);

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

    }

    public static void horizontalBarChartIconLabel(HorizontalBarChart chart, BarData data, Context ctx)
    {
        setTextAppearance(data);
        chart.setData(data);

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

        chart.setRenderer(new HorizontalBarChartIconRenderer(chart, chart.getAnimator(), chart.getViewPortHandler(), imageList, ctx));
        chart.setScaleEnabled(false);
        chart.setExtraOffsets(0, 0, 0, 0);
    }
}
