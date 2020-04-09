/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Workout;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>{


    public static class WorkoutViewHolder extends RecyclerView.ViewHolder{

        final View root;
        final TextView lengthText;
        final TextView timeText;
        final TextView dateText;
        final TextView typeText;
        final TextView commentText;
        final ImageView iconView;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root= itemView;
            lengthText= itemView.findViewById(R.id.workoutLength);
            timeText=   itemView.findViewById(R.id.workoutTime);
            dateText=   itemView.findViewById(R.id.workoutDate);
            typeText=   itemView.findViewById(R.id.workoutType);
            commentText=itemView.findViewById(R.id.workoutComment);
            iconView = itemView.findViewById(R.id.workoutImage);
        }
    }

    private final Workout[] workouts;
    private final WorkoutAdapterListener listener;

    public WorkoutAdapter(Workout[] workouts, WorkoutAdapterListener listener) {
        this.workouts = workouts;
        this.listener = listener;
    }

    @Override
    public WorkoutAdapter.WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_workout, parent, false);
        return new WorkoutViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, final int position) {
        Context context = holder.root.getContext();
        Workout workout= workouts[position];
        holder.dateText.setText(Instance.getInstance(context).dateTimeUtils.formatDateTime(new Date(workout.start)));
        holder.typeText.setText(workout.getWorkoutType().title);
        if(workout.comment != null){
            if(workout.comment.length() > 33){
                holder.commentText.setText(workout.comment.substring(0, 30) + "...");
            }else{
                holder.commentText.setText(workout.comment);
            }
        }else{
            holder.commentText.setText("");
        }
        holder.lengthText.setText(Instance.getInstance(context).distanceUnitUtils.getDistance(workout.length));
        holder.timeText.setText(Instance.getInstance(context).distanceUnitUtils.getHourMinuteTime(workout.duration));
        holder.iconView.setImageResource(workout.getWorkoutType().icon);
        holder.root.setOnClickListener(v -> listener.onItemClick(position, workout));
        holder.root.setOnLongClickListener(v -> {
            listener.onItemLongClick(position, workout);
            return true;
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return workouts.length;
    }

    public interface WorkoutAdapterListener{
        void onItemClick(int pos, Workout workout);
        void onItemLongClick(int pos, Workout workout);
    }


}
