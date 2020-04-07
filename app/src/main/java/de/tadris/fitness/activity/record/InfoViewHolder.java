package de.tadris.fitness.activity.record;

import android.view.View;
import android.widget.TextView;

class InfoViewHolder {
    private final int slot;
    private final InfoViewClickListener listener;
    private final TextView titleView;
    private final TextView valueView;

    InfoViewHolder(int slot, InfoViewClickListener listener, TextView titleView, TextView valueView) {
        this.slot = slot;
        this.listener = listener;
        this.titleView = titleView;
        this.valueView = valueView;
        setOnClickListeners();
    }

    void setText(String title, String value) {
        this.titleView.setText(title);
        this.valueView.setText(value);
    }

    private void setOnClickListeners() {
        titleView.setOnClickListener(getOnClickListener());
        valueView.setOnClickListener(getOnClickListener());
    }

    private View.OnClickListener getOnClickListener() {
        return v -> listener.onInfoViewClick(slot);
    }

    public interface InfoViewClickListener {
        void onInfoViewClick(int slot);
    }
}
