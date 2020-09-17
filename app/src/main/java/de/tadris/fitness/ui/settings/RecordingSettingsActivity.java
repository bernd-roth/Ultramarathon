/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
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

import de.tadris.fitness.R;
import de.tadris.fitness.recording.announcement.TTSController;

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

        findPreference("autoTimeoutConfig").setOnPreferenceClickListener(preference -> {
            showAutoTimeoutConfig();
            return true;
        });
    }

    private TTSController TTSController;

    private void checkTTS(Runnable onTTSAvailable) {
        TTSController = new TTSController(this, available -> {
            if (available) {
                onTTSAvailable.run();
            } else {
                // TextToSpeech is not available
                Toast.makeText(RecordingSettingsActivity.this, R.string.ttsNotAvailable, Toast.LENGTH_LONG).show();
            }
            if (TTSController != null) {
                TTSController.destroy();
            }
        });
    }

    private void showSpeechConfig() {
        startActivity(new Intent(this, VoiceAnnouncementsSettingsActivity.class));
    }

    private void showIntervalSetManagement() {
        startActivity(new Intent(this, ManageIntervalSetsActivity.class));
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
        npT.setFormatter(value -> value == 0 ? "No Timeout" : value * stepWidth + " " + getText(R.string.timeMinuteShort));
        final String autoTimeoutVariable = "autoTimeoutPeriod";
        npT.setValue(preferences.getInt(autoTimeoutVariable, 20) / stepWidth);
        npT.setWrapSelectorWheel(false);

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) ->
                preferences.edit()
                        .putInt(autoTimeoutVariable, npT.getValue() * stepWidth)
                        .apply());

        d.create().show();
    }

}