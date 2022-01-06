package de.tadris.fitness.recording.event;

import de.tadris.fitness.recording.gps.GpsRecorderService;

public class HRBatteryLevelConnectionEvent {
    public final GpsRecorderService.HRBatteryConnectionState state;

    public HRBatteryLevelConnectionEvent(GpsRecorderService.HRBatteryConnectionState state) {
        this.state = state;
    }
}
