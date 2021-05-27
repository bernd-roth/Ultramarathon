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
            return when (typeId) {
                "treadmill" -> StepRecognizer()
                "rope_skipping" -> JumpRecognizer(maxJumpDuration = 1250)
                "trampoline_jumping" -> JumpRecognizer(maxJumpDuration = 2500)
                else -> null
            }
        }

    }

    class RepetitionRecognizedEvent(val timestamp: Long, val intensity: Double = 0.0)

}