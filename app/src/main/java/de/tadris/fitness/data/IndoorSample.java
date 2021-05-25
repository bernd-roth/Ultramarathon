package de.tadris.fitness.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "indoor_sample",
        foreignKeys = @ForeignKey(
                entity = IndoorWorkout.class,
                parentColumns = "id",
                childColumns = "workout_id",
                onDelete = CASCADE))
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndoorSample extends BaseSample {

    double intensity;

}
