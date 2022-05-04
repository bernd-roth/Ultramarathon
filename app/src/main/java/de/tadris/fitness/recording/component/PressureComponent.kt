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

package de.tadris.fitness.recording.component

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import de.tadris.fitness.recording.RecorderService
import de.tadris.fitness.recording.event.PressureChangeEvent
import org.greenrobot.eventbus.EventBus

class PressureComponent : RecorderServiceComponent, SensorEventListener {

    companion object {
        const val TAG = "PressureComponent"
    }

    private lateinit var service: RecorderService
    private var pressureSensor: Sensor? = null

    override fun register(service: RecorderService) {
        this.service = service
        Log.i(TAG, "initializePressureSensor")
        pressureSensor = service.sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
        if(pressureSensor != null){
            Log.i(TAG, "started Pressure Sensor")
            service.sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }else{
            Log.i(TAG, "no Pressure Sensor Available")
        }
    }

    override fun unregister() {
        service.sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        EventBus.getDefault().post(PressureChangeEvent(event.values[0]))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}