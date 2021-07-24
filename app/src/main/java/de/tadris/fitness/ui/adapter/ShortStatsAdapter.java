package de.tadris.fitness.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.ui.statistics.StatisticsActivity;
import de.tadris.fitness.ui.statistics.StatsProvider;
import de.tadris.fitness.util.charts.BarChartIconRenderer;

import static de.tadris.fitness.util.charts.BitmapHelper.drawableToBitmap;
import static de.tadris.fitness.util.charts.BitmapHelper.getBitmaps;

public class ShortStatsAdapter extends RecyclerView.Adapter<ShortStatsAdapter.ViewHolder> {

    private static Context ctx;
    private StatsProvider statsProvider;

    /**
     * The constructor for the adapter.
     * @param ctx is the current context
     */
    public ShortStatsAdapter(Context ctx) {
        this.ctx = ctx;
        statsProvider = new StatsProvider(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.short_stats_item, parent, false);
        return new ViewHolder(view, ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BarData data;
        StatsDataTypes.TimeSpan allTime = new StatsDataTypes.TimeSpan(0,Long.MAX_VALUE);
        switch (position)
        {
            case 0:
                data = statsProvider.numberOfActivities(allTime);
                holder.chart.getDescription().setText(ctx.getString(R.string.numberOfWorkouts));
                break;
            default:
                data = statsProvider.totalDistances(allTime);
                holder.chart.getDescription().setText(ctx.getString(R.string.distances));
                break;
        }
        // data.setDrawValues(false);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return new DecimalFormat("###,##0").format(value);
            }});
        data.setValueTextSize(15);

        holder.chart.setData(data);
        holder.chart.getXAxis().setAxisMinimum(-0.5f);
        holder.chart.getXAxis().setAxisMaximum(holder.chart.getBarData().getXMax()+0.5f);

        ArrayList<Bitmap> imageList = new ArrayList<>();

        for(int i = 0; i < data.getDataSets().get(0).getEntryCount(); i++)
        {
            Drawable d = data.getDataSets().get(0).getEntryForIndex(i).getIcon();
            d.mutate().setColorFilter(ctx.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            imageList.add(drawableToBitmap(d));
        }

        holder.chart.setRenderer(new BarChartIconRenderer(holder.chart, holder.chart.getAnimator(), holder.chart.getViewPortHandler(), imageList, ctx));
        holder.chart.setScaleEnabled(false);
        holder.chart.setExtraOffsets(0, 0, 0, 20);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        BarChart chart;
        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            chart = itemView.findViewById(R.id.short_stats_chart);
            chart.setOnTouchListener(null);
            chart.getAxisLeft().setEnabled(false);
            chart.getAxisLeft().setDrawGridLines(false);
            chart.getAxisRight().setEnabled(false);
            chart.getXAxis().setEnabled(false);
            chart.getLegend().setEnabled(false);
            //chart.getDescription().setEnabled(false);
            chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);





            chart.setDrawBarShadow(false);
            chart.setDrawValueAboveBar(true);
            chart.getDescription().setEnabled(false);
            chart.setPinchZoom(false);
            chart.setDrawGridBackground(false);


            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(7);
            xAxis.setDrawLabels(false);


            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setAxisLineColor(Color.WHITE);
            leftAxis.setDrawGridLines(false);
            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);
            Legend l = chart.getLegend();
            l.setEnabled(false);



            itemView.setOnClickListener(view -> ctx.startActivity(new Intent(ctx, StatisticsActivity.class)));
        }
    }
}
