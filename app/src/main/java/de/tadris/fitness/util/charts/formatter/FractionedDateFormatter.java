package de.tadris.fitness.util.charts.formatter;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataTypes;

public class FractionedDateFormatter extends ValueFormatter {
    Context ctx;
    SimpleDateFormat format;
    float stats_time_factor;
    AggregationSpan span;

    public FractionedDateFormatter(Context ctx, AggregationSpan span)
    {
        setAggregationSpan(span);
        this.span = span;
        this.ctx = ctx;
        TypedValue stats_time_factor = new TypedValue();
        ctx.getResources().getValue(R.dimen.stats_time_factor, stats_time_factor, true);
        this.stats_time_factor = stats_time_factor.getFloat();
    }

    public void setAggregationSpan(AggregationSpan span)
    {
        format = span.dateFormat;
    }

    @Override
    public String getFormattedValue(float value) {
        return format.format(new Date((long) (value * stats_time_factor + span.spanInterval)));
    }
}
