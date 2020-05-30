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

package de.tadris.fitness.util.io;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.util.gpx.Gpx;
import de.tadris.fitness.util.gpx.Metadata;
import de.tadris.fitness.util.gpx.Track;
import de.tadris.fitness.util.gpx.TrackPoint;
import de.tadris.fitness.util.gpx.TrackPointExtension;
import de.tadris.fitness.util.gpx.TrackSegment;
import de.tadris.fitness.util.io.general.IWorkoutExporter;

public class GpxExporter implements IWorkoutExporter {

    @Override
    public void exportWorkout(Workout workout, List<WorkoutSample> samples, OutputStream fileStream) throws IOException {
        Gpx.formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        XmlMapper mapper= new XmlMapper();
        mapper.writeValue(fileStream, getGpxFromWorkout(workout, samples));
    }

    private static Gpx getGpxFromWorkout(Workout workout, List<WorkoutSample> samples) {
        Track track = getTrackFromWorkout(workout, samples, 0);
        ArrayList<Track> tracks = new ArrayList<>();
        tracks.add(track);
        Metadata meta = new Metadata(workout.toString(), workout.comment, getDateTime(workout.start));
        return new Gpx("1.1", "FitoTrack", meta, workout.toString(), workout.comment, tracks);
    }

    private static Track getTrackFromWorkout(Workout workout, List<WorkoutSample> samples, int number) {
        Track track= new Track();
        track.setNumber(number);
        track.setName(workout.toString());
        track.setCmt(workout.comment);
        track.setDesc(workout.comment);
        track.setSrc("FitoTrack");
        track.setType(workout.getWorkoutType().id);
        track.setTrkseg(new ArrayList<>());

        TrackSegment segment= new TrackSegment();
        ArrayList<TrackPoint> trkpt = new ArrayList<>();

        for(WorkoutSample sample : samples){
            trkpt.add(new TrackPoint(sample.lat, sample.lon, sample.elevation,
                    getDateTime(sample.absoluteTime), new TrackPointExtension(sample.speed)));
        }
        segment.setTrkpt(trkpt);

        ArrayList<TrackSegment> segments = new ArrayList<>();
        segments.add(segment);
        track.setTrkseg(segments);

        return track;
    }

    private static String getDateTime(long time) {
        return getDateTime(new Date(time));
    }

    private static String getDateTime(Date date) {
        return Gpx.formatter.format(date);
    }
}
