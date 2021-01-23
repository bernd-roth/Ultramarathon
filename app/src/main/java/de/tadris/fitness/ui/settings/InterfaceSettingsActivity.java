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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;

import java.util.ArrayList;
import java.util.Collections;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.util.unit.DistanceUnitSystem;

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
        bindPreferenceSummaryToValue(findPreference("dateFormat"));
        bindPreferenceSummaryToValue(findPreference("timeFormat"));
        bindPreferenceSummaryToValue(findPreference("energyUnit"));
        findPreference("themeSetting").setOnPreferenceChangeListener((preference, newValue) -> {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);
            Toast.makeText(InterfaceSettingsActivity.this, R.string.hintRestart, Toast.LENGTH_LONG).show();
            return true;
        });

        findPreference("weight").setOnPreferenceClickListener(preference -> {
            showWeightPicker();
            return true;
        });

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Preference mapFilePref = findPreference("offlineMapFileName");
        bindPreferenceSummaryToValue(mapFilePref);
        mapFilePref.setOnPreferenceClickListener(preference -> {
            showFilePickerFor("offlineMapFileName", "map");
            return true;
        });

        Preference themeFilePref = findPreference("offlineMapThemeFileName");
        bindPreferenceSummaryToValue(themeFilePref);
        themeFilePref.setOnPreferenceClickListener(preference -> {
            showFilePickerFor("offlineMapThemeFileName", "xml");
            return true;
        });
    }

    private void showWeightPicker() {
        Instance.getInstance(this).distanceUnitUtils.setUnit(); // Maybe the user changed unit system
        DistanceUnitSystem unitSystem = Instance.getInstance(this).distanceUnitUtils.getDistanceUnitSystem();

        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        d.setTitle(getString(R.string.pref_weight));
        View v = getLayoutInflater().inflate(R.layout.dialog_weight_picker, null);
        NumberPicker np = v.findViewById(R.id.weightPicker);
        np.setMaxValue((int) unitSystem.getWeightFromKilogram(150));
        np.setMinValue((int) unitSystem.getWeightFromKilogram(20));
        np.setFormatter(value -> value + " " + unitSystem.getWeightUnit());
        final String preferenceVariable = "weight";
        np.setValue((int) Math.round(unitSystem.getWeightFromKilogram(preferences.getInt(preferenceVariable, 80))));
        np.setWrapSelectorWheel(false);

        d.setView(v);

        d.setNegativeButton(R.string.cancel, null);
        d.setPositiveButton(R.string.okay, (dialog, which) -> {
            int unitValue = np.getValue();
            int kilograms = (int) Math.round(unitSystem.getKilogramFromUnit(unitValue));
            preferences.edit().putInt(preferenceVariable, kilograms).apply();
        });

        d.create().show();
    }

    private void showFilePickerFor(String key, String filter) {
        final StorageChooser chooser = new StorageChooser.Builder().withActivity(this).withFragmentManager(getFragmentManager()).allowCustomPath(
                true).setType(StorageChooser.FILE_PICKER).customFilter(new ArrayList<String>(Collections.singleton(filter))).build();
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(InterfaceSettingsActivity.this);
                preferences.edit().putString(key, path).apply();
            }
        });
        chooser.show();
    }

}
