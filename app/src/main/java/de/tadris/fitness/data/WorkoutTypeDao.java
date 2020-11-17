package de.tadris.fitness.data;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface WorkoutTypeDao {

    @Query("SELECT * FROM workout_type WHERE id = :id")
    WorkoutType findById(String id);

    @Query("SELECT * FROM workout_type")
    WorkoutType[] findAll();

}
