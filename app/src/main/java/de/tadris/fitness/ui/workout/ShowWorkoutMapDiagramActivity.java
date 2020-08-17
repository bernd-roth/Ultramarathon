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

package de.tadris.fitness.ui.workout;

import android.os.Bundle;

import de.tadris.fitness.R;

public class ShowWorkoutMapDiagramActivity extends WorkoutActivity {

    public static final String DIAGRAM_TYPE_EXTRA = "de.tadris.fitness.ShowWorkoutMapDiagramActivity.DIAGRAM_TYPE";

    public static final String DIAGRAM_TYPE_HEIGHT = "height";
    public static final String DIAGRAM_TYPE_SPEED = "speed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBeforeContent();

        setContentView(R.layout.activity_show_workout_map_diagram);
        initRoot();

        initAfterContent();

        fullScreenItems = true;
        addMap();
        map.setClickable(true);

        diagramsInteractive= true;
        root= findViewById(R.id.showWorkoutDiagramParent);
        switch (getIntent().getStringExtra(DIAGRAM_TYPE_EXTRA)) {
            case DIAGRAM_TYPE_HEIGHT:
                addHeightDiagram();
                break;
            default:
            case DIAGRAM_TYPE_SPEED:
                addSpeedDiagram();
                break;
        }

    }


    @Override
    void initRoot() {
        root = findViewById(R.id.showWorkoutMapParent);
    }
}