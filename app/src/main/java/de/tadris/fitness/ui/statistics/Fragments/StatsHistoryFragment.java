package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;

import androidx.fragment.app.Fragment;

import de.tadris.fitness.R;

public class StatsHistoryFragment extends StatsFragment {

    public StatsHistoryFragment(Context ctx) {
        super(R.layout.fragment_stats_history, ctx);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_history_title);
    }
}