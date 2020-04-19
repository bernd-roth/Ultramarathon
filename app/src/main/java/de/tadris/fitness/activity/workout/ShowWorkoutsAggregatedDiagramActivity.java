package de.tadris.fitness.activity.workout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.unit.DistanceUnitUtils;

import static android.widget.AdapterView.*;

public class ShowWorkoutsAggregatedDiagramActivity extends FitoTrackActivity {

    protected DistanceUnitUtils distanceUnitUtils = Instance.getInstance(this).distanceUnitUtils;

    LineChart chart;
    String selectedWorkoutType = WorkoutType.RUNNING.id;
    double fastestAverage;
    int greatestDistance;
    private String fastestAverageDate;
    private String greatestDistanceDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workouts_aggregated);

        addWorkoutTypeSpinner();
        chart = createChart();

    }

    private LineData calculateLineData(ArrayList<Entry> averageSpeedValues) {
        LineDataSet lineDataSetAverageSpeed;
        lineDataSetAverageSpeed = new LineDataSet(averageSpeedValues, "Average Speed");
        lineDataSetAverageSpeed.setColor(getThemePrimaryColor());
        lineDataSetAverageSpeed.setValueTextColor(getThemePrimaryColor());
        lineDataSetAverageSpeed.setDrawCircles(false);
        lineDataSetAverageSpeed.setLineWidth(4);
        lineDataSetAverageSpeed.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSetAverageSpeed);

        return new LineData(dataSets);
    }

    private void setMaxValues() {
        TextView fastestAverageTextView = findViewById(R.id.fastestAverage);
        fastestAverageTextView.setText(Math.floor(fastestAverage) + " " + distanceUnitUtils.getDistanceUnitSystem().getSpeedUnit());
        TextView fastestAverageDateTextView = findViewById(R.id.fastestAverageDate);
        fastestAverageDateTextView.setText(fastestAverageDate);
        TextView greatestDistanceTextView = findViewById(R.id.greatestDistance);
        greatestDistanceTextView.setText(distanceUnitUtils.getDistance(greatestDistance));
        TextView greatestDistanceDateTextView = findViewById(R.id.greatestDistanceDate);
        greatestDistanceDateTextView.setText(greatestDistanceDate);
    }

    private ArrayList<Entry> calculateValues(Workout[] workouts) {
        fastestAverage = greatestDistance = 0;
        fastestAverageDate = greatestDistanceDate = "--";
        ArrayList<Entry> averageSpeedValues = new ArrayList<>();
        for (Workout workout : workouts) {
            double averageSpeed = distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond((workout.getAvgSpeedTotal()));
            if (fastestAverage < averageSpeed) {
                fastestAverage = averageSpeed;
                fastestAverageDate = workout.getDateString();
            }

            if (greatestDistance < workout.length) {
                greatestDistance = workout.length;
                greatestDistanceDate = workout.getDateString();
            }
            averageSpeedValues.add(new Entry((float) workout.end, (float) averageSpeed));
        }

        return averageSpeedValues;
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
                ArrayList<Entry> averageSpeedValues = calculateValues(workouts);
                setMaxValues();
                LineData data = calculateLineData(averageSpeedValues);
                chart.clear();
                if (workouts.length == 0) {
                    return;
                }
                chart.setData(data);
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

    private LineChart createChart() {
        LineChart chart = findViewById(R.id.aggregatedWorkoutsAverageSpeed);

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
        ;

        chart.getXAxis().setTextColor(Color.DKGRAY);
        chart.getXAxis().setTextSize(15f);
        ;

        chart.setNoDataText(getString(R.string.no_workouts_recorded_for_this_activity));
        chart.setNoDataTextColor(Color.DKGRAY);
        return chart;
    }

}