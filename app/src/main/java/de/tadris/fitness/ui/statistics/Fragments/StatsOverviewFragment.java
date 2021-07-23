package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;

import androidx.fragment.app.Fragment;

import de.tadris.fitness.R;

public class StatsOverviewFragment extends StatsFragment {

    public StatsOverviewFragment(Context ctx) {
        super(R.layout.fragment_stats_overview, ctx);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_overview_title);
    }
}
