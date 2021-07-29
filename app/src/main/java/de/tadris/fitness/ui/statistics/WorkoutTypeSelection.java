package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.graphics.Color;
import android.service.autofill.OnClickAction;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.WorkoutTypeFilter;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.ui.dialog.SelectWorkoutTypeDialog;
import de.tadris.fitness.ui.dialog.SelectWorkoutTypeDialogAll;
import de.tadris.fitness.util.Icon;

public class WorkoutTypeSelection extends LinearLayout {

    private WorkoutType selectedWorkoutType;
    private ArrayList<SelectWorkoutTypeDialog.WorkoutTypeSelectListener> listeners;

    public WorkoutTypeSelection(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_workout_type_selection, this);

        listeners = new ArrayList<>();

        // selectListener forwards the selected entry to setSelectedWorkoutType
        SelectWorkoutTypeDialog.WorkoutTypeSelectListener selectListener = workoutType -> setSelectedWorkoutType(workoutType);
        // Setup onClickListener
        OnClickListener clickListener = view -> new SelectWorkoutTypeDialogAll((FitoTrackActivity) getContext(), selectListener).show();
        this.setOnClickListener(clickListener);

        // The init
        setSelectedWorkoutType(new WorkoutType(WorkoutTypeFilter.ID_ALL,
                context.getString(R.string.workoutTypeAll), 0,
                Color.WHITE, "list", 0, WorkoutType.RecordingType.GPS.id));
    }

    public WorkoutType getSelectedWorkoutType() {
        return selectedWorkoutType;
    }

    public void setSelectedWorkoutType(@NotNull WorkoutType selectedWorkoutType) {
        this.selectedWorkoutType = selectedWorkoutType;

        ImageView imageView = findViewById(R.id.view_workout_type_selection_image);
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(),
                Icon.getIcon(selectedWorkoutType.icon)));

        TextView textView = findViewById(R.id.view_workout_type_selection_text);
        textView.setText(selectedWorkoutType.title);

        for (SelectWorkoutTypeDialog.WorkoutTypeSelectListener listener : listeners) {
            listener.onSelectWorkoutType(selectedWorkoutType);
        }
    }

    public void addOnWorkoutTypeSelectListener(SelectWorkoutTypeDialog.WorkoutTypeSelectListener listener) {
        this.listeners.add(listener);
    }

    public void removeOnWorkoutTypeSelectListener(SelectWorkoutTypeDialog.WorkoutTypeSelectListener listener) {
        this.listeners.remove(listener);
    }
}
