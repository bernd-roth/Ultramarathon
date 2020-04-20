package de.tadris.fitness.activity.workout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.CombinedData;
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
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.dto.AggregatedWorkoutValues;
import de.tadris.fitness.dto.DataPointAverageSpeed;
import de.tadris.fitness.dto.DataPointDistance;
import de.tadris.fitness.util.unit.DistanceUnitUtils;

import static android.widget.AdapterView.*;
import static com.github.mikephil.charting.charts.CombinedChart.*;

public class ShowWorkoutsAggregatedDiagramActivity extends FitoTrackActivity {

    protected DistanceUnitUtils distanceUnitUtils = Instance.getInstance(this).distanceUnitUtils;

    CombinedChart chart;
    String selectedWorkoutType = WorkoutType.RUNNING.id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workouts_aggregated);
        setTitle(getString(R.string.workout_statistics));

        addWorkoutTypeSpinner();
        chart = createChart();
    }

    private LineData createAverageSpeedLineData(ArrayList<DataPointAverageSpeed> averageSpeedValues) {
        final ArrayList<Entry> averageSpeedEntries = new ArrayList<>();
        for (DataPointAverageSpeed averageSpeedDataPoint: averageSpeedValues ) {
            float averageSpeed = (float) distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond((averageSpeedDataPoint.getAverageSpeed()));
            averageSpeedEntries.add(new Entry( averageSpeedDataPoint.getTime(), averageSpeed));
        }

        LineDataSet lineDataSetAverageSpeed;
        lineDataSetAverageSpeed = new LineDataSet(averageSpeedEntries, "Average Speed");
        lineDataSetAverageSpeed.setColor(getThemePrimaryColor());
        lineDataSetAverageSpeed.setValueTextColor(getThemePrimaryColor());
        lineDataSetAverageSpeed.setDrawCircles(false);
        lineDataSetAverageSpeed.setLineWidth(4);
        lineDataSetAverageSpeed.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSetAverageSpeed);

        return new LineData(dataSets);
    }

    private BarData createDistanceBarData(ArrayList<DataPointDistance> distanceValues) {
        ArrayList<BarEntry> distanceEntries = new ArrayList<>();
        for(DataPointDistance dataPoint: distanceValues) {
            float distance = (float) dataPoint.getDistance() / 1000;
            distanceEntries.add(new BarEntry(dataPoint.getTime(), distance));
        }

        BarDataSet set1 = new BarDataSet(distanceEntries, "Distance");
        set1.setColors(Color.MAGENTA);
        set1.setValueTextColor(Color.rgb(60, 220, 78));
        set1.setValueTextSize(15f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        BarData barData = new BarData(set1);
        return barData;

    }

    private void setMaxValues(AggregatedWorkoutValues aggregatedWorkoutValues) {
        TextView fastestAverageTextView = findViewById(R.id.fastestAverage);
        fastestAverageTextView.setText(distanceUnitUtils.getSpeed(aggregatedWorkoutValues.getFastestAverage()));
        TextView fastestAverageDateTextView = findViewById(R.id.fastestAverageDate);
        fastestAverageDateTextView.setText(aggregatedWorkoutValues.getFastestAverageDate());
        TextView greatestDistanceTextView = findViewById(R.id.greatestDistance);
        greatestDistanceTextView.setText(distanceUnitUtils.getDistance(aggregatedWorkoutValues.getGreatestDistance()));
        TextView greatestDistanceDateTextView = findViewById(R.id.greatestDistanceDate);
        greatestDistanceDateTextView.setText(aggregatedWorkoutValues.getGreatestDistanceDate());
    }



    private void addWorkoutTypeSpinner() {
        Spinner spinner = findViewById(R.id.spinner);
        ArrayList<String> workoutTypes = createChoicesList();

        ArrayAdapter<String> SpinnerAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, workoutTypes) {
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.parseColor("#E30D81"));
                return v;
            }

            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View v = super.getDropDownView(position, convertView,
                        parent);
                v.setBackgroundColor(Color.parseColor("#E30D81"));
                ((TextView) v).setTextColor(Color.parseColor("#ffffff"));
                return v;
            }
        };
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedWorkoutType = workoutTypes.get(position);
                Workout[] workouts = Instance.getInstance(getBaseContext()).db.workoutDao().getWorkoutsHistorically(selectedWorkoutType);

                AggregatedWorkoutValues aggregatedWorkoutValues = new AggregatedWorkoutValues(workouts);
                setMaxValues(aggregatedWorkoutValues);
                LineData averageSpeedData = createAverageSpeedLineData(aggregatedWorkoutValues.getAverageSpeedData());
                BarData distanceData = createDistanceBarData(aggregatedWorkoutValues.getDistanceData());
                chart.clear();
                if (workouts.length == 0) {
                    return;
                }

                CombinedData combinedData = new CombinedData();
                combinedData.setData(distanceData);
                combinedData.setData(averageSpeedData);
                chart.setData(combinedData);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }


        });
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(SpinnerAdapter);
    }

    private ArrayList<String> createChoicesList() {
        ArrayList<String> workoutTypes = new ArrayList<>();
        workoutTypes.add(WorkoutType.RUNNING.id);
        workoutTypes.add(WorkoutType.CYCLING.id);
        workoutTypes.add(WorkoutType.HIKING.id);
        workoutTypes.add(WorkoutType.INLINE_SKATING.id);
        workoutTypes.add(WorkoutType.WALKING.id);
        workoutTypes.add(WorkoutType.OTHER.id);

        return workoutTypes;
    }

    private CombinedChart createChart() {
        CombinedChart chart = findViewById(R.id.combinedChartAggregatedWorkouts);

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
        chart.getAxisRight().setTextSize(15f);

        chart.getXAxis().setTextColor(Color.DKGRAY);
        chart.getXAxis().setTextSize(15f);

        chart.setNoDataText(getString(R.string.no_workouts_recorded_for_this_activity));
        chart.setNoDataTextColor(Color.DKGRAY);

        Description description = new Description();
        description.setText(getString(R.string.maximum_average_speed));
        description.setTextColor(Color.DKGRAY);
        chart.setDescription(description);

        chart.setDrawOrder(new DrawOrder[]{
                DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.CANDLE, DrawOrder.LINE, DrawOrder.SCATTER
        });

        return chart;
    }

}