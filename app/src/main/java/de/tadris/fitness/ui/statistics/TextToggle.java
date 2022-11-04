package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import de.tadris.fitness.R;

/**
 * Implementation of App Widget functionality.
 */
public class TextToggle extends LinearLayout {

    public TextView currentTitle;
    public TextView swapTitle;
    private TextView toggleArrows;
    private boolean swapped;

    private IOnToggleListener onToggleListener;

    OnClickListener clickListener;

    public TextToggle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.text_toggle, this);

        this.currentTitle = findViewById(R.id.currentTitle);
        this.swapTitle = findViewById(R.id.swapTitle);
        this.toggleArrows = findViewById(R.id.toggle_arrows);
        this.swapped = false;

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TextToggle);
        currentTitle.setText(array.getText(R.styleable.TextToggle_currentText));
        swapTitle.setText(array.getText(R.styleable.TextToggle_swapText));
        array.recycle();

        clickListener = view -> {
            this.toggle();
        };

        currentTitle.setOnClickListener(clickListener);
        swapTitle.setOnClickListener(clickListener);
        toggleArrows.setOnClickListener(clickListener);
        findViewById(R.id.textToggleLayout).setOnClickListener(clickListener);
    }

    public void toggle() {
        CharSequence current = currentTitle.getText();
        currentTitle.setText(swapTitle.getText());
        swapTitle.setText(current);

        toggleArrows.setText(toggleArrows.getText() == "\u296E" ? "\u296F" : "\u296E");

        swapped = !swapped;

        if (onToggleListener != null) {
            onToggleListener.onToggle(currentTitle.getText());
        }
    }

    public void setOnToggleListener(IOnToggleListener onToggleListener) {
        this.onToggleListener = onToggleListener;
    }

    public boolean isSwapped() {
        return this.swapped;
    }
}