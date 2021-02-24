package de.tadris.fitness.util;

import android.content.Context;
import android.telephony.TelephonyManager;

public class TelephonyHelper {

    /**
     * Check if the user is currently on a call
     * @param context current context
     * @return whether user is on call
     */
    public static boolean isOnCall(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }
}
