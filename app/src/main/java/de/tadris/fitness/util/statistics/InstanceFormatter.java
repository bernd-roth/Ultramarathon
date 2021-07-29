package de.tadris.fitness.util.statistics;

import android.widget.NumberPicker;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.tadris.fitness.aggregation.AggregationSpan;

public class InstanceFormatter implements NumberPicker.Formatter {

    public AggregationSpan aggregationSpan;

    public InstanceFormatter(@NotNull AggregationSpan aggregationSpan) {
        this.aggregationSpan = aggregationSpan;
    }

    public static long mapInstanceToIndex(long instance, AggregationSpan aggregationSpan) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(instance);
        aggregationSpan.applyToCalendar(calendar);
        return calendar.getTimeInMillis() / aggregationSpan.spanInterval + 1;
    }

    public static long mapIndexToInstance(long index, AggregationSpan aggregationSpan) {
        return index * aggregationSpan.spanInterval;
    }

    private Calendar mapIndexToCalendar(long index) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(index * aggregationSpan.spanInterval);
        return calendar;
    }

    @Override
    public String format(int i) {
        String formatString;
        SimpleDateFormat format;
        switch (aggregationSpan) {
            case SINGLE:
                formatString = String.valueOf(mapIndexToCalendar(i).toString());
                break;
            case DAY:
                format = new SimpleDateFormat("dd. MMM yyyy");
                formatString = format.format(mapIndexToCalendar(i).getTime());
                break;
            case WEEK:
                format = new SimpleDateFormat("yyyy 'W'w");
                formatString = format.format(mapIndexToCalendar(i).getTime());
                break;
            case MONTH:
                format = new SimpleDateFormat("MMM yyyy");
                formatString = format.format(mapIndexToCalendar(i).getTime());
                break;
            case YEAR:
                format = new SimpleDateFormat("yyyy");
                formatString = format.format(mapIndexToCalendar(i).getTime());
                break;
            default:
                formatString = "-";
        }
        return formatString;
    }
}
