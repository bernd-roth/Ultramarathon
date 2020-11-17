package de.tadris.fitness.data;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;

@Entity(tableName = "workout_type")
public class WorkoutType implements Serializable {

    public static final String WORKOUT_TYPE_ID_OTHER = "other";
    public static final String WORKOUT_TYPE_ID_RUNNING = "running";

    @PrimaryKey
    public String id;

    public String title;

    @ColumnInfo(name = "min_distance")
    public int minDistance;

    public int color;

    public String icon;

    @ColumnInfo(name = "met")
    public int MET;

    public WorkoutType(String id, String title, int minDistance, int color, String icon, int MET) {
        this.id = id;
        this.title = title;
        this.minDistance = minDistance;
        this.color = color;
        this.icon = icon;
        this.MET = MET;
    }

    private static WorkoutType[] PRESETS = null;

    public static WorkoutType getWorkoutTypeById(Context context, String id) {
        buildPresets(context);
        for (WorkoutType type : PRESETS) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        WorkoutType type = Instance.getInstance(context).db.workoutTypeDao().findById(id);
        if (type == null && !id.equals(WORKOUT_TYPE_ID_OTHER)) {
            return getWorkoutTypeById(context, WORKOUT_TYPE_ID_OTHER);
        } else {
            return type;
        }
    }

    public static List<WorkoutType> getAllTypes(Context context) {
        buildPresets(context);
        List<WorkoutType> result = new ArrayList<>(Arrays.asList(PRESETS));
        WorkoutType[] fromDatabase = Instance.getInstance(context).db.workoutTypeDao().findAll();
        result.addAll(Arrays.asList(fromDatabase));
        return result;
    }

    private static void buildPresets(Context context) {
        if (PRESETS != null) return; // Don't build a second time
        PRESETS = new WorkoutType[]{
                new WorkoutType(WORKOUT_TYPE_ID_RUNNING,
                        context.getString(R.string.workoutTypeRunning),
                        5,
                        context.getResources().getColor(R.color.colorPrimaryRunning),
                        "running",
                        -1),
                new WorkoutType("walking",
                        context.getString(R.string.workoutTypeWalking),
                        5,
                        context.getResources().getColor(R.color.colorPrimaryRunning),
                        "walking",
                        -1),
                new WorkoutType("hiking",
                        context.getString(R.string.workoutTypeHiking),
                        5,
                        context.getResources().getColor(R.color.colorPrimaryHiking),
                        "walking",
                        -1),
                new WorkoutType("cycling",
                        context.getString(R.string.workoutTypeCycling),
                        10,
                        context.getResources().getColor(R.color.colorPrimaryBicycling),
                        "cycling",
                        -1),
                new WorkoutType("inline_skating",
                        context.getString(R.string.workoutTypeInlineSkating),
                        7,
                        context.getResources().getColor(R.color.colorPrimarySkating),
                        "inline_skating",
                        -1),
                new WorkoutType("skateboarding",
                        context.getString(R.string.workoutTypeSkateboarding),
                        7,
                        context.getResources().getColor(R.color.colorPrimarySkating),
                        "skateboarding",
                        -1),
                new WorkoutType("rowing",
                        context.getString(R.string.workoutTypeRowing),
                        7,
                        context.getResources().getColor(R.color.colorPrimaryRowing),
                        "rowing",
                        -1),
        };
    }
}
