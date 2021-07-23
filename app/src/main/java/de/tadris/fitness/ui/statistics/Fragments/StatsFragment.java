package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;

import androidx.fragment.app.Fragment;

public abstract class StatsFragment extends Fragment {
    protected Context context;

    protected StatsFragment(int layoutID, Context ctx)
    {
        super(layoutID);
        this.context = ctx;
    }
    public abstract String getTitle();
}
