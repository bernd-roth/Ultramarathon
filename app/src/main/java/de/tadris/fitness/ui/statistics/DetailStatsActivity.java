package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.ui.adapter.StatisticsAdapter;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.exceptions.NoDataException;

public class DetailStatsActivity extends FitoTrackActivity {

    CombinedChart chart;

    WorkoutType workoutType = new WorkoutType();
    AggregationSpan aggregationSpan = AggregationSpan.WEEK;

    StatsProvider statsProvider = new StatsProvider(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_detail);
        setTitle(getString(R.string.details));
        setupActionBar();

        chart = findViewById(R.id.stats_detail_chart);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String chartId = getIntent().getExtras().getString("chart");

        if (chartId.equals(this.getString(R.string.workoutAvgSpeedShort))){
            workoutType.id = (String) getIntent().getSerializableExtra("type");
            CandleDataSet dataSet = null;
            try {
                dataSet = statsProvider.getSpeedCandleData(aggregationSpan, workoutType);
            } catch (NoDataException e) {
                e.printStackTrace();
            }

            // Add candle data
            CombinedData combinedData = new CombinedData();
            combinedData.setData(new CandleData(dataSet));

            // Create background line
            LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(dataSet);
            combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(this, lineDataSet)));
            chart.setData(combinedData);
            chart.invalidate();
        }
    }
}

