package de.tadris.fitness.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.data.WorkoutSample;

public class LapStatistics {

    public static class LaptimeInfo{
        int dist, metersUp, metersDown;
        long time;
        boolean fastest;
        boolean slowest;
    }

    public enum LapCriterion // Currently only distance supported
    {
        DISTANCE,
        TIME,
        METERS_UP,
        METERS_DOWN
    }

    public static List<Integer> Distances()
    {
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

    public static ArrayList<LaptimeInfo> CreateLapList(java.util.List<WorkoutSample> samples, LapCriterion criterion, double laplength)
    {
        ArrayList<LaptimeInfo> laps = new ArrayList<>();

        LaptimeInfo fastest=null, slowest=null;

        WorkoutSample currentLapStart = samples.get(0);
        double currentAccumulatedDistance = 0;
        double curMetersUp=0;
        double curMetersDown=0;

        int lapsCount=0;
        for (int i=1; i< samples.size(); ++i)
        {
            WorkoutSample sample = samples.get(i);
            WorkoutSample previous = samples.get(i-1);

            currentAccumulatedDistance += sample.toLatLong().sphericalDistance(previous.toLatLong());
            if(sample.elevation < previous.elevation)
                curMetersDown += sample.elevation -previous.elevation;
            if(sample.elevation > previous.elevation)
                curMetersUp += sample.elevation -previous.elevation;

            if (currentAccumulatedDistance > laplength)
            {
                LaptimeInfo info = new LaptimeInfo();
                info.dist = (int)(++lapsCount*laplength);
                info.time = sample.relativeTime - currentLapStart.relativeTime;
                info.metersDown = (int) curMetersDown;
                info.metersUp = (int) curMetersUp;

                if(fastest == null)
                {
                    info.fastest = true;
                    fastest = info;
                }
                else if(info.time < fastest.time)
                {

                    fastest.fastest = false;
                    info.fastest = true;
                    fastest = info;
                }

                if(slowest == null)
                {
                    info.slowest = true;
                    slowest = info;
                }
                else if(info.time > slowest.time)
                {
                    slowest.slowest = false;
                    info.slowest = true;
                    slowest = info;
                }

                laps.add(info);
                currentAccumulatedDistance -= laplength; // substract small overlap if distance can not be matche 100% (next lap starts a bit to late)
                curMetersDown=0;
                curMetersUp=0;
                currentLapStart = sample;
            }
        }

        if(currentAccumulatedDistance != 0)
        {
            LaptimeInfo info = new LaptimeInfo();
            info.dist = (int)(lapsCount*laplength+currentAccumulatedDistance);
            info.time = samples.get(samples.size()-1).relativeTime - currentLapStart.relativeTime;
            info.metersDown = (int) curMetersDown;
            info.metersUp = (int) curMetersUp;
            laps.add(info);
        }

        return laps;
    }
}
