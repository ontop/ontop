package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.IntStream;

public abstract class AbstractDBBinaryTemporalOperationFunctionSymbol extends FunctionSymbolImpl implements DBFunctionSymbol {
    protected final DBTermType resultType;
    protected final ImmutableList<DBTermType> argumentTypes;

    protected AbstractDBBinaryTemporalOperationFunctionSymbol(String operatorName, DBTermType arg1Type, DBTermType arg2Type,
                                                              DBTermType resultType) {
        super(arg1Type.getName() + "_" + arg2Type.getName() + "_" + operatorName, ImmutableList.of(arg1Type, arg2Type));
        this.resultType = resultType;
        this.argumentTypes = ImmutableList.of(arg1Type, arg2Type);
    }

    @Override
    public abstract String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory);

    protected abstract String serializeInterval(Interval interval);

    protected ImmutableList<String> translateIntervals(ImmutableList<? extends ImmutableTerm> terms) {
        ImmutableList<Integer> intervalTermIdx = IntStream.range(0, argumentTypes.size())
                .filter(i -> argumentTypes.get(i).getCategory() == DBTermType.Category.INTERVAL)
                .boxed()
                .collect(ImmutableList.toImmutableList());

        ImmutableList<ImmutableTerm> intervalTerms = intervalTermIdx.stream()
                .map(terms::get)
                .collect(ImmutableList.toImmutableList());

        var intervals =  intervalTerms.stream()
                .map(i -> new Interval(((DBConstant) i).getValue()))
                .collect(ImmutableList.toImmutableList());

        return intervals.stream()
                .map(this::serializeInterval)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(resultType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    protected static class Interval {
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

}
