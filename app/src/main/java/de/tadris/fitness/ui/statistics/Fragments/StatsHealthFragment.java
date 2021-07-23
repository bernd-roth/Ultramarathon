package de.tadris.fitness.ui.statistics.Fragments;

import androidx.fragment.app.Fragment;

import de.tadris.fitness.R;

public class StatsHealthFragment extends Fragment implements StatsFragment {

    public StatsHealthFragment() {
        super(R.layout.fragment_stats_health);
    }

    @Override
    public String getTitle() {
        return getString(R.string.stats_health_title);
    }
}
