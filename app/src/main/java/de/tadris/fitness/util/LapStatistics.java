package de.tadris.fitness.util;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;

public class LapStatistics {

    public static class LapInfo {
        double dist=0, metersUp=0, metersDown=0;
        long time=0;
        boolean fastest=false;
        boolean slowest=false;

        public LapInfo copy() {
            LapInfo info = new LapInfo();
                info.dist =dist;
                info.metersUp =metersUp;
                info.metersDown =metersDown;
                info.time =time;
                info.fastest =fastest;
                info.slowest =slowest;
            return info;
        }
    }

    public enum LapCriterion // Currently only distance supported
    {
        DISTANCE,
        TIME,
        METERS_UP,
        METERS_DOWN,
        NUM_LAPS
    }

    public static List<Integer> Distances() {
        List<Integer> dists = new ArrayList<>();
        dists.add(100);
        dists.add(500);
        dists.add(1000);
        dists.add(2000);
        dists.add(5000);
        dists.add(10000);
        dists.add(21097);
        return dists;
    }

    public static ArrayList<LapInfo> CreateLapList(Workout workout, java.util.List<WorkoutSample> samples, LapCriterion criterion, double laplength) {
        ArrayList<LapInfo> laps = new ArrayList<>();

        if(criterion == LapCriterion.NUM_LAPS)
            laplength = workout.length/laplength; // Transform num laps to simple distance criterion

        LapInfo fastest = null, slowest = null;

        WorkoutSample currentLapStart = samples.get(0);
        LapInfo currentInfo = new LapInfo();

        int lapsCount = 0;
        for (int i = 1; i < samples.size(); ++i) {
            WorkoutSample sample = samples.get(i);
            WorkoutSample previous = samples.get(i - 1);

            currentInfo.dist += sample.toLatLong().sphericalDistance(previous.toLatLong());
            if (sample.elevation < previous.elevation)
                currentInfo.metersDown += sample.elevation - previous.elevation;
            if (sample.elevation > previous.elevation)
                currentInfo.metersUp += sample.elevation - previous.elevation;

            if (CheckCriterion(workout, criterion, currentInfo, laplength)) {
                LapInfo saveInfo = currentInfo.copy();
                saveInfo.time = sample.relativeTime - currentLapStart.relativeTime;
                saveInfo.dist += lapsCount++ * laplength;

                if (fastest == null) {
                    fastest = saveInfo;
                } else if (saveInfo.time < fastest.time) {
                    fastest = saveInfo;
                }

                if (slowest == null) {
                    slowest = saveInfo;
                } else if (saveInfo.time > slowest.time) {
                    slowest = saveInfo;
                }

                laps.add(saveInfo);
                double startDist = currentInfo.dist - laplength; // substract small overlap if distance can not be matche 100% (next lap starts a bit to late)
                currentInfo = new LapInfo();
                currentInfo.dist = startDist;
                currentLapStart = sample;
            }
        }
        slowest.slowest = true;
        fastest.fastest = true;

        if (currentInfo.dist != 0) {
            currentInfo.dist += lapsCount++ * laplength;
            currentInfo.time = samples.get(samples.size() - 1).relativeTime - currentLapStart.relativeTime;
            laps.add(currentInfo);
        }

        return laps;
    }

    private static boolean CheckCriterion(Workout workout, LapCriterion criterion, LapInfo lap, double laplength)
    {
        switch(criterion)
        {
            case METERS_DOWN: // not yet implemented
            case METERS_UP:
            case TIME:
            case DISTANCE:
            case NUM_LAPS:
                return lap.dist > laplength;
        }
        return true;
    }
}
