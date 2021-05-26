package de.tadris.fitness.recording.indoor.exercise

import de.tadris.fitness.recording.indoor.FitoTrackSensorOption
import org.greenrobot.eventbus.EventBus

/**
 * Subclasses recognize repetitions in an indoor exercise. These can be steps, sit-ups, push-ups, etc.
 * When a repetition is recognized they must broadcast a RepetitionRecognizedEvent to the EventBus.
 * The event will then further processed by the IndoorWorkoutRecorder
 */
abstract class ExerciseRecognizer {

    fun start() {
        EventBus.getDefault().register(this)
    }

    fun stop() {
        EventBus.getDefault().unregister(this)
    }

    abstract fun getActivatedSensors(): List<FitoTrackSensorOption>

    companion object {

        fun findByType(typeId: String): ExerciseRecognizer? {
            when (typeId) {
                "treadmill" -> return StepRecognizer()
                else -> return null
            }
        }

    }

    class RepetitionRecognizedEvent(val timestamp: Long, val intensity: Double = 0.0)

}