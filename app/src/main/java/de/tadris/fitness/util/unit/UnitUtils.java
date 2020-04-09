package de.tadris.fitness.util.unit;

import android.content.Context;

import androidx.annotation.StringRes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

abstract class UnitUtils {

    protected Context context;

    public UnitUtils(Context context) {
        this.context = context;
    }

    protected String getString(@StringRes int stringRes) {
        return context.getString(stringRes);
    }

    protected String round(double d, int count) {
        double value = Math.round(d * Math.pow(10, count)) / Math.pow(10, count);
        return String.valueOf(value).replaceAll(",", String.valueOf(getDecimalSeparator()));
    }

    protected char getDecimalSeparator() {
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }


}
