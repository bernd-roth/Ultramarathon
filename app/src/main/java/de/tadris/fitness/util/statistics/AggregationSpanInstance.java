package de.tadris.fitness.util.statistics;

import android.widget.NumberPicker;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.tadris.fitness.aggregation.AggregationSpan;

public class AggregationSpanInstance implements NumberPicker.Formatter {

    private long index;
    private AggregationSpan aggregationSpan;

    public AggregationSpanInstance(@NotNull Calendar calendar, @NotNull AggregationSpan aggregationSpan) {
        setInstance(calendar, aggregationSpan);
    }

    public GregorianCalendar getInstance() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(index * aggregationSpan.spanInterval);
        aggregationSpan.applyToCalendar(calendar);
        return calendar;
    }

    @NotNull
    private GregorianCalendar getInstance(long index) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(index * aggregationSpan.spanInterval);
        aggregationSpan.applyToCalendar(calendar);
        return calendar;
    }

    public static long mapInstanceToIndex(long instance, AggregationSpan aggregationSpan) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(instance);
        aggregationSpan.applyToCalendar(calendar);
        return calendar.getTimeInMillis() / aggregationSpan.spanInterval + 1;
    }

    public void setInstance(@NotNull Calendar calendar, @NotNull AggregationSpan aggregationSpan) {
        index = mapInstanceToIndex(calendar.getTimeInMillis(), aggregationSpan);
        this.aggregationSpan = aggregationSpan;
    }

    public void setInstance(@NotNull long index, @NotNull AggregationSpan aggregationSpan) {
        this.index = index;
        this.aggregationSpan = aggregationSpan;
    }

    public AggregationSpan getAggregationSpan() {
        return aggregationSpan;
    }

    public long getIndex() {
        return index;
    }

    @Override
    public String format(int i) {
        String formatString;
        SimpleDateFormat format;
        switch (aggregationSpan) {
            case SINGLE:
                formatString = String.valueOf(getInstance(i).toString());
                break;
            case DAY:
                format = new SimpleDateFormat("dd. MMM yyyy");
                formatString = format.format(getInstance(i).getTime());
                break;
            case WEEK:
                format = new SimpleDateFormat("yyyy 'W'w");
                formatString = format.format(getInstance(i).getTime());
                break;
            case MONTH:
                format = new SimpleDateFormat("MMM yyyy");
                formatString = format.format(getInstance(i).getTime());
                break;
            case YEAR:
                format = new SimpleDateFormat("yyyy");
                formatString = format.format(getInstance(i).getTime());
                break;
            default:
                formatString = "-";
        }
        return formatString;
    }
}
