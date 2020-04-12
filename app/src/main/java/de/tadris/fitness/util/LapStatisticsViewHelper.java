package de.tadris.fitness.util;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.util.unit.DistanceUnitUtils;
import de.tadris.fitness.util.unit.TimeFormatter;

public class LapStatisticsViewHelper {

    private DistanceUnitUtils distanceUnitUtils;

    private ArrayList<View> CreateLapViews(Activity activity, ViewGroup root, Workout workout, List<LapStatistics.LapInfo> laps) {

        ArrayList<View> lapViews = new ArrayList<>();
        boolean switchRows = false;
        for (LapStatistics.LapInfo lapInfo : laps) {
            View laptimeEntry = (View) activity.getLayoutInflater().inflate(R.layout.laptime_entry, root, false);
            if (switchRows)
                laptimeEntry.setBackgroundColor(activity.getResources().getColor(R.color.lineHighlight));
            switchRows = !switchRows;

            if (lapInfo.fastest) {
                laptimeEntry.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
                laptimeEntry.getBackground().setAlpha(100);
            }
            if (lapInfo.slowest) {
                laptimeEntry.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                laptimeEntry.getBackground().setAlpha(100);
            }
            TextView dist = laptimeEntry.findViewById(R.id.laptimeDist);
            TextView text = laptimeEntry.findViewById(R.id.laptimeText);
            TextView mDown = laptimeEntry.findViewById(R.id.laptimeMetersDown);
            TextView mUp = laptimeEntry.findViewById(R.id.laptimeMetersUp);


            double distInKm = lapInfo.dist/(double)1000;
            double roundedDist = Math.floor(distanceUnitUtils.getDistanceUnitSystem().getDistanceFromKilometers(distInKm)*100)/100.0;
            dist.setText(roundedDist + "");
            text.setText(TimeFormatter.formatDuration(lapInfo.time));
            mDown.setText(Math.round(lapInfo.metersDown) + "");
            mUp.setText(Math.round(lapInfo.metersUp) + "");
            lapViews.add(laptimeEntry);
        }
        return lapViews;
    }

    private ArrayAdapter<String> DistanceAdapter()
    {
        List<String> distUnits = new ArrayList<>();
        distUnits.add(distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit());
        distUnits.add(distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit());
        distUnits.add("#");
        return new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, distUnits);
    }

    private ArrayAdapter<String> TimeAdapter()
    {
        List<String> distUnits = new ArrayList<>();
        distUnits.add("h");
        distUnits.add("min");
        distUnits.add("s");
        distUnits.add("#");
        return new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, distUnits);
    }

    Activity activity;
    ViewGroup root;
    Workout workout;
    List<WorkoutSample> samples;
    ViewGroup listViews;
    Spinner unitSpinner;
    EditText lapLengthEdit;
    Spinner typeSpinner;

    public ViewGroup CreateLapStatisticsView(Activity activity, ViewGroup root, Workout workout, List<WorkoutSample> samples) {
        this.activity = activity;
        this.root = root;
        this.workout = workout;
        this.samples = samples;
        ViewGroup l = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.laptimes, root, false);
        listViews = l.findViewById(R.id.laplist);
        unitSpinner = l.findViewById(R.id.lapLengthUnit);
        lapLengthEdit = l.findViewById(R.id.lapLengthEdit);
        typeSpinner = l.findViewById(R.id.lapTypeSpinner);
        distanceUnitUtils = Instance.getInstance(activity).distanceUnitUtils;

        unitSpinner.setAdapter(DistanceAdapter());
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LapStatistics.LapCriterion criterion = LapStatistics.LapCriterion.values()[typeSpinner.getSelectedItemPosition()];
                switch (criterion)
                {
                    case TIME: // for time like units, use as standard seconds
                        if(i==0) // hours
                            lapLengthEdit.setText("1");
                        else if(i==1) // minutes
                            lapLengthEdit.setText("5");
                        else if(i==2) // seconds
                            lapLengthEdit.setText("30");
                        else if(i==3) // #
                            lapLengthEdit.setText("5");
                        break;
                    case DISTANCE: // For Distance (like) cases, transform to standard "meters"
                    case METERS_UP:
                    case METERS_DOWN:
                        if(i==0) // km/miles/...
                            lapLengthEdit.setText("1");
                        else if(i==1) // m/yd/...
                            lapLengthEdit.setText("100");
                        else if(i==2) // #
                            lapLengthEdit.setText("5");
                        break;
                }

                LoadLaps();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        lapLengthEdit.setText("1000");
        lapLengthEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    lapLengthEdit.clearFocus();
                    InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(lapLengthEdit.getWindowToken(), 0);
                    return true;
                }
                return false;
        }
        });

        lapLengthEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                LoadLaps();
            }
        });

        typeSpinner.setAdapter(new ArrayAdapter<LapStatistics.LapCriterion>(activity, android.R.layout.simple_list_item_1, LapStatistics.LapCriterion.values()));
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LapStatistics.LapCriterion criterion = LapStatistics.LapCriterion.values()[i];
                switch (criterion)
                {
                    case TIME:
                        unitSpinner.setAdapter(TimeAdapter());
                        unitSpinner.setSelection(1);
                        lapLengthEdit.setText("1");
                        break;
                    case DISTANCE:
                        unitSpinner.setAdapter(DistanceAdapter());
                        unitSpinner.setSelection(0);
                        lapLengthEdit.setText("1");
                        break;
                    case METERS_UP:
                    case METERS_DOWN:
                        unitSpinner.setAdapter(DistanceAdapter());
                        lapLengthEdit.setText("100");
                        unitSpinner.setSelection(1);
                        break;
                }
                LoadLaps();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        LoadLaps(activity, root, listViews, workout, samples, LapStatistics.LapCriterion.DISTANCE, 1000);

        return l;
    }

    private void LoadLaps()
    {
        double lapLength = Integer.parseInt(lapLengthEdit.getText().toString());
        int selectedUnit = unitSpinner.getSelectedItemPosition();
        LapStatistics.LapCriterion criterion = LapStatistics.LapCriterion.values()[typeSpinner.getSelectedItemPosition()];
        double actualDist = getNormalizedLapLength(criterion, selectedUnit, lapLength);
        LoadLaps(activity, root, listViews, workout, samples, criterion, actualDist);
    }

    private double getNormalizedLapLength(LapStatistics.LapCriterion criterion, int selectedUnit, double lapLength)
    {
        switch (criterion)
        {
            case TIME: // for time like units, use as standard seconds
                break;
            case DISTANCE: // For Distance (like) cases, transform to standard "meters"
            case METERS_UP:
            case METERS_DOWN:
                if(selectedUnit==0) // km/miles/...
                    lapLength = distanceUnitUtils.getDistanceUnitSystem().getMetersFromLongUnit(lapLength);
                else if(selectedUnit==1) // m/yd/...
                    lapLength = distanceUnitUtils.getDistanceUnitSystem().getMetersFromShortUnit(lapLength);
                else if(selectedUnit==2) // #
                    lapLength = workout.length/lapLength; // Transform num laps to simple distance criterion
                break;
        }
        return lapLength;
    }

    private void LoadLaps(Activity activity, ViewGroup root, ViewGroup list, Workout workout, List<WorkoutSample> samples, LapStatistics.LapCriterion criterion, double lapLength) {
        list.removeAllViews();
        ArrayList<LapStatistics.LapInfo> laps = LapStatistics.CreateLapList(workout, samples, criterion, lapLength);
        ArrayList<View> lapViews = CreateLapViews(activity, root, workout, laps);
        for (View lapView : lapViews) {
            list.addView(lapView);
        }
    }
}
