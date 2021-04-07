package de.tadris.fitness.map;

import de.tadris.fitness.data.WorkoutSample;

public interface MapSampleSelectionListener {
    /**
     * @param sample The sample changed or @null if the selection was removed
     */
    public void onSelectionChanged(WorkoutSample sample);
}
