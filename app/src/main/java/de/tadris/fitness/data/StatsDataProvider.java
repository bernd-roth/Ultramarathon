package de.tadris.fitness.data;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.ui.statistics.StatsProvider;
import de.tadris.fitness.util.WorkoutProperty;

public class StatsDataProvider {

    private final Context context;

    public StatsDataProvider(Context context)
    {
        this.context = context;

    }

    public ArrayList<StatsDataTypes.DataPoint> getData(WorkoutProperty requestedProperty, List<WorkoutType> workoutTypes)
    {
        return getData(requestedProperty, workoutTypes, null);
    }

    public ArrayList<StatsDataTypes.DataPoint> getData(WorkoutProperty requestedProperty, List<WorkoutType> workoutTypes, @Nullable StatsDataTypes.TimeSpan timeSpan)
    {
        ArrayList<StatsDataTypes.DataPoint> data = new ArrayList<>();
        List<BaseWorkout> workouts = Instance.getInstance(context).db.getAllWorkouts();
        for (BaseWorkout workout : workouts) {
            if (workoutTypes.contains(workout.getWorkoutType(this.context)) && timeSpan.contains(workout.start)) {
                try {
                    if (requestedProperty.isBaseProperty()) {
                        data.add(new StatsDataTypes.DataPoint(workout.getWorkoutType(context),
                                workout.id,
                                workout.start,
                                getBasePropertyValue(requestedProperty, workout)));
                    }
                    else if (requestedProperty.isGPSProperty()) {
                        data.add(new StatsDataTypes.DataPoint(workout.getWorkoutType(context),
                                workout.id,
                                workout.start,
                                getGPSPropertyValue(requestedProperty, (GpsWorkout)workout)));
                    }
                    else {
                        data.add(new StatsDataTypes.DataPoint(workout.getWorkoutType(context),
                                workout.id,
                                workout.start,
                                getIndoorPropertyValue(requestedProperty, (IndoorWorkout) workout)));
                    }
                }
                catch (Exception e) {
                    // This should never happen, cause it is checked by the if clauses above
                }
            }
        }
        return data;
    }

    private double getBasePropertyValue(WorkoutProperty property, BaseWorkout workout) throws Exception {
        switch (property)
        {
            case START:
                return workout.start;
            case END:
                return workout.end;
            case DURATION:
                return workout.duration;
            case PAUSE_DURATION:
                return workout.pauseDuration;
            case AVG_HEART_RATE:
                return workout.avgHeartRate;
            case MAX_HEART_RATE:
                return workout.maxHeartRate;
            case CALORIE:
                return workout.calorie;
            default:
                throw new Exception("Property is no BaseProperty");
        }
    }

    private double getGPSPropertyValue(WorkoutProperty property, GpsWorkout workout) throws Exception {
        switch (property)
        {
            case LENGTH:
                return workout.length;
            case AVG_SPEED:
                return workout.avgSpeed;
            case TOP_SPEED:
                return workout.topSpeed;
            case AVG_PACE:
                return workout.avgPace;
            case ASCENT:
                return workout.ascent;
            case DESCENT:
                return workout.descent;
            default:
                throw new Exception("Property is no GPS Property");
        }
    }

    private double getIndoorPropertyValue(WorkoutProperty property, IndoorWorkout workout) throws Exception {
        switch (property)
        {
            case AVG_FREQUENCY:
                return workout.avgFrequency;
            case MAX_FREQUENCY:
                return workout.maxFrequency;
            case MAX_INTENSITY:
                return workout.maxIntensity;
            case AVG_INTENSITY:
                return workout.avgIntensity;
            default:
                throw new Exception("Property is no Indoor Property");
        }
    }
}