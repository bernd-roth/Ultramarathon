package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;

public class TimeSpanSelection extends LinearLayout {
    Spinner aggregationSpanSpinner;
    Spinner aggregationSpanInstanceSpinner;

    public TimeSpanSelection(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_time_span_selection, this);

        // Load views
        aggregationSpanSpinner = findViewById(R.id.aggregationSpanSpinner);
        //aggregationSpanInstanceSpinner = findViewById(R.id.aggregationSpanInstanceSpinner);

        loadAggregationSpanEntries();
    }

    private void loadAggregationSpanEntries() {
        ArrayList<String> aggregationSpanStrings = new ArrayList<>();
        for (AggregationSpan aggregationSpan : AggregationSpan.values()) {
            aggregationSpanStrings.add(getContext().getString(aggregationSpan.title));
        }

        ArrayAdapter<String> aggregationSpanArrayAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.support_simple_spinner_dropdown_item, aggregationSpanStrings);

        aggregationSpanSpinner.setAdapter(aggregationSpanArrayAdapter);
    }

}
