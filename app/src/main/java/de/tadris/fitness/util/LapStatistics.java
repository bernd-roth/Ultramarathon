package de.tadris.fitness.util;

import java.util.ArrayList;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;

public class LapStatistics {

    public static class LapInfo {
        double dist=0, ascent=0, descent=0;
        long time=0;
        boolean fastest=false;
        boolean slowest=false;

        public LapInfo copy() {
            LapInfo info = new LapInfo();
                info.dist =dist;
                info.ascent = ascent;
                info.descent = descent;
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
        ASCENT,
        DESCENT
    }

    public static ArrayList<LapInfo> CreateLapList(Workout workout, java.util.List<WorkoutSample> samples, LapCriterion criterion, double laplength) {
        ArrayList<LapInfo> laps = new ArrayList<>();

        LapInfo fastest = null, slowest = null;

        WorkoutSample currentLapStart = samples.get(0);
        LapInfo currentInfo = new LapInfo();

        int lapsCount = 0;
        for (int i = 1; i < samples.size(); ++i) {
            WorkoutSample sample = samples.get(i);
            WorkoutSample previous = samples.get(i - 1);

            currentInfo.dist += sample.toLatLong().sphericalDistance(previous.toLatLong());
            currentInfo.time = sample.relativeTime - currentLapStart.relativeTime;
            if (sample.elevation < previous.elevation)
                currentInfo.descent += sample.elevation - previous.elevation;
            if (sample.elevation > previous.elevation)
                currentInfo.ascent += sample.elevation - previous.elevation;

            if (CheckCriterion(criterion, currentInfo, laplength)) {
                LapInfo saveInfo = currentInfo.copy();

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

                switch (criterion)
                {
                    case DISTANCE:
                        saveInfo.dist += lapsCount++ * laplength;
                        break;
                    case ASCENT:
                        saveInfo.ascent = laplength;
                        break;
                    case DESCENT:
                        saveInfo.descent = -laplength;
                        break;
                    case TIME:
                        //saveInfo.time = laplength;
                        break;
                }

                laps.add(saveInfo);
                double startDist = currentInfo.dist - laplength; // substract small overlap if distance can not be matche 100% (next lap starts a bit to late)
                currentInfo = new LapInfo();
                if(criterion == LapCriterion.DISTANCE)
                    currentInfo.dist = startDist;
                currentLapStart = sample;
            }
        }

        if(lapsCount > 0)
        {
            slowest.slowest = true;
            fastest.fastest = true;
        }

        if (currentInfo.dist != 0) {
            currentInfo.dist += lapsCount++ * laplength;
            currentInfo.time = samples.get(samples.size() - 1).relativeTime - currentLapStart.relativeTime;
            laps.add(currentInfo);
        }

        return laps;
    }

    private static boolean CheckCriterion(LapCriterion criterion, LapInfo lap, double laplength)
    {
        switch(criterion)
        {
            case ASCENT:
                return lap.ascent > laplength;
            case DESCENT:
                return -lap.descent > laplength;
            case TIME:
                return lap.time > laplength;
            case DISTANCE:
                return lap.dist > laplength;
        }
        return true;
    }
}
