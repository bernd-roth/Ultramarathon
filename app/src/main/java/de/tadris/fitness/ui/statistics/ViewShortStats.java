package de.tadris.fitness.ui.statistics;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import de.tadris.fitness.R;
import de.tadris.fitness.ui.adapter.ShortStatsAdapter;

public class ViewShortStats extends ConstraintLayout {
    ViewPager2 myViewPager2;
    ShortStatsAdapter adapter;

    public ViewShortStats(@NonNull Context context) {
        super(context);
        inflate(getContext(), R.layout.view_short_stats, this);

        myViewPager2 = findViewById(R.id.viewpager_short_stats);
        adapter = new ShortStatsAdapter(this.getContext());
        myViewPager2.setAdapter(adapter);
    }
}
