/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.tadris.fitness.model.AutoStartWorkout;

public class UserPreferences {
    private static final String USE_NFC_START_VARIABLE = "nfcStart";
    private static final String AUTO_START_DELAY_VARIABLE = "autoStartDelayPeriod";
    private static final String AUTO_START_MODE_VARIABLE = "autoStartMode";
    private static final String AUTO_TIMEOUT_VARIABLE = "autoTimeoutPeriod";
    private static final String USE_AUTO_PAUSE_VARIABLE = "autoPause";
    private static final String ANNOUNCE_SUPPRESS_DURING_CALL_VARIABLE = "announcementSuppressDuringCall";
    private static final String ANNOUNCE_AUTO_START_COUNTDOWN = "announcement_countdown";
    private static final String USE_AVERAGE_FOR_CURRENT_SPEED = "useAverageForCurrentSpeed";
    private static final String TIME_FOR_CURRENT_SPEED = "timeForCurrentSpeed";
    private static final String HAS_LOWER_TARGET_SPEED_LIMIT = "hasLowerTargetSpeedLimit";
    private static final String LOWER_TARGET_SPEED_LIMIT = "lowerTargetSpeedLimit";
    private static final String HAS_UPPER_TARGET_SPEED_LIMIT = "hasUpperTargetSpeedLimit";
    private static final String UPPER_TARGET_SPEED_LIMIT = "upperTargetSpeedLimit";

    /**
     * Default NFC start enable state if no other has been chosen
     */
    public static final boolean DEFAULT_USE_NFC_START = false;

    /**
     * Default auto start delay in seconds if no other has been chosen
     */
    public static final int DEFAULT_AUTO_START_DELAY_S = AutoStartWorkout.DEFAULT_DELAY_S;

    /**
     * Default auto start mode if no other has been chosen
     */
    public static final String DEFAULT_AUTO_START_MODE = AutoStartWorkout.Mode.getDefault().toString();

    /**
     * Default auto workout stop timeout in minutes if no other has been chosen
     */
    public static final int DEFAULT_AUTO_TIMEOUT_M = 20;

    /**
     * Default auto pause enable state if no other has been chosen
     */
    public static final boolean DEFAULT_USE_AUTO_PAUSE = true;

    /**
     * Default asuppress announcements during call state if no other has been chosen
     */
    public static final boolean DEFAULT_ANNOUNCE_SUPPRESS_DURING_CALL = true;

    /**
     * Default for using auto start countdown TTS announcements if no other has been chosen
     */
    public static final boolean DEFAULT_ANNOUNCE_AUTO_START_COUNTDOWN = true;

    /**
     * Default for using an average over a certain time instead of the last record for calculating
     * the current speed
     */
    private static final boolean DEFAULT_USE_AVERAGE_FOR_CURRENT_SPEED = false;

    /**
     * Default time for calculating the current speed when using average (i.e. when
     * DEFAULT_USE_AVERAGE_FOR_CURRENT_SPEED is true)
     */
    private static final int DEFAULT_TIME_FOR_CURRENT_SPEED = 15;


    private final SharedPreferences preferences;

    public UserPreferences(Context context) {
        this.preferences= PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getUserWeight(){
        return preferences.getInt("weight", 80);
    }

    public int getSpokenUpdateTimePeriod(){
        return preferences.getInt("spokenUpdateTimePeriod", 0);
    }

    public int getSpokenUpdateDistancePeriod(){
        return preferences.getInt("spokenUpdateDistancePeriod", 0);
    }

    public String getMapStyle(){
        return preferences.getString("mapStyle", "osm.mapnik");
    }

    public boolean intervalsIncludePauses() {
        return preferences.getBoolean("intervalsIncludePause", true);
    }

    public String getIdOfDisplayedInformation(int slot) {
        String defValue = "";
        switch (slot) {
            case 0:
                defValue = "distance";
                break;
            case 1:
                defValue = "energy_burned";
                break;
            case 2:
                defValue = "avgSpeedMotion";
                break;
            case 3:
                defValue = "pause_duration";
                break;
        }
        return preferences.getString("information_display_" + slot, defValue);
    }

    public void setIdOfDisplayedInformation(int slot, String id) {
        preferences.edit().putString("information_display_" + slot, id).apply();
    }

    public String getDateFormatSetting() {
        return preferences.getString("dateFormat", "system");
    }

    public String getTimeFormatSetting() {
        return preferences.getString("timeFormat", "system");
    }

    public String getDistanceUnitSystemId() {
        return preferences.getString("unitSystem", "1");
    }

    public String getEnergyUnit() {
        return preferences.getString("energyUnit", "kcal");
    }

    public boolean getShowOnLockScreen() {
        return preferences.getBoolean("showOnLockScreen", false);
    }

    public String getOfflineMapFileName() {
        return preferences.getString("offlineMapFileName", null);
    }

    /**
     * Check if NFC start is currently enabled
     * @return whether NFC start is enabled or not
     */
    public boolean getUseNfcStart() {
        return preferences.getBoolean(USE_NFC_START_VARIABLE, DEFAULT_USE_NFC_START);
    }
    
    /**
     * Get the currently configured auto start delay
     * @return auto start delay in seconds
     */
    public int getAutoStartDelay() {
        return preferences.getInt(AUTO_START_DELAY_VARIABLE, DEFAULT_AUTO_START_DELAY_S);
    }

    /**
     * Change the currently configured auto start delay
     * @param delayS new auto start delay in seconds
     */
    public void setAutoStartDelay(int delayS) {
        preferences.edit().putInt(AUTO_START_DELAY_VARIABLE, delayS).apply();
    }

    /**
     * Get the currently configured auto start mode
     * @return auto start mode
     */
    public AutoStartWorkout.Mode getAutoStartMode() {
        try {
            return AutoStartWorkout.Mode.valueOf(preferences.getString(AUTO_START_MODE_VARIABLE,
                    DEFAULT_AUTO_START_MODE));
        } catch (IllegalArgumentException ex) {
            // use default mode instead if preferences are broken
            return AutoStartWorkout.Mode.getDefault();
        }
    }

    /**
     * Change the currently configured auto start mode
     * @param mode new auto start mode
     */
    public void setAutoStartMode(AutoStartWorkout.Mode mode) {
        preferences.edit().putString(AUTO_START_MODE_VARIABLE, mode.toString()).apply();
    }

    /**
     * Get the currently configured timeout after which a workout is automatically stopped
     * @return auto workout stop timeout in minutes
     */
    public int getAutoTimeout() {
        return preferences.getInt(AUTO_TIMEOUT_VARIABLE, DEFAULT_AUTO_TIMEOUT_M);
    }

    /**
     * Change the currently configured timeout after which a workout is automatically stopped
     * @param timeoutM new auto workout stop timeout in minutes
     */
    public void setAutoTimeout(int timeoutM) {
        preferences.edit().putInt(AUTO_TIMEOUT_VARIABLE, timeoutM).apply();
    }


    /**
     * Check if auto pause is currently enabled
     * @return whether auto pause is enabled or not
     */
    public boolean getUseAutoPause() {
        return preferences.getBoolean(USE_AUTO_PAUSE_VARIABLE, DEFAULT_USE_AUTO_PAUSE);
    }

    /**
     * Change the current state of auto pause
     * @param useAutoStart new auto pause enable state
     */
    public void setUseAutoPause(boolean useAutoStart) {
        preferences.edit().putBoolean(USE_AUTO_PAUSE_VARIABLE, useAutoStart).apply();
    }

    /**
     * Check if voice announcements should be suppressed during calls
     * @return whether announcements should be suppressed during calls
     */
    public boolean getSuppressAnnouncementsDuringCall() {
        return preferences.getBoolean(ANNOUNCE_SUPPRESS_DURING_CALL_VARIABLE, DEFAULT_ANNOUNCE_SUPPRESS_DURING_CALL);
    }

    /**
     * Check if auto start countdown related announcements are enabled
     * @return whether countdown announcements are enabled
     */
    public boolean isAutoStartCountdownAnnouncementsEnabled() {
        return preferences.getBoolean(ANNOUNCE_AUTO_START_COUNTDOWN, DEFAULT_ANNOUNCE_AUTO_START_COUNTDOWN);
    }

    /**
     * Check if the average speed over a certain time shall be used to calculate the current speed
     * (instead of the last record only)
     * @return whether the average shall be used
     */
    public boolean getUseAverageForCurrentSpeed() {
        return preferences.getBoolean(USE_AVERAGE_FOR_CURRENT_SPEED, DEFAULT_USE_AVERAGE_FOR_CURRENT_SPEED);
    }

    /**
     * Set if the average speed over a certain time shall be used to calculate the current speed
     * (instead of the last record only)
     * @param useAverage whether the average shall be used
     */
    public void setUseAverageForCurrentSpeed(boolean useAverage) {
        preferences.edit().putBoolean(USE_AVERAGE_FOR_CURRENT_SPEED, useAverage).apply();
    }

    /**
     * The time (in seconds) to take the average speed over when calculating the current speed
     * @return the time (in seconds) over which to take the average speed
     */
    public int getTimeForCurrentSpeed() {
        return preferences.getInt(TIME_FOR_CURRENT_SPEED, DEFAULT_TIME_FOR_CURRENT_SPEED);
    }

    /**
     * Set the time (in seconds) to take the average speed over when calculating the current speed
     * @param time the time (in seconds) over which to take the average speed
     */
    public void setTimeForCurrentSpeed(int time) {
        preferences.edit().putInt(TIME_FOR_CURRENT_SPEED, time).apply();
    }
}
