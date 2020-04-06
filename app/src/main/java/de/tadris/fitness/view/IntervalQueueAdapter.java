package de.tadris.fitness.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.tadris.fitness.R;
import de.tadris.fitness.data.IntervalQueue;

public class IntervalQueueAdapter extends RecyclerView.Adapter<IntervalQueueAdapter.IntervalQueueViewHolder> {

    public static class IntervalQueueViewHolder extends RecyclerView.ViewHolder{

        final View root;
        final TextView nameText;

        IntervalQueueViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root= itemView;
            nameText= itemView.findViewById(R.id.intervalQueueName);
        }
    }

    private final IntervalQueue[] queues;
    private final IntervalQueueAdapter.IntervalQueueAdapterListener listener;

    public IntervalQueueAdapter(IntervalQueue[] queues, IntervalQueueAdapterListener listener) {
        this.queues = queues;
        this.listener = listener;
    }

    @Override
    public IntervalQueueAdapter.IntervalQueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_workout, parent, false);
        return new IntervalQueueAdapter.IntervalQueueViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(IntervalQueueAdapter.IntervalQueueViewHolder holder, final int position) {
        IntervalQueue intervalQueue= queues[position];
        holder.nameText.setText(intervalQueue.name);
        holder.root.setOnClickListener(view -> listener.onItemSelect(position, intervalQueue));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return queues.length;
    }

    public interface IntervalQueueAdapterListener{
        void onItemSelect(int pos, IntervalQueue queue);
        void onItemDelete(int pos, IntervalQueue queue);
    }

}
