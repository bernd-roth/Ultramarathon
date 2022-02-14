package de.tadris.fitness.aggregation.information

import android.content.Context
import androidx.annotation.StringRes
import de.tadris.fitness.R
import de.tadris.fitness.aggregation.WorkoutInformation
import de.tadris.fitness.data.BaseWorkout


/**
 * This Enum defines all <b>Summary</b>-Type Metrics. There are values that can be used in an
 * activity summary.
 *
 * Note that these are different from "Live"-Information Values.
 */
enum class SummaryInformationType(val id: String, @StringRes val labelRes: Int) {
    AverageHeartRate("TODO-1", R.string.workoutAvgHeartRate),
    AverageMotionSpeed("TODO-2", R.string.avgSpeedInMotion),
    AveragePace("TODO-3", R.string.workoutPace),
    AverageTotalSpeed("TODO-4", R.string.avgSpeedTotalShort),
    BurnedEnergy("TODO-5", R.string.workoutBurnedEnergy),
    Distance("TODO-6", R.string.workoutDistance),
    Duration("TODO-7", R.string.workoutDuration),
    EnergyConsumption("TODO-8", R.string.workoutEnergyConsumption),
    Repetitions("TODO-9", R.string.workoutRepetitions),
    TopSpeed("TODO-10", R.string.workoutTopSpeed),
    WorkoutCount("TODO-11", R.string.workoutNumber),
    // This should be used if one of the TextViews should not display anything.
    Hidden("TODO-12", R.string.hidden);

    companion object {
        @JvmStatic
        @Throws(UnknownTypeException::class)
        fun getEnumById(id: String) : SummaryInformationType {
            return when (id) {
                AverageHeartRate.id -> AverageHeartRate
                AverageMotionSpeed.id -> AverageMotionSpeed
                AveragePace.id -> AveragePace
                AverageTotalSpeed.id -> AverageTotalSpeed
                BurnedEnergy.id -> BurnedEnergy
                Distance.id -> Distance
                Duration.id -> Duration
                EnergyConsumption.id -> EnergyConsumption
                Repetitions.id -> Repetitions
                TopSpeed.id -> TopSpeed
                WorkoutCount.id -> WorkoutCount
                else -> throw UnknownTypeException()
            }
        }

        /**
         * Get the Implementation based on type
         */
        @JvmStatic
        fun getImplementationFromType(ctx: Context, type: SummaryInformationType) : WorkoutInformation {
            return when (type) {
                AverageHeartRate -> AverageHeartRate(ctx)
                AverageMotionSpeed -> AverageMotionSpeed(ctx)
                AveragePace -> AveragePace(ctx)
                AverageTotalSpeed -> AverageTotalSpeed(ctx)
                BurnedEnergy -> BurnedEnergy(ctx)
                Distance -> Distance(ctx)
                Duration -> Duration(ctx)
                EnergyConsumption -> EnergyConsumption(ctx)
                Repetitions -> Repetitions(ctx)
                TopSpeed -> TopSpeed(ctx)
                WorkoutCount -> WorkoutCount(ctx)
                Hidden -> Hidden(ctx)
            }
        }

        @JvmStatic
        fun getAllImplementations(ctx: Context) : List<WorkoutInformation> {
            return listOf(
                    AverageHeartRate(ctx),
                    AverageMotionSpeed(ctx),
                    AveragePace(ctx),
                    AverageTotalSpeed(ctx),
                    BurnedEnergy(ctx),
                    Distance(ctx),
                    Duration(ctx),
                    EnergyConsumption(ctx),
                    Repetitions(ctx),
                    TopSpeed(ctx),
                    WorkoutCount(ctx),
            )
        }

        /**
         * Return <b>all</b> Implementations which, provided the workout, contain data
         * This is useful for letting the user choose which values to display on the Share Screen.
         */
        @JvmStatic
        fun getAllImplementationsWithAvailableInformation(ctx: Context, workout: BaseWorkout) : List<WorkoutInformation> {
            return this.getAllImplementations(ctx).filter { it.isInformationAvailableFor(workout)}
        }

        class UnknownTypeException : Exception()
    }
}