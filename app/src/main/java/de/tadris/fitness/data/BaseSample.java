package de.tadris.fitness.data;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public abstract class BaseSample {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "workout_id", index = true)
    public long workoutId;

    public long absoluteTime;

    public long relativeTime;

    @ColumnInfo(name = "heart_rate")
    public int heartRate; // in bpm

    /**
     * -1 -> No interval was triggered
     * greater than 0 -> Interval with this id was triggered at this sample
     */
    @ColumnInfo(name = "interval_triggered")
    public long intervalTriggered = -1;

}
