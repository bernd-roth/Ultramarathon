package de.tadris.fitness.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.tadris.fitness.data.UserPreferences;

public class DateTimeUtils {

    private UserPreferences preferences;

    public DateTimeUtils(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public String formatDateTime(Date date) {
        return formatDate(date) + " " + formatTime(date);
    }

    public String formatDate(Date date) {
        String pattern = preferences.getDateFormatSetting();
        if (pattern.equals("system")) {
            return SimpleDateFormat.getDateInstance().format(date);
        } else {
            return format(date, pattern);
        }
    }

    public String formatTime(Date date) {
        String mode = preferences.getTimeFormatSetting();
        switch (mode) {
            default:
            case "system":
                return SimpleDateFormat.getTimeInstance().format(date);
            case "12":
                return new SimpleDateFormat("h:mm a").format(date).toUpperCase();
            case "24":
                return new SimpleDateFormat("HH:mm").format(date);
        }
    }

    public String format(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

}
