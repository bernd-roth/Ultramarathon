package de.tadris.fitness.data;

public class StatsDataTypes {
    public static class DataPoint {
        public WorkoutType workoutType;
        public long workoutID;
        public long time;
        public double value;

        public DataPoint(WorkoutType type, long id, long start, double value) {
            this.workoutType = type;
            this.workoutID=id;
            this.time = start;
            this.value = value;
        }
    }

    public static class TimeSpan {
        public long startTime;
        public long endTime;

        public boolean contains(long time) {
            return startTime <= time && time <= endTime;
        }
    }
}
