package de.tadris.fitness.ui.record;

import org.mapsforge.core.model.LatLong;

import java.util.List;

public interface NavigationModeListener {

    enum NavigationMode {
        Automatic,
        Manual
    }

    /**
     * @brief Notifies if mode of navigation changed
     */
    void onNavigationModeChanged(final NavigationMode mode);

    /**
     * @brief Notifies if navigation to a position is needed
     */
    void navigateToPosition(final LatLong navigateTo);

    /**
     * @brief This callback is needed to get details about the map view to calculate the
     * to current location. Depending on that value, a threshold can be evaluated and
     * eventually it can be notified whether to navigate to a specific position or to change
     * navigation mode.
     * @return current center of mapView
     */
    LatLong onGetCenter();
}
