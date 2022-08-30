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

package de.tadris.fitness.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView
import android.widget.Toast
import com.fasterxml.jackson.databind.json.JsonMapper
import de.tadris.fitness.R
import de.tadris.fitness.util.isUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.concurrent.thread


class ConfigureFitTrackeeDialog(val activity: Activity, val onSubmit: (data: String) -> Unit) {

    private val dialog = AlertDialog.Builder(activity)
        .setView(R.layout.dialog_configure_fittrackee)
        .setTitle(R.string.configureFitTrackeeConnection)
        .setPositiveButton(R.string.okay, null)
        .create()!!

    init {
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                submit()
            }
        }
        dialog.show()
    }

    private val urlTextView = dialog.findViewById<TextView>(R.id.fittrackeeURL)!!
    private val emailTextView = dialog.findViewById<TextView>(R.id.fittrackeeEmail)!!
    private val passwordTextView = dialog.findViewById<TextView>(R.id.fittrackeePassword)!!

    private fun submit() {
        val url = urlTextView.text.toString()
        if (url.isUrl()) {
            obtainAuthToken()
        } else {
            urlTextView.error = activity.getString(R.string.enterValidUrl)
        }
    }

    private fun obtainAuthToken() {
        val dialog = ProgressDialogController(activity, activity.getString(R.string.obtainingToken))
        dialog.show()
        dialog.setIndeterminate(true)
        thread {
            try {
                val authToken = doLoginRequest()
                activity.runOnUiThread {
                    val data = urlTextView.text.toString() + "\n" + authToken
                    onSubmit(data)
                    this.dialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity.runOnUiThread {
                    Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_LONG).show()
                }
            } finally {
                activity.runOnUiThread {
                    dialog.cancel()
                }
            }
        }
    }

    private fun doLoginRequest(): String {
        val client = OkHttpClient()

        val url = urlTextView.text.toString()
        val separator = if (url.endsWith("/")) "" else "/"
        val apiUrl = "$url${separator}api/auth/login"

        val mapper = JsonMapper()

        val bodyContent =
            LoginRequestBody(emailTextView.text.toString(), passwordTextView.text.toString())
        val bodyContentJson = mapper.writeValueAsString(bodyContent)

        val request = Request.Builder()
            .url(apiUrl)
            .post(bodyContentJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (response.code != 200) {
            throw Exception("Response ${response.code}: ${response.body?.string()}")
        }

        val responseJson = response.body!!.string()
        val responseBody = mapper.readValue(responseJson, Map::class.java)
        return responseBody["auth_token"] as String
    }

    class LoginRequestBody(val email: String, val password: String)

}