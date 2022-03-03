package de.tadris.fitness.ui.record;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.view.InputListener;

import de.tadris.fitness.recording.event.LocationChangeEvent;
import de.tadris.fitness.recording.gps.GpsRecorderService;

public class NavigationModeHandler implements View.OnTouchListener, View.OnClickListener, InputListener {
    private boolean focusedInitially = false;
    private NavigationMode navigationMode = NavigationMode.Automatic;
    private boolean navigationModeUpdateReq = true;
    private NavigationModeListener navigationModeListener = null;
    private LatLong currentGpsPosition = null;
    private final MapView mapView;

    public enum NavigationMode {
        Automatic,
        Manual,
        ManualInScope
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
    };

    NavigationModeHandler(final MapView mapView) {
        this.mapView = mapView;
    }

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
                updateMode(NavigationMode.ManualInScope, UpdateMode.OnChange);
            }
            break;
            case MotionEvent.ACTION_UP: {
                if(distanceThresholdExceeds()){
                    updateMode(NavigationMode.Manual, UpdateMode.OnChange);
                } else {
                    updateMode(NavigationMode.Automatic, UpdateMode.OnChange);
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if (distanceThresholdExceeds()) {
                    updateMode(NavigationMode.Manual, UpdateMode.OnChange);
                }
            }
            break;
        }

        managePositioning();
        return false;
    }

    private enum UpdateMode {
        ForceUpdate,
        OnChange
    }

    private void updateMode(final NavigationMode mode, final UpdateMode strategy)
    {
        switch (strategy)
        {
            case ForceUpdate: {
                navigationModeUpdateReq = true;
            } break;
            case OnChange: {
                navigationModeUpdateReq = navigationMode != NavigationMode.Manual
                        && navigationMode != mode;
            }
        }

        if (navigationModeUpdateReq) {
            navigationMode = mode;
        }
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
        updateMode(NavigationMode.Automatic, UpdateMode.ForceUpdate);
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

        final LatLong center = mapView.getBoundingBox().getCenterPoint();
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
