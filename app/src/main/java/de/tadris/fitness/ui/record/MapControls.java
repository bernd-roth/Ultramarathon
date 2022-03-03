package de.tadris.fitness.ui.record;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.view.MapView;

public class MapControls implements NavigationModeHandler.NavigationModeListener {

    private final int MSG_ZOOM_CONTROLS_HIDE = 0;
    private final int ZOOM_CONTROLS_TIMEOUT = 2000;

    private MapView mapView;
    private FloatingActionButton mapFocusGpsBtn;
    private FloatingActionButton mapZoomInBtn;
    private FloatingActionButton mapZoomOutBtn;
    private NavigationModeHandler.NavigationMode navigationMode = NavigationModeHandler.NavigationMode.Automatic;
    private NavigationModeHandler.NavigationMode prevNavigationMode;

    private final Handler zoomControlsHideHandler;
    private final NavigationModeHandler navigationModeHandler;
    private boolean zoomControlsShown = false;
    private boolean focusBtnNeeded = false;

    public MapControls(MapView mapview, FloatingActionButton mapFocusGpsBtn,
                       FloatingActionButton mapZoomInBtn,
                       FloatingActionButton mapZoomOutBtn) {
        this.mapView = mapview;
        this.mapFocusGpsBtn = mapFocusGpsBtn;
        this.mapZoomInBtn = mapZoomInBtn;
        this.mapZoomOutBtn = mapZoomOutBtn;
        navigationModeHandler = new NavigationModeHandler(mapView);

        this.zoomControlsHideHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (navigationMode == NavigationModeHandler.NavigationMode.Automatic)
                {
                    MapControls.this.hide();
                }

                return true;
            }
        });
    }

    void init() {
        navigationModeHandler.init();
        navigationModeHandler.setNavigationModeListener(this);

        mapFocusGpsBtn.setVisibility(View.GONE);
        mapFocusGpsBtn.setOnClickListener(navigationModeHandler);

        mapZoomInBtn.setVisibility(View.GONE);
        mapZoomInBtn.setOnClickListener((View v) -> {
            final byte currentZoomLevel = mapView.getModel().mapViewPosition.getZoomLevel();
            mapView.setZoomLevel((byte) (currentZoomLevel - 1));
            showZoomControlsWithTimeout();
        });

        mapZoomOutBtn.setVisibility(View.GONE);
        mapZoomOutBtn.setOnClickListener((View v) -> {
            final byte currentZoomLevel = mapView.getModel().mapViewPosition.getZoomLevel();
            mapView.setZoomLevel((byte) (currentZoomLevel + 1));
            showZoomControlsWithTimeout();
        });

        mapView.setClickable(true);
        mapView.setOnTouchListener(navigationModeHandler);
        mapView.addInputListener(navigationModeHandler);
    }

    void deinit(){
        navigationModeHandler.removeNavigationModeListener();
        navigationModeHandler.deinit();
    }

    private void fadeButton(FloatingActionButton btn, int visibility, float startAlpha, float endAlpha) {
        AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
        anim.setDuration(500);

        btn.startAnimation(anim);
        btn.setVisibility(visibility);
    }

    private void show() {
        final float startAlpha = zoomControlsShown ? 1.0f : 0.0f;
        zoomControlsShown = true;
        if (focusBtnNeeded) {
            fadeButton(mapFocusGpsBtn, View.VISIBLE, startAlpha, 1.0f);
        } else {
            fadeButton(mapFocusGpsBtn, View.GONE, startAlpha, 0.0f);
        }

        fadeButton(mapZoomInBtn, View.VISIBLE, startAlpha, 1.0f);
        fadeButton(mapZoomOutBtn, View.VISIBLE, startAlpha, 1.0f);
    }

    private void hide() {
        zoomControlsShown = false;

        final float startAlpha = zoomControlsShown ? 1.0f : 0.0f;

        fadeButton(mapFocusGpsBtn, View.GONE, startAlpha, 0.0f);
        fadeButton(mapZoomInBtn, View.GONE, startAlpha, 0.0f);
        fadeButton(mapZoomOutBtn, View.GONE, startAlpha, 0.0f);
    }

    private void showZoomControls() {
        this.zoomControlsHideHandler.removeMessages(MSG_ZOOM_CONTROLS_HIDE);
        this.show();
    }

    private void showZoomControlsWithTimeout() {
        showZoomControls();
        this.zoomControlsHideHandler.removeMessages(MSG_ZOOM_CONTROLS_HIDE);
        boolean sent = this.zoomControlsHideHandler.sendEmptyMessageDelayed(MSG_ZOOM_CONTROLS_HIDE, ZOOM_CONTROLS_TIMEOUT);
        assert(sent);
    }


    @Override
    public void navigateToPosition(final LatLong navigateTo) {
        assert(navigateTo != null);
        mapView.getModel().mapViewPosition.animateTo(navigateTo);
    }


    @Override
    public void onNavigationModeChanged(final NavigationModeHandler.NavigationMode mode) {
        this.prevNavigationMode = this.navigationMode;
        this.navigationMode = mode;

        switch(mode){
            case ManualInScope:
                focusBtnNeeded = false;
                showZoomControls();
                break;
            case Automatic:
                focusBtnNeeded = false;
                showZoomControlsWithTimeout();
                break;
            case Manual:
                focusBtnNeeded = true;
                showZoomControls();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mode);
        }
    }

}