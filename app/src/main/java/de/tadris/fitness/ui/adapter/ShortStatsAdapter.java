package de.tadris.fitness.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import de.tadris.fitness.ui.statistics.ShortStatsItemView;

public class ShortStatsAdapter extends RecyclerView.Adapter<ShortStatsAdapter.ViewHolder> {

    private static Context ctx;
    /**
     * The constructor for the adapter.
     * @param ctx is the current context
     */
    public ShortStatsAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = new ShortStatsItemView(ctx);
        ViewGroup.LayoutParams parentParams = parent.getLayoutParams();
        view.setLayoutParams(new ViewGroup.LayoutParams(parentParams.width, parentParams.height));
        return new ViewHolder(view, ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.itemView instanceof ShortStatsItemView) {
            ((ShortStatsItemView) holder.itemView).updateChart();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.itemView instanceof ShortStatsItemView) {
            ((ShortStatsItemView) holder.itemView).chartType = position;
            ((ShortStatsItemView) holder.itemView).updateChart();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
        }
    }
}
