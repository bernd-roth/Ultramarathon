package de.tadris.fitness.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.widget.ArrayAdapter
import de.tadris.fitness.R
import de.tadris.fitness.aggregation.WorkoutInformation
import de.tadris.fitness.aggregation.information.SummaryInformationType
import de.tadris.fitness.data.BaseWorkout

class SelectShareInformationDialog(private val ctx: Context, workout: BaseWorkout, private val slot: Int, private val callback: (slot: Int, informationType: SummaryInformationType) -> Unit) {

    private val informationList = SummaryInformationType.getAllImplementationsWithAvailableInformation(ctx, workout)

    fun show() {
        val builder = AlertDialog.Builder(ctx);
        val arrayAdapter = ArrayAdapter<String>(ctx, R.layout.select_dialog_singlechoice_material)
        informationList.forEach { arrayAdapter.add( ctx.getString(it.titleRes) ) }
        builder.setAdapter(arrayAdapter, this::handleOnSelectEvent)
        builder.create().show()
    }

    private fun handleOnSelectEvent(_dialog: Any, which: Int) {
        val selectedInformation = informationList[which]
        this.callback.invoke(slot, selectedInformation.type)
    }
}