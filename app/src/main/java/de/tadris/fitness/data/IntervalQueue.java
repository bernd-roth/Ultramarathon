package de.tadris.fitness.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "interval_queue")
public class IntervalQueue {

    static final int STATE_VISIBLE= 0;
    static final int STATE_DELETED= 1;

    @PrimaryKey
    public long id;
    public String name;

    public int state;

}
