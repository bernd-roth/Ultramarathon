package de.tadris.fitness.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import de.tadris.fitness.Instance;

/**
 * This class provides an easier interface to the {@link Vibrator} class.
 *
 * It automatically disables the vibrator during calls, if configured in TTS preferences.
 */
public class VibratorController {
    private boolean enabled = true;  // always enabled
    private boolean suppressOnCall;
    private Vibrator vibrator;
    private final Context context;
    private final Instance instance;

    /**
     * Build an instance.
     * @param context the context it should run in
     * @param instance needed for user prefs
     */
    public VibratorController(Context context, Instance instance) {
        this.context = context;
        this.instance = instance;
        this.suppressOnCall = Instance.getInstance(context).userPreferences.getSuppressAnnouncementsDuringCall();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (!vibrator.hasVibrator()) {
            enabled = false;
        }
    }

    /**
     * Vibrate for a certain amount of time.
     * @param millis how long do you want the phone to vibrate
     */
    public void vibrate(int millis) {
        if (!enabled || (suppressOnCall && TelephonyHelper.isOnCall(context))){
            return;
        }
        if (Build.VERSION.SDK_INT < 26) {
            vibrator.vibrate(millis);
        } else {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    /**
     * Stop vibrating.
     */
    public void cancel() {
        if (!enabled) {
            return;
        }
        vibrator.cancel();
    }
}
