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

import androidx.annotation.DrawableRes;

import de.tadris.fitness.R;

public enum Icon {

    RUNNING("running", R.drawable.ic_run),
    WALKING("walking", R.drawable.ic_walk),
    CYCLING("cycling", R.drawable.ic_bike),
    INLINE_SKATING("inline_skating", R.drawable.ic_inline_skating),
    SKATEBOARDING("skateboarding", R.drawable.ic_skateboarding),
    ROWING("rowing", R.drawable.ic_rowing),
    BIKE_SCOOTER("bike_scooter", R.drawable.ic_bike_scooter),
    CAR("car", R.drawable.ic_car),
    E_BIKE("e-bike", R.drawable.ic_e_bike),
    E_SCOOTER("e-scooter", R.drawable.ic_e_scooter),
    FOLLOW_SIGN("follow-sign", R.drawable.ic_follow_sign),
    MOPED("moped", R.drawable.ic_moped),
    POOL("pool", R.drawable.ic_pool),
    BALL("ball", R.drawable.ic_ball),
    AMERICAN_FOOTBALL("american-football", R.drawable.ic_american_football),
    GOLF("golf", R.drawable.ic_golf),
    HANDBALL("handball", R.drawable.ic_handball),
    MOTOR_SPORTS("motor-sports", R.drawable.ic_motorsports),
    MOTOR_CYCLE("motor-cycle", R.drawable.ic_motor_cycle),
    ADD("add", R.drawable.ic_add_white),
    OTHER("other", R.drawable.ic_other);

    public final String name;

    @DrawableRes
    public final int iconRes;

    Icon(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
    }

    @DrawableRes
    public static int getIcon(String iconName) {
        for (Icon icon : values()) {
            if (icon.name.equals(iconName)) {
                return icon.iconRes;
            }
        }
        return OTHER.iconRes;
    }

}
