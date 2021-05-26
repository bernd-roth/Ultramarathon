package de.tadris.fitness.recording.indoor.exercise

import android.hardware.Sensor
import android.hardware.SensorEvent
import de.tadris.fitness.recording.indoor.FitoTrackSensorOption
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class StepRecognizer : ExerciseRecognizer() {

    override fun getActivatedSensors() = listOf(FitoTrackSensorOption.STEPS)

    @Subscribe
    fun onSensorEvent(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            EventBus.getDefault().post(RepetitionRecognizedEvent(event.timestamp))
        }
    }

}