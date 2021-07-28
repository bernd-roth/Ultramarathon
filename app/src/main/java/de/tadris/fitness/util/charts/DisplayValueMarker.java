package de.tadris.fitness.util.charts;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;

import de.tadris.fitness.R;

public class DisplayValueMarker extends MarkerView {

    private TextView tvContent;
    private ValueFormatter formatter;
    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     */
    public DisplayValueMarker(Context context, ValueFormatter formatter) {
        super(context, R.layout.marker_view);
        tvContent = (TextView) findViewById(R.id.marker_text);
        this.formatter = formatter;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvContent.setText("" + formatter.getFormattedValue(e.getY())); // set the entry-value as the display text
        super.refreshContent(e, highlight);
    }
}
