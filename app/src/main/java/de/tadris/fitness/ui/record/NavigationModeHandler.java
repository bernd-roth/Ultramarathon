package de.tadris.fitness.ui.record;

import android.annotation.SuppressLint;
import android.media.metrics.Event;
import android.view.MotionEvent;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.view.InputListener;

import de.tadris.fitness.recording.event.LocationChangeEvent;
import de.tadris.fitness.recording.gps.GpsRecorderService;

public class NavigationModeHandler implements View.OnTouchListener, View.OnClickListener, InputListener {
    private boolean focusedInitially = false;
    private NavigationMode navigationMode = NavigationMode.Automatic;
    private NavigationMode prevNavigationMode = null;
    private boolean navigationModeUpdateReq = true;
    private NavigationModeListener navigationModeListener = null;
    private LatLong currentGpsPosition = null;

    public enum NavigationMode {
        Automatic,
        Manual
    }

    public interface NavigationModeListener {


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
    };

    void init() {
        EventBus.getDefault().register(this);
    }

    void deinit() {
        EventBus.getDefault().unregister(this);
    }

    public void setNavigationModeListener(final NavigationModeListener listener) {
        assert(navigationModeListener == null);
        navigationModeListener = listener;
    }

    public void removeNavigationModeListener() {
        navigationModeListener = null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                prevNavigationMode = navigationMode;
                updateMode(NavigationMode.Manual, false);
            }
            break;
            case MotionEvent.ACTION_UP: {
                if(prevNavigationMode == NavigationMode.Automatic && distanceThresholdExceeds()){
                    updateMode(NavigationMode.Manual, true);
                } else {
                    updateMode(prevNavigationMode, null);
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if(prevNavigationMode == NavigationMode.Automatic){
                    if (distanceThresholdExceeds())
                    {
                        navigationModeListener.onNavigationModeChanged(NavigationMode.Manual);
                    } else {
                        navigationModeListener.onNavigationModeChanged(NavigationMode.Automatic);
                    }
                }
            }
            break;
        }

        managePositioning();
        return false;
    }

    private void updateMode(final NavigationMode mode, Boolean forceUpdate)
    {
        if (forceUpdate != null) {
            navigationModeUpdateReq = forceUpdate;
        } else {
            navigationModeUpdateReq = navigationMode != mode;
        }

        navigationMode = mode;
    }

    @Override
    public void onMoveEvent() {
        managePositioning();
    }

    @Override
    public void onZoomEvent() {
    }

    @Override
    public void onClick(View v) {
        updateMode(NavigationMode.Automatic, true);
        managePositioning();
    }

    @Subscribe
    public void onLocationChange(LocationChangeEvent e) {
        currentGpsPosition =  GpsRecorderService.locationToLatLong(e.location);
        managePositioning();
    }


    private boolean shouldFocus() {
        return  currentGpsPosition != null
                && (!focusedInitially || navigationMode == NavigationMode.Automatic);
    }

    private boolean distanceThresholdExceeds() {
        assert (navigationModeListener != null);

        if (currentGpsPosition == null) {
            return false;
        }

        final LatLong center = navigationModeListener.onGetCenter();
        final double distanceMeter = Math.abs(currentGpsPosition.sphericalDistance(center));

        return distanceMeter > 50;
    }

    private void managePositioning() {
        if (navigationModeListener == null) {
            return;
        }

        if (currentGpsPosition != null) {
            announcePositionUpdate();
            announceNavigationMode();
        }
    }

    private void announcePositionUpdate() {
        assert (navigationModeListener != null);

        if (shouldFocus())
        {
            navigationModeListener.navigateToPosition(currentGpsPosition);

            if (!focusedInitially) {
                focusedInitially = true;
            }
        }
    }

    private void announceNavigationMode() {
        assert (navigationModeListener != null);

        if (navigationMode != null && navigationModeUpdateReq) {
            navigationModeListener.onNavigationModeChanged(navigationMode);
            navigationModeUpdateReq = false;
        }
    }
}
