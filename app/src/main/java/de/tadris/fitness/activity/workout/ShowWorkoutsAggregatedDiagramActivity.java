package de.tadris.fitness.activity.workout;

import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.activity.FitoTrackActivity;

import static java.time.LocalDate.now;

public class ShowWorkoutsAggregatedDiagramActivity extends FitoTrackActivity {

    private LineChart chart;
    private TextView tvX, tvY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workouts_aggregated);

        chart = findViewById(R.id.aggregatedWorkoutsAverageSpeed);

        ArrayList<Entry> values = new ArrayList<>();

        values.add(new Entry(0, 7));
        values.add(new Entry(1, 8));
        values.add(new Entry(2, 10));
        values.add(new Entry(3, 12));

        LineDataSet set1;

        set1 = new LineDataSet(values, "Average Running Speed");
        set1.enableDashedLine(10f, 5f, 0f);
        set1.setDrawFilled(true);
        set1.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return chart.getAxisLeft().getAxisMinimum();
            }
        });

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);
    }

}