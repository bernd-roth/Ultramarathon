package de.tadris.fitness.aggregation.information

import android.content.Context
import de.tadris.fitness.R
import de.tadris.fitness.aggregation.AggregationType
import de.tadris.fitness.data.GpsWorkout

class Hidden(ctx: Context) : GpsWorkoutInformation(ctx) {
    override fun getTitleRes(): Int {
        return R.string.hidden
    }

    override fun getUnit(): String {
        return ""
    }

    override fun getType(): SummaryInformationType {
        return SummaryInformationType.Hidden
    }

    override fun getValueFromWorkout(workout: GpsWorkout?): Double {
        return 0.0
    }

    override fun getAggregationType(): AggregationType {
        // Hacky. But this should only be used when Sharing, so hopefully this doesn't matter.
        return AggregationType.SUM
    }
}