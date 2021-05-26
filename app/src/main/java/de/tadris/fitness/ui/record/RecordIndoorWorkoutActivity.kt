/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.ui.record;

import android.os.Bundle;

import de.tadris.fitness.R;
import de.tadris.fitness.recording.BaseRecorderService;
import de.tadris.fitness.recording.indoor.IndoorRecorderService;

public class RecordIndoorWorkoutActivity extends RecordWorkoutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBeforeContent();
        setContentView(R.layout.activity_record_indoor_workout);
        initAfterContent();

        // TODO setTitle();
    }

    @Override
    Class<? extends BaseRecorderService> getServiceClass() {
        return IndoorRecorderService.class;
    }

    @Override
    protected void onListenerStart() {
        // TODO
    }
}