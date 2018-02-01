package it.unibz.inf.ontop.temporal.mapping;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.temporal.mapping.impl.TemporalMappingIntervalImpl;
import it.unibz.inf.ontop.temporal.model.term.BooleanConstant;
import it.unibz.inf.ontop.temporal.model.term.impl.BooleanConstantImpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntervalQueryParser {

    private final TermFactory termFactory;

    @Inject
    IntervalQueryParser(TermFactory termFactory, TermFactory termFactory1){

        this.termFactory = termFactory1;
    }

    public TemporalMappingInterval parse(String input){

        BooleanConstant beginInclusive = null;
        BooleanConstant endInclusive = null;
        Variable begin;
        Variable end;
        String strBegin = "";
        String strEnd = "";
        String strBeginInclusive;
        String strEndInclusive;

        Pattern variablePattern = Pattern.compile("(?i)\\{[\\w.-]+\\}");
        Matcher m = variablePattern.matcher(input);
        if(m.find()) {
            strBegin = m.group();
            strBegin = strBegin.substring(1, strBegin.length()-1);
        }
        if(m.find()) {
            strEnd = m.group();
            strEnd = strEnd.substring(1, strEnd.length()-1);
        }
        begin = termFactory.getVariable(strBegin);
        end = termFactory.getVariable(strEnd);

        strBeginInclusive = input.substring(0,1);
        strEndInclusive = input.substring(input.length()-1, input.length());

        if(strBeginInclusive.equals("["))
            beginInclusive = new BooleanConstantImpl("true");
        else if(strBeginInclusive.equals("("))
            beginInclusive = new BooleanConstantImpl("false");

        if(strEndInclusive.equals("]"))
            endInclusive = new BooleanConstantImpl("true");
        else if(strEndInclusive.equals(")"))
            endInclusive = new BooleanConstantImpl("false");

        return new TemporalMappingIntervalImpl(beginInclusive,endInclusive,begin,end);
    }

    public static boolean temporalMappingIntervalValidator(String input){
        Pattern intervalRegexPattern = Pattern.compile("(?i)[\\[\\(]\\s*\\{[\\w.-]+\\}(\\^{2}[\\w.-]+:(dateTimeStamp|dateTime|time))?\\s*,\\s*\\{[\\w.-]+\\}(\\^{2}[\\w.-]+:(dateTimeStamp|dateTime|time))?\\s*[\\)\\]]");
        Matcher m = intervalRegexPattern.matcher(input);
        return m.matches();
    }
}
