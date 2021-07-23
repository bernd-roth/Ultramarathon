package de.tadris.fitness.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

import de.tadris.fitness.ui.statistics.Fragments.StatsOverviewFragment;

public class StatisticsAdapter extends FragmentStateAdapter {

    public StatisticsAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @Override
    public Fragment createFragment(@NotNull int position) {
        return new StatsOverviewFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
