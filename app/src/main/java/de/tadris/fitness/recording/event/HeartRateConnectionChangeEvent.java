package de.tadris.fitness.recording.event;

import de.tadris.fitness.recording.RecorderService;

public class HeartRateConnectionChangeEvent {

    public final RecorderService.HeartRateConnectionState state;

    public HeartRateConnectionChangeEvent(RecorderService.HeartRateConnectionState state) {
        this.state = state;
    }
}
