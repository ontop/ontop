package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DefaultDBBinaryTemporalOperationFunctionSymbol extends AbstractDBBinaryTemporalOperationFunctionSymbol {
    private final String template;
    private final String operatorString;
    private final ImmutableList<DBTermType> argumentTypes;
    private final DBTermType resultType;

    public DefaultDBBinaryTemporalOperationFunctionSymbol(String operatorName, DBTermType arg1Type, DBTermType arg2Type, DBTermType resultType) {
        super(operatorName, arg1Type, arg2Type, resultType);
        this.template = "%s " + operatorName + " %s";
        this.operatorString = operatorName;
        this.argumentTypes = ImmutableList.of(arg1Type, arg2Type);
        this.resultType = resultType;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        int intervalCount = (int) argumentTypes.stream()
                .filter(t -> t.getCategory() == DBTermType.Category.INTERVAL)
                .count();

        ImmutableList<String> serializedIntervals = translateIntervals(terms);

        if (intervalCount == 0) {
            return String.format(template,
                    termConverter.apply(terms.get(0)),
                    termConverter.apply(terms.get(1)));
        } else if (intervalCount == 2) {
            return String.format(template, serializedIntervals.get(0), serializedIntervals.get(1));
        } else if (intervalCount == 1) {
            // Always put the interval as the second argument
            int intervalIdx = argumentTypes.get(0).getCategory() == DBTermType.Category.INTERVAL ? 0 : 1;
            int otherIdx = 1 - intervalIdx;
            return String.format(template,
                    "CAST(" + termConverter.apply(terms.get(otherIdx)) + " AS " + argumentTypes.get(otherIdx).getCastName() + ")",
                    serializedIntervals.get(0));
        } else {
            throw new IllegalStateException("Unexpected interval count: " + intervalCount);
        }
    }

    @Override
    protected String serializeInterval(Interval interval) {
        String intervalYearMonth = String.format("INTERVAL '%d-%d' YEAR TO MONTH",
                interval.getYears(), interval.getMonths());
        String intervalDayTime = String.format("INTERVAL '%d %d:%d:%f' DAY TO SECOND",
                interval.getDays(), interval.getHours(), interval.getMinutes(), interval.getTotalSeconds());

        if (interval.isNegative()) {
            return String.format("(-%s) %s (-%s)", intervalYearMonth, operatorString, intervalDayTime);
        } else {
            return String.format("%s %s %s", intervalYearMonth, operatorString, intervalDayTime);
        }
    }

}
