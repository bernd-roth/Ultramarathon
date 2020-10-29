/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.util;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class DataManager {

    public static void cleanFilesASync(Context context) {
        new Thread(() -> cleanFiles(context)).start();
    }

    public static void cleanFiles(Context context) {
        File dir = new File(getSharedDirectory(context));
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (file.delete()) {
                    Log.d("DataManager", "Deleted file " + file.getPath());
                } else {
                    Log.d("DataManager", "Could not delete file " + file.getPath());
                }
            }
        }
    }

    public static String getSharedDirectory(Context context) {
        return context.getFilesDir().getAbsolutePath() + "/shared";
    }

}
