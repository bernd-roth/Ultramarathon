package de.tadris.fitness.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "interval",
        foreignKeys = @ForeignKey(
        entity = IntervalQueue.class,
        parentColumns = "id",
        childColumns = "queue_id",
        onDelete = CASCADE))
public class Interval {

    @PrimaryKey
    public long id;

    @ColumnInfo(name = "queue_id")
    public long queueId;

    public String name;

    // Delay AFTER the announcement/interval
    public long delayMillis;

}
