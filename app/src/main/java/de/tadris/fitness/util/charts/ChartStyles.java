package de.tadris.fitness.util.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.Icon;
import de.tadris.fitness.util.charts.formatter.FractionedDateFormatter;
import de.tadris.fitness.util.charts.marker.DisplayValueMarker;
import de.tadris.fitness.util.charts.marker.WorkoutDisplayMarker;

import static de.tadris.fitness.util.charts.BitmapHelper.drawableToBitmap;

public class ChartStyles {

    public static float BAR_WIDTH_FACTOR = 2f/3f;

    public static void defaultChart(BarLineChartBase chart)
    {
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
    }

    public static void defaultBarChart(BarChart chart)
    {
        defaultChart(chart);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setEnabled(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setFitBars(true);
    }

    public static void defaultLineChart(BarLineChartBase chart) {
        defaultChart(chart);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setDrawGridLines(true);
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

    public static void updateCombinedChartToSpan(CombinedChart chart, CombinedData combinedData, AggregationSpan aggregationSpan, float stats_time_factor, Context ctx) {
        ChartStyles.setTextAppearance(combinedData);
        if(combinedData.getBarData() != null) {
            float barWidth = Math.max(aggregationSpan.spanInterval, AggregationSpan.DAY.spanInterval);
            combinedData.getBarData().setBarWidth(barWidth / stats_time_factor * ChartStyles.BAR_WIDTH_FACTOR);
        }
        chart.getXAxis().setValueFormatter(new FractionedDateFormatter(ctx,aggregationSpan));
        chart.getXAxis().setGranularity((float)aggregationSpan.spanInterval / stats_time_factor);
        ChartStyles.setXAxisLabel(chart, ctx.getString(aggregationSpan.axisLabel));

        chart.getAxisLeft().setValueFormatter(combinedData.getMaxEntryCountSet().getValueFormatter());

        if (chart.getLegend().getEntries().length > 0) {
            String yLabel = chart.getLegend().getEntries()[0].label;
            chart.setMarker(new DisplayValueMarker(ctx, chart.getAxisLeft().getValueFormatter(), yLabel));
        } else {
            chart.setMarker(new DisplayValueMarker(ctx, chart.getAxisLeft().getValueFormatter(), ""));
        }

        chart.setData(combinedData);
        chart.getXAxis().setAxisMinimum(combinedData.getXMin() - aggregationSpan.spanInterval / stats_time_factor / 2);
        chart.getXAxis().setAxisMaximum(combinedData.getXMax() + aggregationSpan.spanInterval / stats_time_factor / 2);
        chart.invalidate();
    }

    public static void setTextAppearance(ChartData data)
    {
        data.setValueTextSize(10);
    }


    public static void barChartIconLabel(BarChart chart, BarData data, Context ctx)
    {
        setTextAppearance(data);

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
        chart.setMarker(new WorkoutDisplayMarker(ctx));
        chart.getAxisLeft().setAxisMinimum(0);
    }

    public static void barChartNoData(BarChart chart, Context ctx)
    {
        chart.setDrawMarkers(false);
        chart.setData(new BarData()); // Needed in case there is nothing to clear...
        chart.clearValues();
        chart.setExtraOffsets(0, 0, 0, 0);
        ChartStyles.setXAxisLabel(chart, ctx.getString(R.string.no_workouts_recorded));
    }

    public static void horizontalBarChartIconLabel(HorizontalBarChart chart, BarData data, Context ctx)
    {
        setTextAppearance(data);

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

        chart.setRenderer(new HorizontalBarChartIconRenderer(chart, chart.getAnimator(), chart.getViewPortHandler(), imageList, ctx));
        chart.setScaleEnabled(false);
        chart.setExtraOffsets(30, 0, 0, 0);

        chart.setData(data);
        chart.setMarker(new WorkoutDisplayMarker(ctx));
        chart.setDrawMarkers(true);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisRight().setAxisMinimum(0);
        chart.getDescription().setEnabled(false);
    }

    public static AggregationSpan statsAggregationSpan(BarLineChartBase chart, float stats_time_factor)
    {
        long timeSpan = (long) ((chart.getHighestVisibleX() - chart.getLowestVisibleX()) * stats_time_factor);
        AggregationSpan aggregationSpan;

        if (TimeUnit.DAYS.toMillis(1095) < timeSpan) {
            aggregationSpan = AggregationSpan.YEAR;
        } else if (TimeUnit.DAYS.toMillis(93) < timeSpan) {
            aggregationSpan = AggregationSpan.MONTH;
        } else if (TimeUnit.DAYS.toMillis(21) < timeSpan) {
            aggregationSpan = AggregationSpan.WEEK;
        } else {
            aggregationSpan = AggregationSpan.SINGLE;
        }
        return aggregationSpan;
    }
}
