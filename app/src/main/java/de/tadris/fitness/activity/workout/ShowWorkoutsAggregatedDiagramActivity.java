package de.tadris.fitness.activity.workout;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.activity.FitoTrackActivity;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.util.unit.DistanceUnitUtils;

public class ShowWorkoutsAggregatedDiagramActivity extends FitoTrackActivity {

    protected DistanceUnitUtils distanceUnitUtils = Instance.getInstance(this).distanceUnitUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workouts_aggregated);

        ArrayList<Entry> averageSpeedValues = new ArrayList<>();

        Workout[] workouts = Instance.getInstance(this).db.workoutDao().getWorkoutsHistorically();

        double fastestAverage = 0;
        int greatestDistance = 0;

        for (Workout workout: workouts) {
            double averageSpeed = distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond((workout.getAvgSpeedTotal()));
            fastestAverage = Math.max(fastestAverage, averageSpeed);
            greatestDistance = Math.max(greatestDistance, workout.length);
            averageSpeedValues.add(new Entry((float) workout.end, (float) averageSpeed));
        }

        TextView fastestAverageTextView = findViewById(R.id.fastestAverage);
        fastestAverageTextView.setText(Math.floor(fastestAverage) + " " + distanceUnitUtils.getDistanceUnitSystem().getSpeedUnit());
        TextView greatestDistanceTextView = findViewById(R.id.greatestDistance);
        greatestDistanceTextView.setText(distanceUnitUtils.getDistance(greatestDistance));

        LineDataSet lineDataSetAverageSpeed;
        lineDataSetAverageSpeed = new LineDataSet(averageSpeedValues, "Average Speed");
        lineDataSetAverageSpeed.enableDashedLine(10f, 5f, 0f);
        lineDataSetAverageSpeed.setDrawFilled(true);

        LineChart chart = createChart();;
        lineDataSetAverageSpeed.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSetAverageSpeed);

        LineData data = new LineData(dataSets);

        chart.setData(data);
    }

    private LineChart createChart() {
        LineChart chart  = findViewById(R.id.aggregatedWorkoutsAverageSpeed);

        AxisBase xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {

                long millis = TimeUnit.MILLISECONDS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        chart.getAxisLeft().setTextColor(Color.DKGRAY);
        chart.getAxisLeft().setTextSize(15f);
        chart.getAxisRight().setTextColor(Color.DKGRAY);
        chart.getAxisRight().setTextSize(15f);;

        chart.getXAxis().setTextColor(Color.DKGRAY);
        chart.getXAxis().setTextSize(15f);;

        return chart;
    }

}