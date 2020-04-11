package de.tadris.fitness.util;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.R;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.util.unit.TimeFormatter;

public class LapStatisticsViewHelper implements AdapterView.OnItemSelectedListener {
    private static ArrayList<View> CreateLapViews(Activity activity, ViewGroup root, List<LapStatistics.LaptimeInfo> laps)
    {
        ArrayList<View> lapViews = new ArrayList<>();
        boolean switchRows = false;
        for (LapStatistics.LaptimeInfo lapInfo:laps)
        {
            View laptimeEntry = (View) activity.getLayoutInflater().inflate(R.layout.laptime_entry, root, false);
            if(switchRows)
                laptimeEntry.setBackgroundColor(activity.getResources().getColor(R.color.lineHighlight));
            switchRows = !switchRows;

            if(lapInfo.fastest)
            {
                laptimeEntry.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
                laptimeEntry.getBackground().setAlpha(100);
            }
            if(lapInfo.slowest)
            {
                laptimeEntry.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                laptimeEntry.getBackground().setAlpha(100);
            }
            TextView dist = laptimeEntry.findViewById(R.id.laptimeDist);
            TextView text = laptimeEntry.findViewById(R.id.laptimeText);
            TextView mDown = laptimeEntry.findViewById(R.id.laptimeMetersDown);
            TextView mUp = laptimeEntry.findViewById(R.id.laptimeMetersUp);

            dist.setText(Math.round(lapInfo.dist/10)/100.0+"");
            text.setText(TimeFormatter.formatDuration(lapInfo.time));
            mDown.setText(lapInfo.metersDown+"");
            mUp.setText(lapInfo.metersUp+"");
            lapViews.add(laptimeEntry);
        }
        return lapViews;
    }

    Activity activity;
    ViewGroup root;
    List<WorkoutSample> samples;
    ViewGroup listViews;
    public ViewGroup CreateLapStatisticsView(Activity activity, ViewGroup root, List<WorkoutSample> samples)
    {
        this.activity = activity;
        this.root = root;
        this.samples = samples;
        ViewGroup l = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.laptimes, root, false);
//        Spinner typeSpinner = (Spinner) l.findViewById(R.id.lapTypeSpinner);
//        typeSpinner.setAdapter(new ArrayAdapter<LapStatistics.LapCriterion>(activity, android.R.layout.simple_list_item_2, LapStatistics.LapCriterion.values()));

        Spinner lapLengthSpinner = (Spinner) l.findViewById(R.id.lapLengthSpinner);
        lapLengthSpinner.setAdapter(new ArrayAdapter<Integer>(activity, android.R.layout.simple_spinner_item, LapStatistics.Distances()));
        lapLengthSpinner.setSelection(2);
        lapLengthSpinner.setOnItemSelectedListener(this);
        listViews = l.findViewById(R.id.laplist);
        LoadLaps(activity, root, listViews, samples, LapStatistics.LapCriterion.DISTANCE, 1000);

        return l;
    }

    private void LoadLaps(Activity activity, ViewGroup root, ViewGroup list, List<WorkoutSample> samples, LapStatistics.LapCriterion criterion, int lapLength)
    {
        list.removeAllViews();
        ArrayList<LapStatistics.LaptimeInfo> laps = LapStatistics.CreateLapList(samples, criterion, lapLength);
        ArrayList<View> lapViews = CreateLapViews(activity, root, laps);
        for (View lapView: lapViews)
        {
            list.addView(lapView);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        LoadLaps(activity, root, listViews, samples, LapStatistics.LapCriterion.DISTANCE, LapStatistics.Distances().get(pos));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
