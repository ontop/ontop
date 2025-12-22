package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interval {
    private final int years;
    private final int months;
    private final int days;
    private final int hours;
    private final int minutes;
    private final int seconds;
    private final int milliseconds;
    private final boolean isNegative;
    private static final Pattern PATTERN = Pattern.compile(
            "^(-)?P(?=\\d|T\\d)(?:(\\d+)Y)?(?:(\\d+)M)?(?:(\\d+)D)?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+(?:\\.\\d+)?)S)?)?$"
    );


    public Interval(String xsdLexicalValue) {
        if (xsdLexicalValue == null) throw new IllegalArgumentException("Lexical value cannot be null");

        Matcher matcher = PATTERN.matcher(xsdLexicalValue);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid xsd:duration: " + xsdLexicalValue);
        }

        this.isNegative = matcher.group(1) != null;
        this.years = parse(matcher.group(2));
        this.months = parse(matcher.group(3));
        this.days = parse(matcher.group(4));
        this.hours = parse(matcher.group(5));
        this.minutes = parse(matcher.group(6));

        // Special handling for seconds and milliseconds
        String secGroup = matcher.group(7);
        if (secGroup != null) {
            double s = Double.parseDouble(secGroup);
            this.seconds = (int) s;
            this.milliseconds = (int) Math.round((s - this.seconds) * 1000);
        } else {
            this.seconds = 0;
            this.milliseconds = 0;
        }
    }

    private int parse(String group) {
        return (group == null) ? 0 : Integer.parseInt(group);
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
    public int getSeconds() {
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

    public ImmutableMap<String, Integer> toMap() {
        return ImmutableMap.of(
                "year", years,
                "month", months,
                "day", days,
                "hour", hours,
                "minute", minutes,
                "second", seconds,
                "millisecond", milliseconds
        );
    }

}
