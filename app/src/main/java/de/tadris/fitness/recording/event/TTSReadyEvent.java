package de.tadris.fitness.recording.event;

public class TTSReadyEvent {

    public final boolean ttsAvailable;

    public TTSReadyEvent(boolean ttsAvailable) {
        this.ttsAvailable = ttsAvailable;
    }
}
