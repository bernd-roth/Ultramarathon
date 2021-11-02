package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;

import java.util.GregorianCalendar;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataProvider;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.exceptions.NoDataException;

public class ShortStatsItemView extends LinearLayout {
    BarChart chart;
    TextView title;
    TimeSpanSelection timeSpanSelection;

    StatsProvider statsProvider;

    public int chartType;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ShortStatsItemView(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ShortStatsItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.short_stats_item, this);

        chart = findViewById(R.id.short_stats_chart);
        timeSpanSelection = findViewById(R.id.short_stats_time_span_selection);
        title = findViewById(R.id.short_stats_title);

        statsProvider = new StatsProvider(context);

        chart.setOnTouchListener(null);
        ChartStyles.defaultBarChart(chart);

        StatsDataProvider statsDataProvider = new StatsDataProvider(context);

        setOnClickListener(view -> context.startActivity(new Intent(getContext(), StatisticsActivity.class)));

        long firstWorkoutTime;
        long lastWorkoutTime;
        try {
            firstWorkoutTime = statsDataProvider.getFirstData(WorkoutProperty.LENGTH, WorkoutTypeManager.getInstance().getAllTypes(context)).time;
            lastWorkoutTime = statsDataProvider.getLastData(WorkoutProperty.LENGTH, WorkoutTypeManager.getInstance().getAllTypes(context)).time;

        }
        catch (NoDataException e)
        {
            return;
        }
        timeSpanSelection.setLimits(firstWorkoutTime, lastWorkoutTime);
        timeSpanSelection.addOnTimeSpanSelectionListener((aggregationSpan, instance) -> updateChart());

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateChart() {
        BarData data;

        timeSpanSelection.loadAggregationSpanFromPreferences();
        long start = timeSpanSelection.getSelectedInstance();
        StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(start,
                start + timeSpanSelection.getSelectedAggregationSpan().spanInterval);

        try {
            switch (chartType)
            {
                case 0:
                    title.setText(getContext().getString(R.string.workoutDistance));
                    data = new BarData(statsProvider.totalDistances(span));
                    ChartStyles.setXAxisLabel(chart, Instance.getInstance(getContext()).distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit());
                    break;
                case 1:
                    title.setText(getContext().getString(R.string.workoutDuration));
                    data = new BarData(statsProvider.totalDurations(span));
                    ChartStyles.setXAxisLabel(chart, getContext().getString(R.string.timeHourShort));
                    break;
                default:
                    title.setText(getContext().getString(R.string.numberOfWorkouts));
                    data = new BarData(statsProvider.numberOfActivities(span));
                    break;
            }
            ChartStyles.barChartIconLabel(chart, data, getContext());
        } catch (NoDataException e) {
            ChartStyles.barChartNoData(chart, getContext());
        }
        chart.invalidate();
    }
}
