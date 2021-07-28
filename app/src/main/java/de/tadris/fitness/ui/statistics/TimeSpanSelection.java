package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.util.statistics.AggregationSpanInstance;

public class TimeSpanSelection extends LinearLayout {
    private Spinner aggregationSpanSpinner;
    private ArrayAdapter<String> aggregationSpanArrayAdapter;

    long firstInstance;
    long lastInstance;

    private NumberPicker aggregationSpanInstancePicker;

    private AggregationSpanInstance selectedInstance;

    private ArrayList<OnTimeSpanSelectionListener> listeners;

    public TimeSpanSelection(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        listeners = new ArrayList<>();
        firstInstance = 0;
        lastInstance = Long.MAX_VALUE;

        inflate(context, R.layout.view_time_span_selection, this);


        // Load views
        aggregationSpanSpinner = findViewById(R.id.aggregationSpanSpinner);
        loadAggregationSpanEntries();
        aggregationSpanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                specifyAggregationSpanInstance();
                updateLimits();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        aggregationSpanInstancePicker = findViewById(R.id.aggregationSpanInstancePicker);
        // Set initial selected instance
        setAggregationSpanInstance(new AggregationSpanInstance(GregorianCalendar.getInstance(),
                AggregationSpan.YEAR));
        aggregationSpanInstancePicker.setFormatter(selectedInstance);
        aggregationSpanInstancePicker.setOnValueChangedListener((numberPicker, i, i1) -> specifyAggregationSpanInstance());

    }

    private void notifyListener() {
        for (OnTimeSpanSelectionListener listener : listeners) {
            listener.onTimeSpanChanged(selectedInstance);
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
    }

    private void specifyAggregationSpanInstance() {
        String selectedString = aggregationSpanArrayAdapter.getItem(aggregationSpanSpinner.getSelectedItemPosition());

        for (AggregationSpan aggregationSpan : AggregationSpan.values()) {
            if (selectedString.equals(getContext().getString(aggregationSpan.title))) {
                selectedInstance.setInstance(aggregationSpanInstancePicker.getValue(), aggregationSpan);
                notifyListener();
                break;
            }
        }
    }

    public void setAggregationSpanInstance(AggregationSpanInstance aggregationSpanInstance) {
        selectedInstance = aggregationSpanInstance;
        aggregationSpanSpinner.setSelection(aggregationSpanArrayAdapter.getPosition(
                getContext().getString(selectedInstance.getAggregationSpan().title)), true);
        updateLimits();
        aggregationSpanInstancePicker.setValue((int) selectedInstance.getIndex());
    }

    public AggregationSpanInstance getAggregationSpanInstance() {
        return selectedInstance;
    }

    public void addOnTimeSpanSelectionListener(OnTimeSpanSelectionListener listener) {
        listeners.add(listener);
    }

    public void removeOnTimeSpanSelectionListener(OnTimeSpanSelectionListener listener) {
        listeners.remove(listener);
    }

    public void setLimits(long lowerTimeBound, long upperTimeBound) {
        this.firstInstance = lowerTimeBound;
        this.lastInstance = upperTimeBound;
        updateLimits();
    }

    public long getLowerTimeBound() {
        return this.firstInstance;
    }

    public long getUpperTimeBound() {
        return this.lastInstance;
    }

    private void updateLimits() {
        int min = (int) AggregationSpanInstance.mapInstanceToIndex(firstInstance, selectedInstance.getAggregationSpan());
        int max = (int) AggregationSpanInstance.mapInstanceToIndex(lastInstance, selectedInstance.getAggregationSpan());
        if (min != aggregationSpanInstancePicker.getMinValue()) {
            aggregationSpanInstancePicker.setMinValue(min);
        }
        if (max != aggregationSpanInstancePicker.getMaxValue()) {
            aggregationSpanInstancePicker.setMaxValue(max);
        }
    }

    public interface OnTimeSpanSelectionListener {
        void onTimeSpanChanged(AggregationSpanInstance aggregationSpanInstance);
    }
}

