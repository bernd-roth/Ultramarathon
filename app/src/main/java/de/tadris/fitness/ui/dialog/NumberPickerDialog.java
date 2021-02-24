package de.tadris.fitness.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.NumberPicker;

import de.tadris.fitness.R;
import de.tadris.fitness.util.NumberPickerUtils;

/**
 * Base class for building a number picker dialog
 */
public abstract class NumberPickerDialog<T> {

    protected final Activity context;
    private final String title;
    private AlertDialog dialog;

    /**
     *
     * @param context The context this dialog should be shown in
     * @param title The dialog's title
     */
    public NumberPickerDialog(Activity context, String title) {
        this.context = context;
        this.title = title;
    }

    /**
     * Show the number picker dialog.
     */
    public void show() {
        final AlertDialog.Builder d = new AlertDialog.Builder(context);
        d.setTitle(title);
        View v = context.getLayoutInflater().inflate(R.layout.dialog_auto_timeout_picker, null);

        NumberPicker npT = v.findViewById(R.id.autoTimeoutPicker);
        npT.setMaxValue(getOptionCount() - 1);
        npT.setMinValue(0);
        npT.setFormatter(value -> format(fromOptionNum(value)));
        npT.setValue(toOptionNum(getInitOption()));
        npT.setWrapSelectorWheel(false);
        NumberPickerUtils.fixNumberPicker(npT);

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) ->
                onSelectOption(fromOptionNum(npT.getValue())));
        dialog = d.create();
        dialog.show();
    }

    /**
     * Get the dialog instance.
     */
    public AlertDialog getDialog() {
        return dialog;
    }

    /**
     * Get the amount of different options to be displayed
     */
    protected abstract int getOptionCount();

    /**
     * Get the initially selected option
     */
    protected abstract T getInitOption();

    /**
     * Specify how a specific option should be presented to the user
     * @param option the option that should be displayed
     * @return the string that will be shown for this option
     */
    protected abstract String format(T option);

    /**
     * Get the option number in the dialog for a specific option
     * @return option num (must be in [0, {@link #getOptionCount()} - 1])
     * @see #fromOptionNum(int)
     */
    protected abstract int toOptionNum(T option);

    /**
     * Get the original option for a specific option number
     * @see #toOptionNum(Object)
     */
    protected abstract T fromOptionNum(int optionNum);

    /**
     * Provide the selected option
     */
    protected abstract void onSelectOption(T option);
}