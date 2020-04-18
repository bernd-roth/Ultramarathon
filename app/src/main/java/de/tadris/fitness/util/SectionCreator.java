package de.tadris.fitness.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.R;
import de.tadris.fitness.data.WorkoutSample;

class SectionCreator {

    static ArrayList<Section> createSectionList(java.util.List<WorkoutSample> samples, SectionCriterion criterion, double sectionLength) {
        ArrayList<Section> sections = new ArrayList<>();

        Section bestSection = null, worstSection = null;

        WorkoutSample currentSectionStart = samples.get(0);
        Section currentSection = new Section();

        int sectionsCount = 0;
        for (int i = 1; i < samples.size(); ++i) {
            WorkoutSample sample = samples.get(i);
            WorkoutSample previous = samples.get(i - 1);

            currentSection.dist += sample.toLatLong().sphericalDistance(previous.toLatLong());
            currentSection.time = sample.relativeTime - currentSectionStart.relativeTime;
            if (sample.elevation < previous.elevation)
                currentSection.descent += sample.elevation - previous.elevation;
            if (sample.elevation > previous.elevation)
                currentSection.ascent += sample.elevation - previous.elevation;

            if (checkCriterion(criterion, currentSection, sectionLength)) {
                Section saveSection = currentSection.copy();

                if (bestSection == null) {
                    bestSection = saveSection;
                } else if (isSectionBetter(criterion, saveSection, bestSection)) {
                    bestSection = saveSection;
                }

                if (worstSection == null) {
                    worstSection = saveSection;
                } else if (isSectionBetter(criterion, worstSection, saveSection)) {
                    worstSection = saveSection;
                }

                addLengthToSection(saveSection, criterion, sectionsCount++ * sectionLength);

                sections.add(saveSection);
                double startDist = currentSection.dist - sectionLength; // substract small overlap if distance can not be matche 100% (next section starts a bit to late)
                currentSection = new Section();
                if (criterion == SectionCriterion.DISTANCE)
                    currentSection.dist = startDist;
                currentSectionStart = sample;
            }
        }

        if (sectionsCount > 0) {
            worstSection.worst = true;
            bestSection.best = true;
        }

        if (currentSection.dist != 0) {
            addLengthToSection(currentSection, criterion, sectionsCount * sectionLength);
            sections.add(currentSection);
        }

        return sections;
    }

    private static void addLengthToSection(Section section, SectionCriterion criterion, double previousLength) {
        switch (criterion) {
            case DISTANCE:
                section.dist += previousLength;
                break;
            case ASCENT:
                section.ascent += previousLength;
                break;
            case DESCENT:
                section.descent += -previousLength;
                break;
            case TIME:
                section.time += (long) (previousLength);
                break;
        }
    }

    private static boolean isSectionBetter(SectionCriterion criterion, Section section, Section compareTo) {
        if (criterion == SectionCriterion.TIME) {
            return section.dist > compareTo.dist;
        }
        return section.time < compareTo.time;
    }

    private static boolean checkCriterion(SectionCriterion criterion, Section section, double length) {
        switch (criterion) {
            case ASCENT:
                return section.ascent > length;
            case DESCENT:
                return -section.descent > length;
            case TIME:
                return section.time > length;
            case DISTANCE:
                return section.dist > length;
        }
        return true;
    }

    public enum SectionCriterion {
        DISTANCE,
        TIME,
        ASCENT,
        DESCENT;

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
        long time = 0;
        boolean best = false;
        boolean worst = false;

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
