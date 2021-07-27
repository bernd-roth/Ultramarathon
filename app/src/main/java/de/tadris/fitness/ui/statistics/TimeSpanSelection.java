package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;

public class TimeSpanSelection extends LinearLayout {
    private Spinner aggregationSpanSpinner;
    private ArrayAdapter<String> aggregationSpanArrayAdapter;

    private Spinner aggregationSpanInstanceSpinner;
    private ArrayAdapter<String> aggregationSpanInstanceArrayAdapter;

    private AggregationSpan currentAggregationSpan;

    private ArrayList<OnTimeSpanSelectionListener> listeners;

    public TimeSpanSelection(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        listeners = new ArrayList<>();

        inflate(context, R.layout.view_time_span_selection, this);

        // Load views
        aggregationSpanSpinner = findViewById(R.id.aggregationSpanSpinner);
        aggregationSpanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                specifyAggregationSpan();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //aggregationSpanInstanceSpinner = findViewById(R.id.aggregationSpanInstanceSpinner);

        loadAggregationSpanEntries();
    }

    private void notifyListener() {
        for (OnTimeSpanSelectionListener listener : listeners) {
            listener.onTimeSpanChanged(currentAggregationSpan);
        }
    }

    private void loadAggregationSpanEntries() {
        ArrayList<String> aggregationSpanStrings = new ArrayList<>();
        for (AggregationSpan aggregationSpan : AggregationSpan.values()) {
            if (aggregationSpan != AggregationSpan.SINGLE) {
                aggregationSpanStrings.add(getContext().getString(aggregationSpan.title));
            }
        }

        aggregationSpanArrayAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.support_simple_spinner_dropdown_item, aggregationSpanStrings);

        aggregationSpanSpinner.setAdapter(aggregationSpanArrayAdapter);

        specifyAggregationSpan();
    }

    private void specifyAggregationSpan() {
        String selectedString = aggregationSpanArrayAdapter.getItem(aggregationSpanSpinner.getSelectedItemPosition());

        for (AggregationSpan aggregationSpan : AggregationSpan.values()) {
            if (selectedString.equals(getContext().getString(aggregationSpan.title))) {
                currentAggregationSpan = aggregationSpan;
                notifyListener();
            }
        }
    }

    public void setAggregationSpan(AggregationSpan aggregationSpan) {
        currentAggregationSpan = aggregationSpan;
        aggregationSpanSpinner.setSelection(aggregationSpanArrayAdapter.getPosition(getContext().getString(aggregationSpan.title)), true);
    }

    public AggregationSpan getAggregationSpan() {
        return currentAggregationSpan;
    }

    public void addOnTimeSpanSelectionListener(OnTimeSpanSelectionListener listener) {
        listeners.add(listener);
    }

    public void removeOnTimeSpanSelectionListener(OnTimeSpanSelectionListener listener) {
        listeners.remove(listener);
    }

    public interface OnTimeSpanSelectionListener {
        void onTimeSpanChanged(AggregationSpan aggregationSpan);
    }
}

