package de.tadris.fitness.data;

import androidx.room.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity(tableName = "indoor_workout")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndoorWorkout extends BaseWorkout {

    int amount;

    /**
     * Average frequency in hertz
     */
    double avgFrequency;

    double maxIntensity;

    double avgIntensity;

}
