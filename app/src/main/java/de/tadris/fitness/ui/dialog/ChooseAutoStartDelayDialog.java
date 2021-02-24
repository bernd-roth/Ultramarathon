package de.tadris.fitness.ui.dialog;

import android.app.Activity;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;

/**
 * Build a dialog to choose the delay after which a workout will be started automatically
 */
public class ChooseAutoStartDelayDialog extends NumberPickerDialog<Integer> {

    private static final int STEP_WIDTH = 5;
    private static final int NO_DELAY = 0;
    private static final int MAX_VALUE = 60;

    private final AutoStartDelaySelectListener listener;
    private final int initialDelayS;

    /**
     * @param context       The context this dialog should be shown in
     * @param listener      The listener that is called when the user selects a delay
     * @param initialDelayS Initially selected auto start delay in seconds
     */
    public ChooseAutoStartDelayDialog(Activity context, AutoStartDelaySelectListener listener, int initialDelayS) {
        super(context, context.getString(R.string.pref_auto_start_delay_title));
        this.listener = listener;
        this.initialDelayS = initialDelayS;
    }

    /**
     * Initialize dialog with default delay from preferences
     * @param context       The context this dialog should be shown in
     * @param listener      The listener that is called when the user selects a delay
     */
    public ChooseAutoStartDelayDialog(Activity context, AutoStartDelaySelectListener listener) {
        super(context, context.getString(R.string.pref_auto_start_delay_title));
        this.listener = listener;
        this.initialDelayS = Instance.getInstance(context).userPreferences.getAutoStartDelay();
    }

    @Override
    protected int getOptionCount() {
        return (MAX_VALUE - NO_DELAY) / STEP_WIDTH + 1;
    }

    @Override
    protected Integer getInitOption() {
        return initialDelayS;
    }

    @Override
    protected String format(Integer delayS) {
        return delayS == NO_DELAY
                ? context.getText(R.string.noAutoStartDelay).toString()
                : delayS + " " + context.getText(R.string.timeSecondsShort);
    }

    @Override
    protected int toOptionNum(Integer delayS) {
        int res = delayS / STEP_WIDTH;
        if (res < 0) {
            return 0;
        } else if (res >= getOptionCount()) {
            return getOptionCount() - 1;
        } else {
            return res;
        }
    }

    @Override
    protected Integer fromOptionNum(int optionNum) {
        return optionNum * STEP_WIDTH;
    }

    @Override
    protected void onSelectOption(Integer delayS) {
        listener.onSelectAutoStartDelay(delayS);
    }

    public interface AutoStartDelaySelectListener {
        /**
         * @param delayS Selected auto pause timeout in seconds
         */
        void onSelectAutoStartDelay(int delayS);
    }
}
