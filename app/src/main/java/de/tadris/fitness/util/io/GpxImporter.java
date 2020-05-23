package de.tadris.fitness.util.io;

import android.annotation.SuppressLint;
import android.util.Pair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.util.gpx.Gpx;
import de.tadris.fitness.util.gpx.Track;
import de.tadris.fitness.util.gpx.TrackPoint;
import de.tadris.fitness.util.gpx.TrackSegment;
import de.tadris.fitness.util.io.general.IWorkoutImporter;

public class GpxImporter implements IWorkoutImporter {
    @Override
    public WorkoutImportResult readWorkout(InputStream input) throws IOException {
        Gpx gpx = getGpx(input);

        if(!gpx.getCreator().equals("FitoTrack"))
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
        List<WorkoutSample> samples = getSamplesFromTrack(gpx.getTrk().get(0));

        return new WorkoutImportResult(workout, samples);
    }

    private static Gpx getGpx(InputStream input) throws IOException {
        XmlMapper mapper= new XmlMapper();
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        return mapper.readValue(input, Gpx.class);
    }

    private static List<WorkoutSample> getSamplesFromTrack(Track track) {
        List<WorkoutSample> samples = new ArrayList<>();

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
}
