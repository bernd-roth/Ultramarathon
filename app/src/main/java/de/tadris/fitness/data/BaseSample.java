package de.tadris.fitness.data;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public abstract class BaseSample {

    @PrimaryKey
    public long id;

    public long absoluteTime;

    public long relativeTime;

    @ColumnInfo(name = "heart_rate")
    public int heartRate; // in bpm

}
