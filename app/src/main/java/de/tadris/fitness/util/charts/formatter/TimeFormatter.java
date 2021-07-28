package de.tadris.fitness.util.charts.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TimeFormatter extends ValueFormatter {
    TimeUnit input;
    boolean dispSecs, dispMins, dispHours;

    public TimeFormatter(TimeUnit input) {
        this(input, true, true, true);
    }

    public TimeFormatter(TimeUnit input, boolean dispSecs, boolean dispMins, boolean dispHours) {
        this.input = input;
        this.dispSecs = dispSecs;
        this.dispMins = dispMins;
        this.dispHours = dispHours;
    }

    /**
     * Expects time in milliseconds
     * @param value time
     * @return
     */
    @Override
    public String getFormattedValue(float value) {
        long s = input.toSeconds((long) value);
        if(dispSecs)
            return de.tadris.fitness.util.unit.TimeFormatter.formatDuration(s);
        else if (dispHours)
            return de.tadris.fitness.util.unit.TimeFormatter.formatHoursMinutes(s);
        else
            return de.tadris.fitness.util.unit.TimeFormatter.formatMinutesOnly(s);
        // TODO: Implement rest
    }
}
