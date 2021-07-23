package de.tadris.fitness.ui.statistics.Fragments;

import androidx.fragment.app.Fragment;

import de.tadris.fitness.R;

public class StatsOverviewFragment extends Fragment implements StatsFragment {

    public StatsOverviewFragment() {
        super(R.layout.fragment_stats_overview);
    }

    @Override
    public String getTitle() {
        return getString(R.string.stats_overview_title);
    }
}
