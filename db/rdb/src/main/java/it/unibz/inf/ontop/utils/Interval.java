package it.unibz.inf.ontop.utils;

import java.util.regex.Matcher;

public class Interval {
    private final int years;
    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;
    private final int milliseconds;
    private final boolean isNegative;


    public Interval(String sparqlInterval) {
        int years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0, milliseconds = 0;

        // Check if interval is negative
        boolean isNegative = sparqlInterval.startsWith("-");
        if (isNegative) {
            sparqlInterval = sparqlInterval.substring(1);
        }

        // Remove the leading P
        String interval = sparqlInterval.substring(1);

        // Split into date and time parts
        String[] parts = interval.split("T");
        String datePart = parts.length > 0 ? parts[0] : "";
        String timePart = parts.length > 1 ? parts[1] : "";

        if (!datePart.isEmpty()) {
            Matcher yearMatcher = java.util.regex.Pattern.compile("(\\d+)Y").matcher(datePart);
            if (yearMatcher.find()) years = Integer.parseInt(yearMatcher.group(1));

            Matcher monthMatcher = java.util.regex.Pattern.compile("(\\d+)M").matcher(datePart);
            if (monthMatcher.find()) months = Integer.parseInt(monthMatcher.group(1));

            Matcher dayMatcher = java.util.regex.Pattern.compile("(\\d+)D").matcher(datePart);
            if (dayMatcher.find()) days = Integer.parseInt(dayMatcher.group(1));
        }

        if (!timePart.isEmpty()) {
            Matcher hourMatcher = java.util.regex.Pattern.compile("(\\d+)H").matcher(timePart);
            if (hourMatcher.find()) hours = Integer.parseInt(hourMatcher.group(1));

            Matcher minuteMatcher = java.util.regex.Pattern.compile("(\\d+)M").matcher(timePart);
            if (minuteMatcher.find()) minutes = Integer.parseInt(minuteMatcher.group(1));

            Matcher secondMatcher = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)S").matcher(timePart);
            if (secondMatcher.find()) {
                String secondStr = secondMatcher.group(1);
                if (secondStr.contains(".")) {
                    seconds = (int) Float.parseFloat(secondStr);
                    milliseconds = (int) ((Float.parseFloat(secondStr) - seconds) * 1000);
                } else {
                    seconds = Integer.parseInt(secondStr);
                }
            }
        }

        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        this.isNegative = isNegative;
    }

    public int getYears() {
        return years;
    }
    public int getMonths() {
        return months;
    }
    public int getDays() {
        return days;
    }
    public int getHours() {
        return hours;
    }
    public int getMinutes() {
        return minutes;
    }
    public float getSeconds() {
        return seconds;
    }
    public int getMilliseconds() {
        return milliseconds;
    }
    public float getTotalSeconds() {
        return seconds + (milliseconds / 1000f);
    }
    public boolean isNegative() {
        return isNegative;
    }

}
