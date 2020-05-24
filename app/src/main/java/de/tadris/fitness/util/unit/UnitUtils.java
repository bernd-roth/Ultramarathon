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

package de.tadris.fitness.util.unit;

import android.content.Context;

import androidx.annotation.StringRes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public abstract class UnitUtils {

    protected Context context;

    public UnitUtils(Context context) {
        this.context = context;
    }

    protected String getString(@StringRes int stringRes) {
        return context.getString(stringRes);
    }

    public static String round(double d, int count) {
        double value = Math.round(d * Math.pow(10, count)) / Math.pow(10, count);
        return String.valueOf(value).replaceAll("\\.", String.valueOf(getDecimalSeparator()));
    }

    protected static char getDecimalSeparator() {
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }


}
