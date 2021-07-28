package de.tadris.fitness.util.charts.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TimeFormatter extends ValueFormatter {

    public TimeFormatter() {
    }

    /**
     * Expects time in milliseconds
     * @param value time
     * @return
     */
    @Override
    public String getFormattedValue(float value) {
        long s = TimeUnit.MILLISECONDS.toSeconds((long) value);
        return de.tadris.fitness.util.unit.TimeFormatter.formatDuration(s);
    }
}
