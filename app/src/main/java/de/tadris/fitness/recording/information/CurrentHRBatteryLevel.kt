package de.tadris.fitness.recording.information

import android.content.Context
import de.tadris.fitness.R
import de.tadris.fitness.recording.BaseWorkoutRecorder

class CurrentHRBatteryLevel(context: Context) : RecordingInformation(context) {

    override val id = "current_hr_battery_level"
    override val isEnabledByDefault = false
    override fun canBeDisplayed() = true

    override fun getTitle(): String {
        return getString(R.string.heartRateSensorBattery)
    }

    override fun getDisplayedText(recorder: BaseWorkoutRecorder): String {
        return if (isHRBatteryAvailable(recorder)) {
            recorder.currentHRBatteryLevel.toString() + getString(R.string.unitHeartRateSensorBattery)
        } else {
            "-" // No heart rate data available
        }
    }

    override fun getSpokenText(recorder: BaseWorkoutRecorder): String {
        return if (isHRBatteryAvailable(recorder)) {
            getTitle() + ": " + getDisplayedText(recorder)
        } else {
            getString(R.string.heartRateNotAvailable)
        }
    }

    private fun isHRBatteryAvailable(recorder: BaseWorkoutRecorder): Boolean {
        return recorder.currentHRBatteryLevel >= 0
    }
}