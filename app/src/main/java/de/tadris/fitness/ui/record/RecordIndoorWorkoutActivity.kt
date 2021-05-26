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
package de.tadris.fitness.ui.record

import android.os.Bundle
import android.widget.TextView
import de.tadris.fitness.Instance
import de.tadris.fitness.R
import de.tadris.fitness.data.WorkoutType
import de.tadris.fitness.recording.BaseRecorderService
import de.tadris.fitness.recording.BaseWorkoutRecorder
import de.tadris.fitness.recording.indoor.IndoorRecorderService
import de.tadris.fitness.recording.indoor.IndoorWorkoutRecorder
import de.tadris.fitness.recording.indoor.exercise.ExerciseRecognizer
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RecordIndoorWorkoutActivity : RecordWorkoutActivity() {

    private lateinit var repetitionsText: TextView
    private lateinit var exerciseText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workoutType = intent.getSerializableExtra(WORKOUT_TYPE_EXTRA)
        if (workoutType is WorkoutType) {
            activity = workoutType
            if (instance.recorder != null && instance.recorder.state != BaseWorkoutRecorder.RecordingState.IDLE) {
                instance.recorder.stop()
                saveIfNotSaved()
            }
            instance.recorder = IndoorWorkoutRecorder(applicationContext, activity)
        }

        initBeforeContent()
        setContentView(R.layout.activity_record_indoor_workout)
        repetitionsText = findViewById(R.id.indoorRecordingReps)
        exerciseText = findViewById(R.id.indoorRecordingType)
        initAfterContent()

        updateStartButton(true, R.string.start) { start() }

        setTitle(R.string.recordWorkout) // TODO
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRepetitionRecognized(event: ExerciseRecognizer.RepetitionRecognizedEvent) {
        refreshRepetitions()
    }

    private fun refreshRepetitions() {
        val recorder = Instance.getInstance(this).recorder as IndoorWorkoutRecorder
        repetitionsText.text = recorder.repetitionsTotal.toString()
        exerciseText.text = resources.getQuantityString(activity.repeatingExerciseName, recorder.repetitionsTotal)
    }

    public override fun getServiceClass(): Class<out BaseRecorderService?> {
        return IndoorRecorderService::class.java
    }

    override fun onListenerStart() {
        // TODO
    }
}