package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.util.statistics.InstanceFormatter;
import de.westnordost.osmapi.user.User;

public class TimeSpanSelection extends LinearLayout {
    private Spinner aggregationSpanSpinner;
    private ArrayAdapter<String> aggregationSpanArrayAdapter;

    private NumberPicker aggregationSpanInstancePicker;

    long firstInstance;
    long lastInstance;
    long selectedInstance;
    AggregationSpan selectedAggregationSpan;
    boolean isInstanceSelectable;
    UserPreferences preferences;

    private InstanceFormatter instanceFormatter;

    private ArrayList<OnTimeSpanSelectionListener> listeners;

    public TimeSpanSelection(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        preferences = new UserPreferences(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TimeSpanSelection);
        firstInstance = array.getInt(R.styleable.TimeSpanSelection_firstInstance, 0);
        lastInstance = array.getInt(R.styleable.TimeSpanSelection_lastInstance, 0);
        selectedInstance = array.getInt(R.styleable.TimeSpanSelection_firstInstance, 0);
        isInstanceSelectable = array.getBoolean(R.styleable.TimeSpanSelection_isInstanceSelectable, true);
        array.recycle();

        if (lastInstance == 0) {
            lastInstance = Long.MAX_VALUE;
        }

        selectedAggregationSpan = preferences.getStatisticsAggregationSpan();
        listeners = new ArrayList<>();
        instanceFormatter = new InstanceFormatter(selectedAggregationSpan);

        inflate(context, R.layout.view_time_span_selection, this);


        // Load views
        aggregationSpanSpinner = findViewById(R.id.aggregationSpanSpinner);
        aggregationSpanInstancePicker = findViewById(R.id.aggregationSpanInstancePicker);
        loadAggregationSpanEntries();

        if (!isInstanceSelectable) {
            findViewById(R.id.aggregationSpanInstancePickerLayout).getLayoutParams().width = 0;
            aggregationSpanInstancePicker.getLayoutParams().width = 0;
        }

        setAggregationSpan(selectedAggregationSpan);
        setInstance(GregorianCalendar.getInstance().getTimeInMillis());

        aggregationSpanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                specifyAggregationSpan();
                updateLimits();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        aggregationSpanInstancePicker.setFormatter(instanceFormatter);
        aggregationSpanInstancePicker.setOnValueChangedListener((numberPicker, i, i1) -> specifyInstance());

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

    private void specifyAggregationSpan() {
        String selectedString = aggregationSpanArrayAdapter.getItem(aggregationSpanSpinner.getSelectedItemPosition());

        for (AggregationSpan aggregationSpan : AggregationSpan.values()) {
            if (selectedString.equals(getContext().getString(aggregationSpan.title))) {
                selectedAggregationSpan = aggregationSpan;
                instanceFormatter.aggregationSpan = aggregationSpan;
                preferences.setStatisticsAggregationSpan(aggregationSpan);
                notifyListener();
                break;
            }
        }
    }

    private void specifyInstance() {
        selectedInstance = InstanceFormatter.mapIndexToInstance(aggregationSpanInstancePicker.getValue(), selectedAggregationSpan);
        notifyListener();
    }

    public AggregationSpan getSelectedAggregationSpan() {
        return selectedAggregationSpan;
    }

    public void loadAggregationSpanFromPreferences()
    {
        setAggregationSpan(preferences.getStatisticsAggregationSpan());
    }

    public long getSelectedInstance() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(selectedInstance);
        selectedAggregationSpan.applyToCalendar(calendar);
        return calendar.getTimeInMillis();
    }

    public void setAggregationSpan(@NotNull AggregationSpan aggregationSpan) {
        selectedAggregationSpan = aggregationSpan;
        aggregationSpanSpinner.setSelection(aggregationSpanArrayAdapter.getPosition(getContext().getString(aggregationSpan.title)), true);
        instanceFormatter.aggregationSpan = aggregationSpan;
        updateLimits();
    }

    public void setInstance(long instance) {
        selectedInstance = instance;
        aggregationSpanInstancePicker.setValue((int) InstanceFormatter.mapInstanceToIndex(instance, selectedAggregationSpan));
    }

    public void setLimits(long firstInstance, long lastInstance) {
        this.firstInstance = firstInstance;
        this.lastInstance = lastInstance;
        updateLimits();
    }

    public long getFirstInstance() {
        return this.firstInstance;
    }

    public long getLastInstance() {
        return this.lastInstance;
    }

    private void updateLimits() {
        int min = (int) InstanceFormatter.mapInstanceToIndex(firstInstance, selectedAggregationSpan);
        min = (min >= 0) ? min : 0;
        int max = (int) InstanceFormatter.mapInstanceToIndex(lastInstance, selectedAggregationSpan);
        max = (max >= 0) ? max : Integer.MAX_VALUE; // Make values safe after cast
        if (min != aggregationSpanInstancePicker.getMinValue()) {
            aggregationSpanInstancePicker.setMinValue(min);
        }
        if (max != aggregationSpanInstancePicker.getMaxValue()) {
            aggregationSpanInstancePicker.setMaxValue(max);
        }
    }

    public void addOnTimeSpanSelectionListener(OnTimeSpanSelectionListener listener) {
        listeners.add(listener);
    }

    public void removeOnTimeSpanSelectionListener(OnTimeSpanSelectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListener() {
        for (OnTimeSpanSelectionListener listener : listeners) {
            listener.onTimeSpanChanged(getSelectedAggregationSpan(), getSelectedInstance());
        }
    }

    public interface OnTimeSpanSelectionListener {
        void onTimeSpanChanged(AggregationSpan aggregationSpan, long instance);
    }
}

