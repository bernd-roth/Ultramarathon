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
import android.widget.TextView;

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
import de.tadris.fitness.util.charts.ChartStyles;

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
                holder.title.setText(ctx.getString(R.string.numberOfWorkouts));
                break;
            default:
                data = statsProvider.totalDistances(allTime);
                holder.title.setText(ctx.getString(R.string.distances));
                break;
        }
        ChartStyles.barChartIconLabel(holder.chart, data, ctx);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        BarChart chart;
        TextView title;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            chart = itemView.findViewById(R.id.short_stats_chart);
            title = itemView.findViewById(R.id.short_stats_title);
            chart.setOnTouchListener(null);
            ChartStyles.defaultBarChart(chart);

            itemView.setOnClickListener(view -> ctx.startActivity(new Intent(ctx, StatisticsActivity.class)));
        }
    }
}
