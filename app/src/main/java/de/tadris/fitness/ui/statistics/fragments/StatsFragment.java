package de.tadris.fitness.ui.statistics.fragments;

import androidx.fragment.app.Fragment;

import de.tadris.fitness.ui.FitoTrackActivity;

public abstract class StatsFragment extends Fragment {
    protected FitoTrackActivity context;

    protected StatsFragment(int layoutID, FitoTrackActivity ctx)
    {
        super(layoutID);
        this.context = ctx;
    }
    public abstract String getTitle();
}
