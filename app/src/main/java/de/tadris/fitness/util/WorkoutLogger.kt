/*
 * Copyright (c) 2022 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.util

import android.content.Context
import android.util.Log
import de.tadris.fitness.Instance
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class WorkoutLogger(context: Context) {

    companion object {

        @JvmStatic
        fun log(tag: String, message: String){
            val instance = Instance.getInstance()
            if(instance != null){
                instance.logger.info(tag, message)
            }else{
                Log.w("Logger", "Couldn't log output")
                Log.i(tag, message)
            }
        }

    }

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val currentDate get() = formatter.format(Date())

    val file = File(context.filesDir, "recorder.log")
    private val writer = FileOutputStream(file, true).bufferedWriter()

    fun info(tag: String, message: String){
        appendLine("[$currentDate][$tag] $message")
        Log.i(tag, message)
    }

    private fun appendLine(line: String){
        writer.write(line)
        writer.newLine()
        writer.flush()
    }

}