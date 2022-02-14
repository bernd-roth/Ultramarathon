package de.tadris.fitness.data.preferences

import android.content.SharedPreferences
import android.util.Log
import de.tadris.fitness.aggregation.information.SummaryInformationType
import de.tadris.fitness.data.WorkoutType
import java.lang.Exception

private const val TAG = "SharingScreenInfoPrefs"
/**
 * Preferences within this class concern the Metrics (Information) shown on the Sharing
 * Activities
 */
class SharingScreenInformationPreferences(private val prefs: SharedPreferences) {

    @Throws(UnknownModeException::class)
    fun getIdOfDisplayedInformation(mode: String, slot: Int) : SummaryInformationType {
        val defaultValueEnum = when(mode) {
            WorkoutType.RecordingType.INDOOR.id -> this.getDefaultIndoorActivityId(slot)
            WorkoutType.RecordingType.GPS.id -> this.getDefaultGpsActivityId(slot)
            else -> throw UnknownModeException()
        }

        val preferenceKey = "information_display_share_${mode}_${slot}"
        val storedValueAsString = this.prefs.getString(preferenceKey, defaultValueEnum.id)!!

        // Try to parse. If this thrown an error, overwrite with a fallback value
        var storedValueAsEnum : SummaryInformationType

        try {
            storedValueAsEnum = SummaryInformationType.getEnumById(storedValueAsString)
        }
        catch (err: SummaryInformationType.Companion.UnknownTypeException) {
            // Unknown value! Fallback to a safe value.
            Log.e(TAG, "Value stored under \"$preferenceKey\" (\"$storedValueAsString\") " +
                    "could not be parsed into an Information Type. Falling back to \"$defaultValueEnum.id\" ")
            this.setIdOfDisplayedInformation(mode, slot, defaultValueEnum)
            storedValueAsEnum = defaultValueEnum
        }

        return storedValueAsEnum
    }

    fun setIdOfDisplayedInformation(mode: String, slot: Int, valueEnum: SummaryInformationType) {
        val preferenceKey = "information_display_share_${mode}_${slot}"
        this.prefs.edit().putString(preferenceKey, valueEnum.id).apply()
    }

    private fun getDefaultIndoorActivityId(slot: Int): SummaryInformationType {
        return when(slot) {
            0 -> SummaryInformationType.AveragePace
            1 -> SummaryInformationType.Duration
            2 -> SummaryInformationType.BurnedEnergy
            else -> SummaryInformationType.BurnedEnergy
        }
    }

    private fun getDefaultGpsActivityId(slot: Int): SummaryInformationType {
        return when(slot) {
            0 -> SummaryInformationType.Distance
            1 -> SummaryInformationType.TopSpeed
            2 -> SummaryInformationType.Duration
            else -> SummaryInformationType.AverageTotalSpeed
        }
    }

    class UnknownModeException : Exception()

}