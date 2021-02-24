package de.tadris.fitness.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;

import de.tadris.fitness.Instance;

/**
 * This class provides an easier interface to the {@link ToneGenerator} class.
 *
 * It uses the currently configured volume for the selected audio stream and makes sure
 * {@link ToneGenerator}s are cleaned up, when they're not longer needed. Also it takes care of
 * muting the {@link ToneGenerator} during calls, if configured in TTS preferences.
 */
public class ToneGeneratorController {
    private boolean enabled;
    private boolean suppressOnCall;

    private ToneGenerator toneGenerator;
    private int streamVolumeRange;
    private final int audioStream;
    private static final int toneGeneratorVolumeRange = ToneGenerator.MAX_VOLUME - ToneGenerator.MIN_VOLUME;

    private final Context context;
    private final Instance instance;
    private final AudioManager audioManager;

    /**
     * Build an instance.
     * @param context the context it should run in
     * @param instance needed for user prefs
     * @param stream select an audio stream to use for playing tones (e.g. {@link AudioManager.STREAM_NOTIFICATION})
     */
    public ToneGeneratorController(Context context, Instance instance, int stream) {
        toneGenerator = null;
        this.context = context;
        this.instance = instance;
        audioStream = stream;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        suppressOnCall = instance.userPreferences.getSuppressAnnouncementsDuringCall();
        enabled = true;

        // determine audio stream volume range, needed to convert to ToneGenerator volume
        streamVolumeRange = audioManager.getStreamMaxVolume(audioStream);
        if (Build.VERSION.SDK_INT >= 28) {
            streamVolumeRange -= audioManager.getStreamMinVolume(audioStream);
        }
    }

    /**
     * Play a tone using the current audio stream.
     * @param tone the tone to play must be from {@link ToneGenerator.TONE_UNKNOWN} -
     *  {@link ToneGenerator.TONE_CDMA_SIGNAL_OFF}
     * @param millis how long do you want to play the tone (make sure this fits the selected tone)
     */
    public void playTone(int tone, int millis) {
        // don't play a sound when currently on a call or disabled entirely
        if (!enabled || (suppressOnCall && TelephonyHelper.isOnCall(context))) {
            return;
        }
        // otherwise we're good to go
        createToneGenerator().startTone(tone, millis);
    }

    /**
     * Create a {@link ToneGenerator} object with the correct stream and volume level.
     * @return tone generator instance
     */
    private ToneGenerator createToneGenerator() {
        // make sure to tare down the previous tone generator, otherwise phone crashes after a
        // certain amount of auto start sequences have been run
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        toneGenerator = new ToneGenerator(audioStream, getCurrentVolume());
        return toneGenerator;
    }

    /**
     * Get the current volume level for {@link ToneGenerator}.
     * @return volume level
     */
    private int getCurrentVolume() {
        int curVolumeLevel = audioManager.getStreamVolume(audioStream);
        return stream2generatorVolume(curVolumeLevel);
    }

    /**
     * Convert {@link AudioManager}'s notification stream volume to {@link ToneGenerator} volume.
     * @param streamVolume notification volume level
     * @return according {@link ToneGenerator} volume level
     */
    private int stream2generatorVolume(int streamVolume) {
        if (Build.VERSION.SDK_INT >= 28) {
            streamVolume -= audioManager.getStreamMinVolume(audioStream);
        }
        return streamVolume * toneGeneratorVolumeRange / streamVolumeRange + ToneGenerator.MIN_VOLUME;
    }
}
