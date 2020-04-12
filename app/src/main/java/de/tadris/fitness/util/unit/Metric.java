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

import de.tadris.fitness.R;

public class Metric implements DistanceUnitSystem {

    @Override
    public double getDistanceFromMeters(double meters) {
        return meters;
    }

    @Override
    public double getMetersFromShortUnit(double distanceInUnit) {
        return distanceInUnit;
    }

    @Override
    public double getMetersFromLongUnit(double distanceInUnit) {
        return distanceInUnit*1000;
    }

    @Override
    public double getDistanceFromKilometers(double kilometers) {
        return kilometers;
    }

    @Override
    public double getWeightFromKilogram(double kilogram) {
        return kilogram;
    }

    @Override
    public double getKilogramFromUnit(double unit) {
        return unit;
    }

    @Override
    public double getSpeedFromMeterPerSecond(double meterPerSecond) {
        return meterPerSecond * 3.6;
    }

    @Override
    public String getLongDistanceUnit() {
        return "km";
    }

    @Override
    public String getShortDistanceUnit() {
        return "m";
    }

    @Override
    public String getWeightUnit() {
        return "kg";
    }

    @Override
    public String getSpeedUnit() {
        return "km/h";
    }

    @Override
    public int getLongDistanceUnitTitle(boolean isPlural) {
        return isPlural ? R.string.unitKilometersPlural : R.string.unitKilometersSingular;
    }

    @Override
    public int getShortDistanceUnitTitle(boolean isPlural) {
        return isPlural ? R.string.unitMetersPlural : R.string.unitMetersSingular;
    }

    @Override
    public int getSpeedUnitTitle() {
        return R.string.unitKilometersPerHour;
    }
}
