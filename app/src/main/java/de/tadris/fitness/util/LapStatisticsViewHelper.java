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

    protected DistanceUnitUtils distanceUnitUtils;

    private static ArrayList<View> CreateLapViews(Activity activity, ViewGroup root, Workout workout, List<LapStatistics.LapInfo> laps) {

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

            dist.setText(Math.round(lapInfo.dist / 10) / 100.0 + "");
            text.setText(TimeFormatter.formatDuration(lapInfo.time));
            mDown.setText(Math.round(lapInfo.metersDown) + "");
            mUp.setText(Math.round(lapInfo.metersUp) + "");
            lapViews.add(laptimeEntry);
        }
        return lapViews;
    }

    Activity activity;
    ViewGroup root;
    Workout workout;
    List<WorkoutSample> samples;
    ViewGroup listViews;
    TextView unitView;

    public ViewGroup CreateLapStatisticsView(Activity activity, ViewGroup root, Workout workout, List<WorkoutSample> samples) {
        this.activity = activity;
        this.root = root;
        this.workout = workout;
        this.samples = samples;
        ViewGroup l = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.laptimes, root, false);
        listViews = l.findViewById(R.id.laplist);
        unitView = l.findViewById(R.id.lapLengthUnit);
        DistanceUnitUtils distanceUnitUtils = Instance.getInstance(activity).distanceUnitUtils;
        unitView.setText(distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit());

        EditText lapLengthEdit = (EditText) l.findViewById(R.id.lapLengthEdit);
        Spinner typeSpinner = (Spinner) l.findViewById(R.id.lapTypeSpinner);

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
                int lapLength = Integer.parseInt(lapLengthEdit.getText().toString());
                int distInMeters = (int)distanceUnitUtils.getDistanceUnitSystem().getMetersFromUnit(lapLength);
                LapStatistics.LapCriterion criterion = LapStatistics.LapCriterion.values()[typeSpinner.getSelectedItemPosition()];
                LoadLaps(activity, root, listViews, workout, samples, criterion, distInMeters);
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
                        unitView.setText(R.string.timeSecondsShort);
                        lapLengthEdit.setText("1");
                        break;
                    case DISTANCE:
                        unitView.setText(distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit());
                        lapLengthEdit.setText("1000");
                        break;
                    case NUM_LAPS:
                        unitView.setText("#");
                        lapLengthEdit.setText("5");
                        break;
                    case METERS_UP:
                    case METERS_DOWN:
                        unitView.setText(distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit());
                        lapLengthEdit.setText("100");
                        break;
                }
                int lapLength = Integer.parseInt(lapLengthEdit.getText().toString());
                int distInMeters = (int)distanceUnitUtils.getDistanceUnitSystem().getMetersFromUnit(lapLength);
                LoadLaps(activity, root, listViews, workout, samples, criterion, distInMeters);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        LoadLaps(activity, root, listViews, workout, samples, LapStatistics.LapCriterion.DISTANCE, 1000);

        return l;
    }

    private void LoadLaps(Activity activity, ViewGroup root, ViewGroup list, Workout workout, List<WorkoutSample> samples, LapStatistics.LapCriterion criterion, int lapLength) {
        list.removeAllViews();
        ArrayList<LapStatistics.LapInfo> laps = LapStatistics.CreateLapList(workout, samples, criterion, lapLength);
        ArrayList<View> lapViews = CreateLapViews(activity, root, workout, laps);
        for (View lapView : lapViews) {
            list.addView(lapView);
        }
    }
}
