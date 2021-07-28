package de.tadris.fitness.util.charts.formatter;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.concurrent.TimeUnit;

public class MinutesFormatter extends ValueFormatter {

    public MinutesFormatter() {
    }

    /**
     * Expects time in minutes
     * @param value time
     * @return
     */
    @Override
    public String getFormattedValue(float value) {
        long s = TimeUnit.MINUTES.toSeconds((long) value);
        return de.tadris.fitness.util.unit.TimeFormatter.formatDuration(s);
    }
}
