package de.tadris.fitness.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.NumberPicker;

import de.tadris.fitness.R;
import de.tadris.fitness.util.NumberPickerUtils;

/**
 * Base class for building a dialog to choose an arbitrary time from arbitrary options
 */
public abstract class ChooseAutoTimeDialog {

    protected final Activity context;
    private final String title;
    private AlertDialog dialog;

    /**
     *
     * @param context The context this dialog should be shown in
     * @param title The dialog's title
     */
    public ChooseAutoTimeDialog(Activity context, String title) {
        this.context = context;
        this.title = title;
    }

    /**
     * Show the time dialog.
     */
    public void show() {
        final AlertDialog.Builder d = new AlertDialog.Builder(context);
        d.setTitle(title);
        View v = context.getLayoutInflater().inflate(R.layout.dialog_auto_timeout_picker, null);

        NumberPicker npT = v.findViewById(R.id.autoTimeoutPicker);
        npT.setMaxValue(getOptionCount() - 1);
        npT.setMinValue(0);
        npT.setFormatter(value -> format(optionToTime(value)));
        npT.setValue(timeToOption(getInitTime()));
        npT.setWrapSelectorWheel(false);
        NumberPickerUtils.fixNumberPicker(npT);

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) ->
                onSelectAutoTime(optionToTime(npT.getValue())));
        dialog = d.create();
        dialog.show();
    }

    /**
     * Hide the time dialog.
     */
    public AlertDialog getDialog() {
        return dialog;
    }

    /**
     * Get the amount of different options to be displayed
     */
    protected abstract int getOptionCount();

    /**
     * Get the initially selected time
     */
    protected abstract int getInitTime();

    /**
     * Specify how a specific time should be presented to the user
     * @param time
     * @return
     */
    protected abstract String format(int time);

    /**
     * Convert "real-world" time to dialog option num
     * @param time
     * @return option num (must be in [0, {@link #getOptionCount()} - 1])
     */
    protected abstract int timeToOption(int time);

    /**
     * Convert dialog option num to "real-world" time
     * @param option
     * @return
     */
    protected abstract int optionToTime(int option);

    /**
     * Provide the selected time
     * @param time
     */
    protected abstract void onSelectAutoTime(int time);
}