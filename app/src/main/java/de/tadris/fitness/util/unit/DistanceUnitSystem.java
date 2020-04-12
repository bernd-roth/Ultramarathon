/*
 * Copyright (c) 2019 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.util.unit;

import androidx.annotation.StringRes;

public interface DistanceUnitSystem {

    double getDistanceFromMeters(double meters);
    double getMetersFromUnit(double distanceInUnit);
    double getDistanceFromKilometers(double kilometers);
    double getWeightFromKilogram(double kilogram);
    double getKilogramFromUnit(double unit);
    double getSpeedFromMeterPerSecond(double meterPerSecond);
    String getLongDistanceUnit();

    @StringRes
    int getLongDistanceUnitTitle(boolean isPlural);
    String getShortDistanceUnit();

    @StringRes
    int getShortDistanceUnitTitle(boolean isPlural);
    String getWeightUnit();
    String getSpeedUnit();

    @StringRes
    int getSpeedUnitTitle();

}
