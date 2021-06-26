package de.tadris.fitness.ui;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.TileLayer;
import org.mapsforge.map.layer.queue.Job;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;

import java.util.Set;

import de.tadris.fitness.Instance;

public class MapActivity extends FitoTrackActivity implements XmlRenderThemeMenuCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_KEY_PREFIX_RENDER_THEME = "XmlRenderThemeMenu-";

    protected MapView mapView;

    @Override
    public Set<String> getCategories(XmlRenderThemeStyleMenu styleMenu) {
        Instance.getInstance(this).userPreferences.setXmlRenderThemeStyleMenu(styleMenu);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String id = prefs.getString(PREF_KEY_PREFIX_RENDER_THEME + styleMenu.getId(), styleMenu.getDefaultValue());
        XmlRenderThemeStyleLayer baseLayer = styleMenu.getLayer(id.replace(PREF_KEY_PREFIX_RENDER_THEME, ""));
        if (baseLayer == null) {
            return null;
        }
        Set<String> result = baseLayer.getCategories();
        // add the categories from overlays that are enabled
        for (XmlRenderThemeStyleLayer overlay : baseLayer.getOverlays()) {
            if (prefs.getBoolean(PREF_KEY_PREFIX_RENDER_THEME + overlay.getId(), overlay.isEnabled())) {
                result.addAll(overlay.getCategories());
            }
        }
        return result;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.startsWith(PREF_KEY_PREFIX_RENDER_THEME)) {
            for (Layer layer : mapView.getLayerManager().getLayers()) {
                if (layer instanceof TileLayer) {
                    ((TileLayer<? extends Job>) layer).getTileCache().purge();
                }
            }
            mapView.repaint();
        }
    }

}
