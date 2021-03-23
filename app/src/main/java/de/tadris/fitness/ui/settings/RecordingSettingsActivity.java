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
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.model.AutoStartWorkout;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.recording.event.TTSReadyEvent;
import de.tadris.fitness.ui.dialog.ChooseAutoStartDelayDialog;
import de.tadris.fitness.ui.dialog.ChooseAutoStartModeDialog;
import de.tadris.fitness.ui.dialog.ChooseAutoTimeoutDialog;
import de.tadris.fitness.util.NfcAdapterHelper;
import de.tadris.fitness.util.NumberPickerUtils;

public class RecordingSettingsActivity
        extends FitoTrackSettingsActivity
        implements ChooseAutoStartModeDialog.AutoStartModeSelectListener,
        ChooseAutoStartDelayDialog.AutoStartDelaySelectListener,
        ChooseAutoTimeoutDialog.AutoTimeoutSelectListener {

    Instance instance;
    UserPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        instance = Instance.getInstance(this);
        preferences = Instance.getInstance(this).userPreferences;

        setTitle(R.string.preferencesRecordingTitle);

        addPreferencesFromResource(R.xml.preferences_recording);

        // modify NFC option
        if (!NfcAdapterHelper.isNfcPresent(this)) { // disable the NFC option if the device doesn't support NFC
            findPreference("nfcStart").setEnabled(false);
        } else {
            // ask the user to enable NFC in device settings when they want to use it in the app
            // but NFC is globally disabled
            findPreference("nfcStart").setOnPreferenceChangeListener((pref, newValue) -> {
                if ((Boolean) newValue && !NfcAdapterHelper.isNfcEnabled(this)) {
                    NfcAdapterHelper.createNfcEnableDialog(this).show();
                    return false;   // do NOT use NFC yet, user first needs to enable it in device settings
                }
                return true;
            });
        }

        findPreference("speech").setOnPreferenceClickListener(preference -> {
            checkTTS(this::showSpeechConfig);
            return true;
        });
        findPreference("intervals").setOnPreferenceClickListener(preference -> {
            checkTTS(this::showIntervalSetManagement);
            return true;
        });

        findPreference("autoStartModeConfig").setOnPreferenceClickListener(preference -> {
            showAutoStartModeConfig();
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

        findPreference("currentSpeedAverageTimeConfig").setOnPreferenceClickListener(preference -> {
            showCurrentSpeedAverageTimePicker();
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

    private void showAutoStartModeConfig() {
        new ChooseAutoStartModeDialog(this, this).show();
    }

    private void showAutoStartDelayConfig() {
        int initialDelayS = instance.userPreferences.getAutoStartDelay();
        new ChooseAutoStartDelayDialog(this, this,
                (long) initialDelayS * 1_000).show();
    }

    private void showCurrentSpeedAverageTimePicker() {
        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        final float disabledAlpha = 0.3f;

        d.setTitle(getString(R.string.preferenceCurrentSpeedTime));
        View v = getLayoutInflater().inflate(R.layout.dialog_current_speed, null);
        Switch sw = v.findViewById(R.id.useAverageForCurrentSpeed);

        // number picker: 0-120 seconds, only enabled when sw is checked, transparent if disabled
        NumberPicker np = v.findViewById(R.id.currentSpeedAverageTime);
        sw.setChecked(preferences.getUseAverageForCurrentSpeed());
        np.setEnabled(sw.isChecked());
        np.setAlpha(sw.isChecked() ? 1f : disabledAlpha);
        np.setMaxValue(120);
        np.setMinValue(0);
        np.setFormatter(value -> getResources().getQuantityString(R.plurals.seconds, value, value));
        np.setValue(preferences.getTimeForCurrentSpeed());
        np.setWrapSelectorWheel(false);
        NumberPickerUtils.fixNumberPicker(np);

        sw.setOnCheckedChangeListener((view, isChecked) -> {
            np.setEnabled(isChecked);
            np.setAlpha(isChecked ? 1f : disabledAlpha);
        });

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) -> {
            preferences.setUseAverageForCurrentSpeed(sw.isChecked());
            if (sw.isChecked()) {
                preferences.setTimeForCurrentSpeed(np.getValue());
            }
        });

        d.create().show();
    }

    private void showAutoTimeoutConfig() {
        new ChooseAutoTimeoutDialog(this, this).show();
    }

    @Override
    public void onSelectAutoStartMode(AutoStartWorkout.Mode mode) {
        Instance.getInstance(this).userPreferences.setAutoStartMode(mode);
    }

    @Override
    public void onSelectAutoStartDelay(int delayS) {
        Instance.getInstance(this).userPreferences.setAutoStartDelay(delayS);
    }

    @Override
    public void onSelectAutoTimeout(int timeoutM) {
        Instance.getInstance(this).userPreferences.setAutoTimeout(timeoutM);
    }
}
