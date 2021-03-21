package de.tadris.fitness.ui.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import androidx.annotation.Nullable;

import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.ui.MapActivity;

public class XmlThemeStyleSettingsActivity extends FitoTrackSettingsActivity {

    private static final String KEY_PREFIX = MapActivity.PREF_KEY_PREFIX_RENDER_THEME;
    private static final String KEY_OVERLAY_CATEGORY = KEY_PREFIX + "overlayCategories";

    private String language;
    private XmlRenderThemeStyleMenu styleOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Instance.getInstance(this).themes.getDefaultTheme());
        setupActionBar();
        setTitle(R.string.pref_render_theme_style_settings);
        addPreferencesFromResource(R.xml.preferences_render_theme);
        language = Locale.getDefault().getLanguage();
        styleOptions = Instance.getInstance(this).xmlRenderThemeStyleMenu;
        setupOptions();
    }

    private void setupOptions() {
        PreferenceScreen preferenceScreen = this.getPreferenceScreen();

        if (styleOptions == null) {
            Preference infoPreference = new Preference(preferenceScreen.getContext());
            infoPreference.setTitle("N/A");
            infoPreference.setSummary(R.string.pref_render_theme_style_info);
            infoPreference.setEnabled(true);
            infoPreference.setSelectable(false);
            preferenceScreen.addPreference(infoPreference);
            return;
        }

        ListPreference stylePreference = new ListPreference(preferenceScreen.getContext());
        stylePreference.setTitle(R.string.pref_render_theme_style);
        stylePreference.setKey(KEY_PREFIX + styleOptions.getId());
        Set<XmlRenderThemeStyleLayer> visibleLayers = styleOptions.getLayers().values().stream().filter(XmlRenderThemeStyleLayer::isVisible).collect(
                Collectors.toSet());
        stylePreference.setEntries(visibleLayers.stream().map(layer -> layer.getTitle(language)).toArray(CharSequence[]::new));
        stylePreference.setEntryValues(visibleLayers.stream().map(layer -> KEY_PREFIX + layer.getId()).toArray(CharSequence[]::new));
        stylePreference.setEnabled(true);
        stylePreference.setPersistent(true);
        stylePreference.setDefaultValue(KEY_PREFIX + styleOptions.getDefaultValue());
        stylePreference.setOnPreferenceChangeListener(onStylePrefChangedListener);
        preferenceScreen.addPreference(stylePreference);

        PreferenceCategory preferenceOverlayCategory = new PreferenceCategory(preferenceScreen.getContext());
        preferenceOverlayCategory.setTitle(R.string.preferenceCategoryRenderThemeCategories);
        preferenceOverlayCategory.setKey(KEY_OVERLAY_CATEGORY);
        preferenceScreen.addPreference(preferenceOverlayCategory);

        String layerId = getLayerIdFromSelection(stylePreference.getValue());
        stylePreference.setSummary(styleOptions.getLayer(layerId).getTitle(language));
        addOrReplaceCategories(layerId, preferenceOverlayCategory);
    }

    private String getLayerIdFromSelection(String selection) {
        String layerId;
        if (selection == null || !styleOptions.getLayers().containsKey(selection.replace(KEY_PREFIX, ""))) {
            layerId = styleOptions.getLayer(styleOptions.getDefaultValue()).getId();
        } else {
            layerId = selection.replace(KEY_PREFIX, "");
        }
        return layerId;
    }

    private void addOrReplaceCategories(String selection, PreferenceCategory preferenceOverlayCategory) {
        preferenceOverlayCategory.removeAll();
        String layerId = getLayerIdFromSelection(selection);
        for (XmlRenderThemeStyleLayer overlay : styleOptions.getLayer(layerId).getOverlays()) {
            CheckBoxPreference checkbox = new CheckBoxPreference(this);
            checkbox.setKey(KEY_PREFIX + overlay.getId());
            checkbox.setPersistent(true);
            checkbox.setTitle(overlay.getTitle(language));
            if (findPreference(KEY_PREFIX + overlay.getId()) == null) {
                checkbox.setChecked(overlay.isEnabled());
            }
            preferenceOverlayCategory.addPreference(checkbox);
        }
    }

    private final Preference.OnPreferenceChangeListener onStylePrefChangedListener = (preference, value) -> {
        PreferenceCategory preferenceOverlayCategory = (PreferenceCategory) this.getPreferenceScreen().findPreference(KEY_OVERLAY_CATEGORY);
        String selection = (String) value;
        preference.setSummary(styleOptions.getLayer(getLayerIdFromSelection(selection)).getTitle(language));
        addOrReplaceCategories(selection, preferenceOverlayCategory);
        return true;
    };
}
