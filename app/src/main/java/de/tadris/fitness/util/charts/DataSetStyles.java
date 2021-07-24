package de.tadris.fitness.util.charts;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.LineDataSet;

import de.tadris.fitness.R;

public class DataSetStyles {
    public static CandleDataSet applyDefaultCandleStyle(Context ctx, CandleDataSet candleDataSet) {
        candleDataSet.setShadowColor(Color.GRAY);
        candleDataSet.setShadowWidth(2f);
        candleDataSet.setNeutralColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
        return candleDataSet;
    }

    public static LineDataSet applyBackgroundLineStyle(Context ctx, LineDataSet lineDataSet) {
        lineDataSet.setColor(ContextCompat.getColor(ctx, R.color.stats_background_line));
        lineDataSet.setCircleColor(Color.TRANSPARENT);
        lineDataSet.setCircleHoleColor(Color.TRANSPARENT);
        lineDataSet.setValueTextColor(Color.TRANSPARENT);
        return lineDataSet;
    }
}
