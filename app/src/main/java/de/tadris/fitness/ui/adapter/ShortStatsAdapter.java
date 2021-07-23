package de.tadris.fitness.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;

import de.tadris.fitness.R;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.ui.statistics.StatisticsActivity;
import de.tadris.fitness.ui.statistics.StatsProvider;
import de.tadris.fitness.ui.workout.AggregatedWorkoutStatisticsActivity;

public class ShortStatsAdapter extends RecyclerView.Adapter<ShortStatsAdapter.ViewHolder> {

    private Context ctx;
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
        CombinedData data = new CombinedData();
        StatsDataTypes.TimeSpan allTime = new StatsDataTypes.TimeSpan(0,Long.MAX_VALUE);
        switch (position)
        {
            case 0:
                data.setData(statsProvider.numberOfActivities(allTime));
                break;
            default:
                data.setData(statsProvider.totalDistances(allTime));
                break;
        }
        holder.chart.setData(data);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CombinedChart chart;
        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            chart = itemView.findViewById(R.id.short_stats_chart);
            chart.setOnTouchListener(null);
            itemView.setOnClickListener(view -> ctx.startActivity(new Intent(ctx, StatisticsActivity.class)));
        }
    }
}
