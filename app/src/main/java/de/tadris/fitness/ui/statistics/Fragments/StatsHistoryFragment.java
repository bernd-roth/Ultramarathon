package de.tadris.fitness.ui.statistics.Fragments;

import androidx.fragment.app.Fragment;

import de.tadris.fitness.R;

public class StatsHistoryFragment extends Fragment implements StatsFragment {

    public StatsHistoryFragment() {
        super(R.layout.fragment_stats_history);
    }

    @Override
    public String getTitle() {
        return getString(R.string.stats_history_title);
    }
}