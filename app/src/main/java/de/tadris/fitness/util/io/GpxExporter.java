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

import android.annotation.SuppressLint;
import android.content.Context;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.util.gpx.Gpx;
import de.tadris.fitness.util.gpx.Metadata;
import de.tadris.fitness.util.gpx.Track;
import de.tadris.fitness.util.gpx.TrackPoint;
import de.tadris.fitness.util.gpx.TrackPointExtension;
import de.tadris.fitness.util.gpx.TrackSegment;

public class GpxExporter {

    public static void exportWorkout(Context context, Workout workout, File file) throws IOException {
        XmlMapper mapper= new XmlMapper();
        mapper.writeValue(file, getGpxFromWorkout(context, workout));
    }

    private static Gpx getGpxFromWorkout(Context context, Workout workout) {
        Track track = getTrackFromWorkout(context, workout, 0);
        ArrayList<Track> tracks = new ArrayList<>();
        tracks.add(track);
        Metadata meta = new Metadata(workout.toString(), workout.comment, getDateTime(workout.start));
        Gpx gpx= new Gpx("1.1", "FitoTrack", meta, workout.toString(), workout.comment, tracks);
        return gpx;
    }

    private static Track getTrackFromWorkout(Context context, Workout workout, int number) {
        WorkoutSample[] samples= Instance.getInstance(context).db.workoutDao().getAllSamplesOfWorkout(workout.id);
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

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static String getDateTime(long time) {
        return getDateTime(new Date(time));
    }

    private static String getDateTime(Date date) {
        return formatter.format(date);
    }




}
