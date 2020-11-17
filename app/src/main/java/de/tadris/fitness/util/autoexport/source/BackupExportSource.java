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

package de.tadris.fitness.util.autoexport.source;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import de.tadris.fitness.export.BackupController;
import de.tadris.fitness.util.DataManager;

public class BackupExportSource implements ExportSource {

    @Override
    public File provideFile(Context context) throws Exception {
        return provideFile(context, BackupController.ExportStatusListener.DUMMY);
    }

    public File provideFile(Context context, BackupController.ExportStatusListener listener) throws Exception {
        String file = DataManager.getSharedDirectory(context) + "/backup" + System.currentTimeMillis() + ".ftb";
        File parent = new File(file).getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot write");
        }

        BackupController backupController = new BackupController(context, new File(file), listener);
        backupController.exportData();
        return new File(file);
    }
}
