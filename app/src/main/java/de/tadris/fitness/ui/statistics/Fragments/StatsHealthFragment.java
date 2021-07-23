package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;

import androidx.fragment.app.Fragment;

import de.tadris.fitness.R;

public class StatsHealthFragment extends StatsFragment {

    public StatsHealthFragment(Context ctx) {
        super(R.layout.fragment_stats_health, ctx);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_health_title);
    }
}
