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

public class UserPreferences {
    private static final String USE_NFC_START_VARIABLE = "nfcStart";
    private static final String AUTO_START_DELAY_VARIABLE = "autoStartDelayPeriod";
    private static final String AUTO_TIMEOUT_VARIABLE = "autoTimeoutPeriod";
    private static final String USE_AUTO_PAUSE_VARIABLE = "autoPause";
    private static final String ANNOUNCE_SUPPRESS_DURING_CALL_VARIABLE = "announcementSuppressDuringCall";
    private static final String ANNOUNCE_AUTO_START_COUNTDOWN = "announcement_countdown";

    /**
     * Default NFC start enable state if no other has been chosen
     */
    public static final boolean DEFAULT_USE_NFC_START = false;

    /**
     * Default auto start delay in seconds if no other has been chosen
     */
    public static final int DEFAULT_AUTO_START_DELAY_S = 20;

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
}
