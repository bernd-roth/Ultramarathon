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

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import de.tadris.fitness.R;
import de.tadris.fitness.util.unit.UnitUtils;

public class InterfaceSettingsActivity extends FitoTrackSettingsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setTitle(R.string.preferencesUserInterfaceTitle);

        addPreferencesFromResource(R.xml.preferences_user_interface);

        bindPreferenceSummaryToValue(findPreference("unitSystem"));
        bindPreferenceSummaryToValue(findPreference("mapStyle"));
        bindPreferenceSummaryToValue(findPreference("themeSetting"));
        findPreference("themeSetting").setOnPreferenceChangeListener((preference, newValue) -> {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);
            Toast.makeText(InterfaceSettingsActivity.this, R.string.hintRestart, Toast.LENGTH_LONG).show();
            return true;
        });

        findPreference("weight").setOnPreferenceClickListener(preference -> {
            showWeightPicker();
            return true;
        });

    }

    private void showWeightPicker() {
        UnitUtils.setUnit(this); // Maybe the user changed unit system

        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        d.setTitle(getString(R.string.pref_weight));
        View v = getLayoutInflater().inflate(R.layout.dialog_weight_picker, null);
        NumberPicker np = v.findViewById(R.id.weightPicker);
        np.setMaxValue((int) UnitUtils.CHOSEN_SYSTEM.getWeightFromKilogram(150));
        np.setMinValue((int) UnitUtils.CHOSEN_SYSTEM.getWeightFromKilogram(20));
        np.setFormatter(value -> value + " " + UnitUtils.CHOSEN_SYSTEM.getWeightUnit());
        final String preferenceVariable = "weight";
        np.setValue((int) Math.round(UnitUtils.CHOSEN_SYSTEM.getWeightFromKilogram(preferences.getInt(preferenceVariable, 80))));
        np.setWrapSelectorWheel(false);

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) -> {
            int unitValue = np.getValue();
            int kilograms = (int) Math.round(UnitUtils.CHOSEN_SYSTEM.getKilogramFromUnit(unitValue));
            preferences.edit().putInt(preferenceVariable, kilograms).apply();
        });

        d.create().show();
    }

}
