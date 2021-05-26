package de.tadris.fitness.recording.indoor

import android.hardware.Sensor

enum class FitoTrackSensorOption(val sensorType: Int) {
    ACCELERATION(Sensor.TYPE_LINEAR_ACCELERATION),
    STEPS(Sensor.TYPE_STEP_DETECTOR)
}