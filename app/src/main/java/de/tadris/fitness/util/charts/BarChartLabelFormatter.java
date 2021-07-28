package de.tadris.fitness.util.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Arrays;

public class BarChartLabelFormatter extends ValueFormatter {
    private final String[] labels;
    private final float[] positions;

    public BarChartLabelFormatter(String[] labels, float[] positions) {
        this.labels = labels;
        Arrays.sort(positions);
        this.positions = positions;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int i=0;
        while(i < positions.length)
        {
            if(value<=positions[i])
                break;
            i++;
        }
        i = i < positions.length ? i : positions.length-1;
        return labels[i];
    }
}
