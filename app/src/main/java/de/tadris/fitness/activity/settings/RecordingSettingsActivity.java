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

package de.tadris.fitness.activity.settings;

import android.content.Intent;
import android.os.Bundle;
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

}
