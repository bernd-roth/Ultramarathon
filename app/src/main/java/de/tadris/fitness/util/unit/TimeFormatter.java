package de.tadris.fitness.util.unit;

public class TimeFormatter {

    public static String formatDuration(long duration)
    {
        String formatted="";

        duration = duration /1000;
        // get Hours (unlikely, but hey)
        if(duration / 3600 > 1)
            return String.format(
                    "%d:%02d:%02d",
                    duration / 3600,
                    (duration % 3600) / 60,
                    duration % 60);

        return String.format(
                "%d:%02d",
                (duration % 3600) / 60,
                duration % 60);
    }
}
