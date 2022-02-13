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

package de.tadris.fitness.util.autoexport.target;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.tadris.fitness.R;

public class DirectoryTarget implements ExportTarget {

    private final String directoryUri;

    public DirectoryTarget(String directoryUri) {
        this.directoryUri = directoryUri;
    }

    @Override
    public void exportFile(Context context, File file) throws Exception {
        InputStream input = context.getContentResolver().openInputStream(Uri.fromFile(file));
        if (input == null) {
            throw new IOException("Source file not found");
        }

        Uri targetFolder = Uri.parse(directoryUri);

        DocumentFile directoryFile = DocumentFile.fromTreeUri(context, targetFolder);
        DocumentFile targetFile = directoryFile.createFile("application/*", file.getName());

        if (!targetFile.canWrite()) {
            throw new IOException("Cannot write to target file.");
        }

        OutputStream output = context.getContentResolver().openOutputStream(targetFile.getUri());
        if (output == null) {
            throw new IOException("Target file not found");
        }
        IOUtils.copy(input, output);
    }

    @Override
    public String getId() {
        return TARGET_TYPE_DIRECTORY;
    }

    @Override
    public int getTitleRes() {
        return R.string.exportTargetDirectory;
    }
}
