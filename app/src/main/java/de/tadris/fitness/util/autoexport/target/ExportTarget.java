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

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.work.Constraints;

import java.io.File;

public interface ExportTarget {

    void exportFile(Context context, File file) throws Exception;

    String getId();

    @StringRes
    int getTitleRes();

    default Constraints getConstraints() {
        return new Constraints.Builder().build();
    }

    ExportTarget[] exportTargetTypes = new ExportTarget[]{
            new DirectoryTarget(null),
    };

    @Nullable
    static ExportTarget getExportTargetImplementation(String type, String data) {
        // TODO: check null-safety for usages
        switch (type) {
            case DirectoryTarget.TARGET_TYPE_DIRECTORY:
                return new DirectoryTarget(data);
            default:
                return null;
        }
    }

}
