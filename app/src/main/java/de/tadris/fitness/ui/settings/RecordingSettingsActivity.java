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

package de.tadris.fitness.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.tadris.fitness.R;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.recording.event.TTSReadyEvent;
import de.tadris.fitness.util.NumberPickerUtils;

public class RecordingSettingsActivity extends FitoTrackSettingsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setTitle(R.string.preferencesRecordingTitle);

        addPreferencesFromResource(R.xml.preferences_recording);

        findPreference("speech").setOnPreferenceClickListener(preference -> {
            checkTTS(this::showSpeechConfig);
            return true;
        });
        findPreference("intervals").setOnPreferenceClickListener(preference -> {
            checkTTS(this::showIntervalSetManagement);
            return true;
        });

        findPreference("autoStartDelayConfig").setOnPreferenceClickListener(preference -> {
            showAutoStartDelayConfig();
            return true;
        });

        findPreference("autoTimeoutConfig").setOnPreferenceClickListener(preference -> {
            showAutoTimeoutConfig();
            return true;
        });
    }

    private TTSController TTSController;

    private void checkTTS(Runnable onTTSAvailable) {
        TTSController = new TTSController(this);
        EventBus.getDefault().register(new Object() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            public void onTTSReady(TTSReadyEvent e) {
                if (e.ttsAvailable) {
                    onTTSAvailable.run();
                } else {
                    // TextToSpeech is not available
                    Toast.makeText(RecordingSettingsActivity.this, R.string.ttsNotAvailable, Toast.LENGTH_LONG).show();
                }
                if (TTSController != null) {
                    TTSController.destroy();
                }
                EventBus.getDefault().unregister(this);
            }
        });
    }

    private void showSpeechConfig() {
        startActivity(new Intent(this, VoiceAnnouncementsSettingsActivity.class));
    }

    private void showIntervalSetManagement() {
        startActivity(new Intent(this, ManageIntervalSetsActivity.class));
    }

    private void showAutoStartDelayConfig() {
        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        d.setTitle(getString(R.string.pref_auto_start_delay_title));
        View v = getLayoutInflater().inflate(R.layout.dialog_auto_timeout_picker, null);

        // TODO:
        //  - not sure which start delays (min-max) are actually useful, some people would certainly
        //      want delays larger than 60s
        //  - step size should probably be non-linear, e.g.
        //           5s from   0-30s,
        //          10s from  30s-60s,
        //          15s from  60s-180s,
        //          30s from 180s-
        int stepWidth = 5; // 5 secs Step Width

        NumberPicker npT = v.findViewById(R.id.autoTimeoutPicker);
        npT.setMaxValue(60 / stepWidth);
        npT.setMinValue(0);
        npT.setFormatter(value -> value == 0
                ? getText(R.string.noAutoStartDelay).toString()
                : value * stepWidth + " " + getText(R.string.timeSecondsShort));
        final String autoStartDelayVariable = "autoStartDelayPeriod";
        npT.setValue(preferences.getInt(autoStartDelayVariable, 20) / stepWidth);
        npT.setWrapSelectorWheel(false);

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) ->
                preferences.edit()
                        .putInt(autoStartDelayVariable, npT.getValue() * stepWidth)
                        .apply());

        d.create().show();
    }

    private void showAutoTimeoutConfig() {
        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        d.setTitle(getString(R.string.pref_auto_timeout_title));
        View v = getLayoutInflater().inflate(R.layout.dialog_auto_timeout_picker, null);

        int stepWidth = 5; // 5 Min Step Width

        NumberPicker npT = v.findViewById(R.id.autoTimeoutPicker);
        npT.setMaxValue(60 / stepWidth);
        npT.setMinValue(0);
        npT.setFormatter(value -> value == 0 ? getText(R.string.notimeout).toString() : value * stepWidth + " " + getText(R.string.timeMinuteShort));
        final String autoTimeoutVariable = "autoTimeoutPeriod";
        npT.setValue(preferences.getInt(autoTimeoutVariable, 20) / stepWidth);
        npT.setWrapSelectorWheel(false);
        NumberPickerUtils.fixNumberPicker(npT);

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) ->
                preferences.edit()
                        .putInt(autoTimeoutVariable, npT.getValue() * stepWidth)
                        .apply());

        d.create().show();
    }

}
