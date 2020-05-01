package de.tadris.fitness.util.sections;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.R;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;

public class SectionListModel {
    Workout workout;
    List<WorkoutSample> samples;
    List<String> units = new ArrayList<>();
    SectionCriterion criterion = SectionCriterion.DISTANCE;
    double sectionLength = 1;
    int selectedUnitID = 1;
    boolean paceToggle = true;

    public SectionListModel(Workout workout, List<WorkoutSample> samples) {
        this.samples = samples;
        this.workout = workout;
    }

    public void setCriterion(SectionCriterion criterion) {
        this.criterion = criterion;
        switch (criterion) {
            case TIME:
                setSelectedUnitID(1); // default: min
                break;
            case DISTANCE:
                setSelectedUnitID(0); // default: km / miles
                break;
            case ASCENT:
            case DESCENT:
                setSelectedUnitID(1); // default : m
                break;
        }
    }

    public SectionCriterion getCriterion() {
        return criterion;
    }

    public double getSectionLength() {
        return sectionLength;
    }

    public void setSectionLength(double length) {
        this.sectionLength = length;
    }

    public int getSelectedUnitID() {
        return selectedUnitID;
    }

    public void setSelectedUnitID(int unitID) {
        selectedUnitID = unitID;
    }

    public boolean getPaceToggle() {
        return paceToggle;
    }

    public void setPaceToggle(boolean toggle) {
        paceToggle = toggle;
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setUnits(List<String> units) {
        this.units = units;
    }

    public List<String> getUnits() {
        return units;
    }

    public ArrayList<Section> getSectionList() {
        ArrayList<Section> sections = new ArrayList<>();

        Section currentSection = new Section();
        Section bestSection = currentSection, worstSection = currentSection;

        for (int i = 1; i < samples.size(); ++i) {
            WorkoutSample sample = samples.get(i);
            WorkoutSample previous = samples.get(i - 1);

            currentSection.dist += sample.toLatLong().sphericalDistance(previous.toLatLong());
            currentSection.time += sample.relativeTime - previous.relativeTime;
            if (sample.elevation < previous.elevation)
                currentSection.descent += previous.elevation - sample.elevation;
            if (sample.elevation > previous.elevation)
                currentSection.ascent += sample.elevation - previous.elevation;

            double currentCriteriaLength;
            switch (criterion) {
                case ASCENT:
                    currentCriteriaLength = currentSection.ascent;
                    break;
                case DESCENT:
                    currentCriteriaLength = currentSection.descent;
                    break;
                case TIME:
                    currentCriteriaLength = currentSection.time;
                    break;
                case DISTANCE:
                default:
                    currentCriteriaLength = currentSection.dist;
                    break;
            }

            if (currentCriteriaLength >= sectionLength) {
                // interpolate to find actual values
                double interpolate = sectionLength/currentCriteriaLength;
                Section saveSection = currentSection.copy();
                saveSection.dist *= interpolate;
                saveSection.time *= interpolate;
                saveSection.descent *= interpolate;
                saveSection.ascent *= interpolate;

                // check for best / worst
                if (isSectionBetter(saveSection, bestSection)) {
                    bestSection = saveSection;
                } else if (isSectionBetter(worstSection, saveSection)) {
                    worstSection = saveSection;
                }

                sections.add(saveSection);

                // Set start values for new section considering inertpolated values
                Section cSection = new Section();
                cSection.dist = currentSection.dist - saveSection.dist;
                cSection.time =  currentSection.time - saveSection.time;
                cSection.ascent =  currentSection.ascent - saveSection.ascent;
                cSection.descent =  currentSection.descent - saveSection.descent;
                currentSection = cSection;
            }
        }


        if (currentSection.dist > 0 && currentSection.time > 0) {
            sections.add(currentSection);
            if (isSectionBetter(currentSection, bestSection)) {
                bestSection = currentSection;
            } else if (isSectionBetter(worstSection, currentSection)) {
                worstSection = currentSection;
            }
        }

        worstSection.worst = true;
        bestSection.best = true;

        return sections;
    }

    private static boolean isSectionBetter(Section section, Section compareTo) {
        return section.getPace() <= compareTo.getPace();
    }

    public enum SectionCriterion {
        DISTANCE(0),
        TIME(1),
        ASCENT(2),
        DESCENT(3);

        private int id;

        SectionCriterion(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static List<String> getStringRepresentations(Context context) {
            List<String> criteria = new ArrayList<>();
            criteria.add(context.getString(R.string.workoutDistance));
            criteria.add(context.getString(R.string.workoutTime));
            criteria.add(context.getString(R.string.workoutAscent));
            criteria.add(context.getString(R.string.workoutDescent));
            return criteria;
        }
    }

    public static class Section {
        double dist = 0, ascent = 0, descent = 0;
        double time = 0;
        boolean best = false;
        boolean worst = false;

        public double getPace() {
            return time / dist;
        }

        public Section copy() {
            Section section = new Section();
            section.dist = dist;
            section.ascent = ascent;
            section.descent = descent;
            section.time = time;
            section.best = best;
            section.worst = worst;
            return section;
        }
    }
}
