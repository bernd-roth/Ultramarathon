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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sisyphsu.dateparser.DateParserUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.gpx.Gpx;
import de.tadris.fitness.util.gpx.Track;
import de.tadris.fitness.util.gpx.TrackPoint;
import de.tadris.fitness.util.gpx.TrackPointExtensions;
import de.tadris.fitness.util.gpx.TrackSegment;
import de.tadris.fitness.util.io.general.IWorkoutImporter;

public class GpxImporter implements IWorkoutImporter {
    @Override
    public WorkoutImportResult readWorkout(InputStream input) throws IOException {
        Gpx gpx = getGpx(input);

        if (gpx.getTrk().size() == 0
                || gpx.getTrk().get(0).getTrkseg().size() == 0
                || gpx.getTrk().get(0).getTrkseg().get(0).getTrkpt().size() == 0) {
            throw new IllegalArgumentException("given GPX file does not contain location data");
        }
        Track track = gpx.getTrk().get(0);
        TrackSegment firstSegment = track.getTrkseg().get(0);
        TrackPoint firstPoint = firstSegment.getTrkpt().get(0);

        Workout workout = new Workout();
        workout.comment = track.getName();
        if (gpx.getMetadata() != null) {
            if (workout.comment == null) {
                workout.comment = gpx.getName();
            }
            if (workout.comment == null) {
                workout.comment = gpx.getMetadata().getName();
            }
            if (workout.comment == null) {
                workout.comment = gpx.getMetadata().getDesc();
            }
        }

        String startTime = firstPoint.getTime();

        workout.start = parseDate(startTime).getTime();

        int index = firstSegment.getTrkpt().size();
        String lastTime = firstSegment.getTrkpt().get(index - 1).getTime();
        workout.end = parseDate(lastTime).getTime();
        workout.duration = workout.end - workout.start;
        workout.workoutTypeId = getTypeById(gpx.getTrk().get(0).getType()).id;

        List<WorkoutSample> samples = getSamplesFromTrack(workout.start, gpx.getTrk().get(0));

        return new WorkoutImportResult(workout, samples);
    }

    private static Gpx getGpx(InputStream input) throws IOException {
        XmlMapper mapper= new XmlMapper();
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        return mapper.readValue(input, Gpx.class);
    }

    private static List<WorkoutSample> getSamplesFromTrack(long startTime, Track track) {
        List<WorkoutSample> samples = new ArrayList<>();

        for (TrackSegment segment : track.getTrkseg()) {
            samples.addAll(getSamplesFromTrackSegment(startTime, segment));
        }

        return samples;
    }

    private static List<WorkoutSample> getSamplesFromTrackSegment(long startTime, TrackSegment segment) {
        List<WorkoutSample> samples = new ArrayList<>();
        for (TrackPoint point : segment.getTrkpt()) {
            WorkoutSample sample = new WorkoutSample();
            sample.absoluteTime = parseDate(point.getTime()).getTime();
            sample.elevation = point.getEle();
            sample.lat = point.getLat();
            sample.lon = point.getLon();
            sample.relativeTime = sample.absoluteTime - startTime;
            TrackPointExtensions extensions = point.getExtensions();
            if (extensions != null) {
                sample.speed = extensions.getSpeed();
                if (extensions.getGpxTpxExtension() != null) {
                    sample.heartRate = extensions.getGpxTpxExtension().getHr();
                }
            }
            samples.add(sample);
        }
        return samples;
    }

    private static Date parseDate(String str) {
        return DateParserUtils.parseDate(str);
    }

    private static WorkoutType getTypeById(String id) {
        if (id == null) {
            id = "";
        }
        switch (id) {
            // Strava IDs
            case "1":
                return WorkoutType.RUNNING;
            case "2":
                return WorkoutType.CYCLING;
            case "11":
                return WorkoutType.WALKING;

            default:
                return WorkoutType.getTypeById(id);
        }
    }
}