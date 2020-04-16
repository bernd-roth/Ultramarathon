package de.tadris.fitness.activity.workout;

import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
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
import de.tadris.fitness.util.unit.Metric;

import static java.time.LocalDate.now;

public class ShowWorkoutsAggregatedDiagramActivity extends FitoTrackActivity {

    private LineChart chart;

    protected DistanceUnitUtils distanceUnitUtils = Instance.getInstance(this).distanceUnitUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workouts_aggregated);

        chart = findViewById(R.id.aggregatedWorkoutsAverageSpeed);

        AxisBase xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {

                long millis = TimeUnit.MILLISECONDS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        ArrayList<Entry> values = new ArrayList<>();

        Workout[] workouts = Instance.getInstance(this).db.workoutDao().getWorkoutsHistorically();

        //@todo reverse order
        for (Workout workout: workouts) {
            values.add(new Entry((float) workout.end, (float) distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond((workout.getAvgSpeedTotal()))));
        }

        LineDataSet set1;

        set1 = new LineDataSet(values, "Average Running Speed");
        set1.enableDashedLine(10f, 5f, 0f);
        set1.setDrawFilled(true);
        set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);
    }

}