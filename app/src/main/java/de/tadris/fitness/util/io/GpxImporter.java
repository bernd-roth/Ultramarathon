package de.tadris.fitness.util.io;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.util.gpx.Gpx;
import de.tadris.fitness.util.gpx.Track;
import de.tadris.fitness.util.gpx.TrackPoint;
import de.tadris.fitness.util.gpx.TrackSegment;

public class GpxImporter {
    public static void importWorkout(Context context, Uri uri) throws IOException {
        XmlMapper mapper= new XmlMapper();
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        Gpx gpx  = mapper.readValue(context.getContentResolver().openInputStream(uri), Gpx.class);
        getWorkoutFromGpx(context, gpx);
    }

    private static void getWorkoutFromGpx(Context context, Gpx gpx) {
        if(gpx.getCreator() != "FitoTrack")
        {
            // TODO: Show warning
        }
        Workout workout = new Workout();
        workout.comment = gpx.getName();
        String startTime;
        if(gpx.getMetadata().getTime() != null)
            startTime = gpx.getMetadata().getTime();
        else
            startTime = gpx.getTrk().get(0).getTrkseg().get(0).getTrkpt().get(0).getTime();
        workout.start = formatter.parse(startTime, new ParsePosition(0)).getTime();

        int index = gpx.getTrk().get(0).getTrkseg().get(0).getTrkpt().size();
        String time = gpx.getTrk().get(0).getTrkseg().get(0).getTrkpt().get(index-1).getTime();
        workout.end = formatter.parse(time, new ParsePosition(0)).getTime();
        workout.duration = workout.end - workout.start;
        workout.workoutTypeId = gpx.getTrk().get(0).getType();
        List<WorkoutSample> samples = getSamplesFromTrack(context, gpx.getTrk().get(0));

        new ImportWorkoutSaver(context, workout, samples).saveWorkout();
    }


    private static List<WorkoutSample> getSamplesFromTrack(Context context, Track track) {
        List<WorkoutSample> samples = new ArrayList<WorkoutSample>();

        TrackSegment segment = track.getTrkseg().get(0);

        for(TrackPoint point : segment.getTrkpt())
        {
            WorkoutSample sample = new WorkoutSample();
            sample.absoluteTime = formatter.parse(point.getTime(), new ParsePosition(0)).getTime();
            sample.elevation = point.getEle();
            sample.lat = point.getLat();
            sample.lon = point.getLon();
            sample.relativeTime = sample.absoluteTime;
            if(point.getExtensions() != null)
                sample.speed = point.getExtensions().getSpeed();
            samples.add(sample);
        }
        return samples;
    }

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static String getDateTime(long time) {
        return getDateTime(new Date(time));
    }

    private static String getDateTime(Date date) {
        return formatter.format(date);
    }
}
