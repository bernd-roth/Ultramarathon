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

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
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

    companion object {
        const val REQUEST_CODE_ACTIVITY_PERMISSION = 12
    }

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

        checkPermissions()

        updateStartButton(true, R.string.start) { start() }

        setTitle(R.string.recordWorkout) // TODO
    }

    override fun onResume() {
        super.onResume()
        refreshRepetitions()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasPermission()) {
            showActivityPermissionConsent()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showActivityPermissionConsent() {
        AlertDialog.Builder(this)
            .setTitle(R.string.recordingPermissionNotGrantedTitle)
            .setMessage(R.string.recordingActivityPermissionMessage)
            .setPositiveButton(R.string.actionGrant) { _, _ -> requestActivityPermission() }
            .setNegativeButton(R.string.cancel) { _, _ -> activityFinish() }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestActivityPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION
            ), REQUEST_CODE_ACTIVITY_PERMISSION
        )
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_ACTIVITY_PERMISSION) {
            if (!hasPermission()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.recordingPermissionNotGrantedTitle)
                    .setMessage(R.string.recordingActivityPermissionMessage)
                    .setPositiveButton(R.string.settings) { _, _ -> openSystemSettings() }
                    .create().show()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRepetitionRecognized(event: ExerciseRecognizer.RepetitionRecognizedEvent) {
        refreshRepetitions()
    }

    private fun refreshRepetitions() {
        val recorder = Instance.getInstance(this).recorder as IndoorWorkoutRecorder
        repetitionsText.text = recorder.repetitionsTotal.toString()
        exerciseText.text =
            resources.getQuantityString(activity.repeatingExerciseName, recorder.repetitionsTotal)
    }

    public override fun getServiceClass(): Class<out BaseRecorderService?> {
        return IndoorRecorderService::class.java
    }

    override fun onListenerStart() {
        // TODO
    }
}