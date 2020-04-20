package de.tadris.fitness.util;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.util.unit.DistanceUnitUtils;
import de.tadris.fitness.util.unit.TimeFormatter;

public class SectionViewHelper {

    private Activity activity;
    private ViewGroup root;
    private Workout workout;
    private List<WorkoutSample> samples;
    private ViewGroup listViews;
    private Spinner unitSpinner;
    private EditText lengthEdit;
    private Spinner typeSpinner;
    private TextView tableHeaderSelected;
    private DistanceUnitUtils distanceUnitUtils;

    private List<String> distanceUnits() {
        List<String> distUnits = new ArrayList<>();
        distUnits.add(distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit());
        distUnits.add(distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit());
        distUnits.add("#");
        return distUnits;
    }

    private List<String> timeUnits() {
        List<String> timeUnits = new ArrayList<>();
        timeUnits.add("h");
        timeUnits.add("min");
        timeUnits.add("s");
        timeUnits.add("#");
        return timeUnits;
    }

    private int getDefaultValueForTime(int unit) {
        switch (unit) {
            case 0: // h
                return 1;
            case 1: // min
                return 5;
            case 2: // s
                return 30;
            case 3: // #
            default:
                return 5;
        }
    }

    private int getDefaultValueForDistance(int unit) {
        switch (unit) {
            case 0: // km / miles / ...
                return 1;
            case 1: // m / yd / ...
                return 100;
            case 2: // #
            default:
                return 5;
        }
    }

    private ArrayAdapter<String> getDistanceAdapter(Activity activity) {
        return new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, distanceUnits());
    }

    private ArrayAdapter<String> getTimeAdapter(Activity activity) {
        return new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, timeUnits());
    }

    private ArrayList<View> createSectionViews(Activity activity, ViewGroup root, SectionCreator.SectionCriterion criterion, List<SectionCreator.Section> sections) {

        ArrayList<View> sectionViews = new ArrayList<>();
        boolean switchRows = true;
        double accumulatedCriterionValue=0;

        double worstPace = 0;
        for (SectionCreator.Section section: sections) {
            if(section.worst){
                worstPace = section.getPace();
                break;
            }
        }

        for (int i = 0; i < sections.size(); i++) {
            SectionCreator.Section section = sections.get(i);
            View sectionEntry = activity.getLayoutInflater().inflate(R.layout.section_entry, root, false);
            View progressBg = sectionEntry.findViewById(R.id.progress1);
            if (switchRows) {
                sectionEntry.setBackgroundColor(activity.getResources().getColor(R.color.lineHighlight));
                sectionEntry.getBackground().setAlpha(127);
            }
            switchRows = !switchRows;

            setProgress(sectionEntry, (float)(section.getPace()/worstPace));

            if (section.best) {
                progressBg.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
                progressBg.getBackground().setAlpha(63);
            }
            if (section.worst) {
                progressBg.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                progressBg.getBackground().setAlpha(63);
            }
            TextView dist = sectionEntry.findViewById(R.id.sectionDist);
            TextView text = sectionEntry.findViewById(R.id.sectionTime);
            TextView mDown = sectionEntry.findViewById(R.id.sectionDescent);
            TextView mUp = sectionEntry.findViewById(R.id.sectionAscent);
            TextView pace = sectionEntry.findViewById(R.id.sectionPace);
            TextView criterionText = sectionEntry.findViewById(R.id.sectionCrit);

            dist.setText(String.valueOf(roundMeterToKilometer(section.dist))+distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit());
            text.setText(TimeFormatter.formatDuration((long)section.time));
            mDown.setText(String.valueOf(Math.round(section.descent))+distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit());
            mUp.setText(String.valueOf(Math.round(section.ascent))+distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit());

            pace.setText(TimeFormatter.formatDuration(section.getPace()));


            switch (criterion)
            {
                case ASCENT:
                    accumulatedCriterionValue += section.ascent;
                    criterionText.setText(String.valueOf(Math.round(accumulatedCriterionValue)));
                    sectionEntry.findViewById(R.id.ascentLayout).setVisibility(View.GONE);
                    break;
                case DESCENT:
                    accumulatedCriterionValue += section.descent;
                    criterionText.setText(String.valueOf(Math.round(accumulatedCriterionValue)));
                    sectionEntry.findViewById(R.id.descentLayout).setVisibility(View.GONE);
                    break;
                case TIME:
                    accumulatedCriterionValue += section.time;
                    criterionText.setText(TimeFormatter.formatDuration((long)accumulatedCriterionValue));
                    sectionEntry.findViewById(R.id.timeLayout).setVisibility(View.GONE);
                    break;
                case DISTANCE:
                default:
                    accumulatedCriterionValue += section.dist;
                    criterionText.setText(String.valueOf(roundMeterToKilometer(accumulatedCriterionValue)));
                    sectionEntry.findViewById(R.id.distLayout).setVisibility(View.GONE);
                    break;
            }

            sectionViews.add(sectionEntry);
        }
        return sectionViews;
    }

    double roundMeterToKilometer(double distance)
    {
        double distInKm = distance / (double) 1000;
        return Math.round(distanceUnitUtils.getDistanceUnitSystem().getDistanceFromKilometers(distInKm) * 100) / 100.0;
    }

    private void setProgress(View sectionEntry, float percentage)
    {
        View v1 = sectionEntry.findViewById(R.id.progress1);
        v1.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                percentage*8));
        View v2 = sectionEntry.findViewById(R.id.progress2);
        v2.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1-percentage)*8 + 6));
    }

    public ViewGroup createSectionsView(Activity activity, ViewGroup root, Workout workout, List<WorkoutSample> samples) {
        this.activity = activity;
        this.root = root;
        this.workout = workout;
        this.samples = samples;

        ViewGroup viewSectionsList = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.view_sections_list, root, false);

        listViews = viewSectionsList.findViewById(R.id.section_list);
        unitSpinner = viewSectionsList.findViewById(R.id.sectionLengthUnit);
        lengthEdit = viewSectionsList.findViewById(R.id.sectionLengthEdit);
        typeSpinner = viewSectionsList.findViewById(R.id.sectionTypeSpinner);
        tableHeaderSelected = viewSectionsList.findViewById(R.id.selectedCriterion);
        distanceUnitUtils = Instance.getInstance(activity).distanceUnitUtils;

        lengthEdit.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                lengthEdit.clearFocus();
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(lengthEdit.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        lengthEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                loadSections();
            }
        });

        unitSpinner.setAdapter(getDistanceAdapter(activity));
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SectionCreator.SectionCriterion criterion = SectionCreator.SectionCriterion.values()[typeSpinner.getSelectedItemPosition()];
                switch (criterion) {
                    case TIME:
                        lengthEdit.setText(String.valueOf(getDefaultValueForTime(i)));
                        break;
                    case DISTANCE: // For Distance (like) cases, transform to standard "meters"
                    case ASCENT:
                    case DESCENT:
                        lengthEdit.setText(String.valueOf(getDefaultValueForDistance(i)));
                        break;
                }

                loadSections();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        typeSpinner.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, SectionCreator.SectionCriterion.getStringRepresentations(activity.getApplicationContext())));
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SectionCreator.SectionCriterion criterion = SectionCreator.SectionCriterion.values()[i];
                tableHeaderSelected.setText((String)typeSpinner.getSelectedItem());
                switch (criterion) {
                    case TIME:
                        unitSpinner.setAdapter(getTimeAdapter(activity));
                        unitSpinner.setSelection(1); // default: min
                        break;
                    case DISTANCE:
                        unitSpinner.setAdapter(getDistanceAdapter(activity));
                        unitSpinner.setSelection(0); // default: km / miles
                        break;
                    case ASCENT:
                    case DESCENT:
                        unitSpinner.setAdapter(getDistanceAdapter(activity));
                        unitSpinner.setSelection(1); // default : m
                        break;
                }
                loadSections();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        return viewSectionsList;
    }

    private double getNormalizedLength(SectionCreator.SectionCriterion criterion, int selectedUnit, double length) {
        switch (criterion) {
            case TIME: // for time like units, use as standard microseconds
                if (selectedUnit == 0) // h
                    length *= 3600000;
                else if (selectedUnit == 1) // min
                    length *= 60000;
                else if (selectedUnit == 2) // s
                    length *= 1000;
                else if (selectedUnit == 3) // #
                    length = workout.duration / length;
                break;
            case DISTANCE: // For Distance (like) cases, transform to standard "meters"
            case ASCENT:
            case DESCENT:
                if (selectedUnit == 0) // km/miles/...
                    length = distanceUnitUtils.getDistanceUnitSystem().getMetersFromLongUnit(length);
                else if (selectedUnit == 1) // m/yd/...
                    length = distanceUnitUtils.getDistanceUnitSystem().getMetersFromShortUnit(length);
                else if (selectedUnit == 2) // #
                    if (criterion == SectionCreator.SectionCriterion.DISTANCE)
                        length = workout.length / length; // Transform num sections to simple distance criterion
                    else if (criterion == SectionCreator.SectionCriterion.ASCENT)
                        length = workout.ascent / length;
                    else if (criterion == SectionCreator.SectionCriterion.DESCENT)
                        length = workout.descent / length;
                break;
        }
        return length;
    }

    private void loadSections() {
        double length = Integer.parseInt(lengthEdit.getText().toString());
        if (length > 0) {
            int selectedUnit = unitSpinner.getSelectedItemPosition();
            SectionCreator.SectionCriterion criterion = SectionCreator.SectionCriterion.values()[typeSpinner.getSelectedItemPosition()];
            double actualDist = getNormalizedLength(criterion, selectedUnit, length);
            loadSections(activity, root, listViews, samples, criterion, actualDist);
        }
    }

    private void loadSections(Activity activity, ViewGroup root, ViewGroup list, List<WorkoutSample> samples, SectionCreator.SectionCriterion criterion, double length) {
        list.removeAllViews();
        ArrayList<SectionCreator.Section> sections = SectionCreator.createSectionList(samples, criterion, length);
        ArrayList<View> sectionViews = createSectionViews(activity, root, criterion, sections);
        for (View v : sectionViews) {
            list.addView(v);
        }
    }
}
