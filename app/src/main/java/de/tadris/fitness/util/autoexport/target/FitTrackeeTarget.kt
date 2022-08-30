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

package de.tadris.fitness.util.autoexport.target

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import de.tadris.fitness.R
import de.tadris.fitness.util.autoexport.source.ExportSource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class FitTrackeeTarget(private val url: String, private val token: String) : ExportTarget {

    constructor(data: List<String>) : this(data[0], data[1])

    /**
     * Data in the format "<url>\n<token>"
     */
    constructor(data: String) : this(data.split("\n"))

    override val id get() = ExportTarget.TARGET_TYPE_HTTP_POST

    override val titleRes get() = R.string.exportTargetFitTrackee

    override val constraints
        get() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    override fun exportFile(context: Context, file: ExportSource.ExportedFile) {
        val input = context.contentResolver.openInputStream(Uri.fromFile(file.file))
            ?: throw IOException("Source file not found")

        val separator = if (url.endsWith("/")) "" else "/"
        val apiUrl = "$url${separator}api/workouts"
        Log.d("FitTrackeeTarget", "Requesting: $apiUrl")

        val client = OkHttpClient()

        val reader = input.bufferedReader()
        val gpxString = reader.readText()
        reader.close()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.file.name,
                gpxString.toRequestBody("application/*".toMediaType())
            )
            .addFormDataPart("data", """{"sport_id": 1, "notes": ""}""")
            .build()

        val request = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        Log.d("FitTrackeeTarget", "Response code: " + response.code)
        if (response.code != 200) {
            Log.w("FitTrackeeTarget", "Error: " + response.body?.string())
        }
    }
}