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
    AverageHeartRate("TODO", R.string.workoutAvgHeartRate),
    AverageMotionSpeed("TODO", R.string.avgSpeedInMotion),
    AveragePace("TODO", R.string.workoutPace),
    AverageTotalSpeed("TODO", R.string.avgSpeedTotalShort),
    BurnedEnergy("TODO", R.string.workoutBurnedEnergy),
    Distance("TODO", R.string.workoutDistance),
    Duration("TODO", R.string.workoutDuration),
    EnergyConsumption("TODO", R.string.workoutEnergyConsumption),
    Repetitions("TODO", R.string.workoutRepetitions),
    TopSpeed("TODO", R.string.workoutTopSpeed),
    WorkoutCount("TODO", R.string.workoutNumber);

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