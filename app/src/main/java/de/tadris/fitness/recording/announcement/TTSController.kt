/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tadris.fitness.recording.announcement

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import de.tadris.fitness.recording.BaseWorkoutRecorder
import de.tadris.fitness.recording.announcement.AnnouncementMode
import de.tadris.fitness.recording.event.TTSReadyEvent
import org.greenrobot.eventbus.EventBus
import java.util.*

class TTSController(context: Context, val id: String = DEFAULT_TTS_CONTROLLER_ID) {

    private val textToSpeech = TextToSpeech(context) { status: Int -> ttsReady(status) }

    var isTtsAvailable = false
        private set

    private val currentMode = AnnouncementMode.getCurrentMode(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun ttsReady(status: Int) {
        isTtsAvailable =
            status == TextToSpeech.SUCCESS && textToSpeech.setLanguage(Locale.getDefault()) >= 0
        if (isTtsAvailable) {
            textToSpeech.setOnUtteranceProgressListener(TextToSpeechListener())
        }
        EventBus.getDefault().post(TTSReadyEvent(isTtsAvailable, id))
    }

    fun speak(recorder: BaseWorkoutRecorder?, announcement: Announcement) {
        if (!announcement.isAnnouncementEnabled) {
            return
        }
        val text = announcement.getSpokenText(recorder!!)
        if (text != null && text != "") {
            speak(text)
        }
    }

    private var speakId = 1
    fun speak(text: String) {
        if (!isTtsAvailable) {
            // Cannot speak
            return
        }
        if (currentMode === AnnouncementMode.HEADPHONES && !isHeadsetOn) {
            // Not allowed to speak
            return
        }
        Log.d("Recorder", "TTS speaks: $text")
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "announcement" + ++speakId)
    }

    private val isHeadsetOn: Boolean
        get() {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val bluetoothHeadsetConnected =
                (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled
                        && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED)
            return audioManager.isWiredHeadsetOn || bluetoothHeadsetConnected
        }

    /**
     * Destroys the TTS instance immediately. Ongoing announcements might be aborted.<br></br>
     * Use [.destroyWhenDone] instead, if you don't want to abort ongoing announcements.
     */
    fun destroy() {
        textToSpeech.shutdown()
    }

    /**
     * Waits for the end of an ongoing announcement before the TTS instance is destroyed.<br></br>
     * Use [.destroy] instead, if you don't care about that.
     */
    fun destroyWhenDone() {
        val destroyTimer = Timer("TTS_Destroy")
        destroyTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (!textToSpeech.isSpeaking) {
                    destroy()
                    cancel()
                    destroyTimer.cancel()
                }
            }
        }, 20, 20)
    }

    private inner class TextToSpeechListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_SYSTEM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }

        override fun onDone(utteranceId: String) {
            audioManager.abandonAudioFocus(null)
        }

        override fun onError(utteranceId: String) {}
    }

    companion object {
        const val DEFAULT_TTS_CONTROLLER_ID = "TTSController"
    }
}