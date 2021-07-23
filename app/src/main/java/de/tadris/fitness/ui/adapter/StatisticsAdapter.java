package de.tadris.fitness.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import de.tadris.fitness.ui.statistics.Fragments.StatsFragment;
import de.tadris.fitness.ui.statistics.Fragments.StatsHistoryFragment;
import de.tadris.fitness.ui.statistics.Fragments.StatsHealthFragment;
import de.tadris.fitness.ui.statistics.Fragments.StatsOverviewFragment;

public class StatisticsAdapter extends FragmentStateAdapter {

    ArrayList<Fragment> fragments = new ArrayList<Fragment>(Arrays.asList(
            new StatsOverviewFragment(),
            new StatsHistoryFragment(),
            new StatsHealthFragment()));

    public StatisticsAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @Override
    public Fragment createFragment(@NotNull int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public ArrayList<String> getTitles() {
        ArrayList<String> list = new ArrayList<>();
        for (Fragment fragment : fragments) {
            StatsFragment statsFragment = (StatsFragment)fragment;
            list.add(statsFragment.getTitle());
        }
        return list;
    }
}
