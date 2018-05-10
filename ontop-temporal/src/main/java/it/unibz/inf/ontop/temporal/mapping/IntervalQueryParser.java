package it.unibz.inf.ontop.temporal.mapping;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.temporal.mapping.impl.TemporalMappingIntervalImpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntervalQueryParser {

    private final TermFactory termFactory;

    @Inject
    public IntervalQueryParser(TermFactory termFactory) {

        this.termFactory = termFactory;
    }

    public TemporalMappingInterval parse(String input){

        Variable beginInclusive = null;
        Variable endInclusive = null;
        Variable begin;
        Variable end;
        String strBegin = "";
        String strEnd = "";
        String strBeginInclusive = "";
        String strEndInclusive = "";

        Pattern variablePattern = Pattern.compile("(?i)\\{[\\w.-]+\\}");
        Matcher m = variablePattern.matcher(input);
        if(m.find()) {
            strBeginInclusive = m.group();
            strBeginInclusive = strBeginInclusive.substring(1, strBeginInclusive.length()-1);
        }
        if(m.find()) {
            strBegin = m.group();
            strBegin = strBegin.substring(1, strBegin.length()-1);
        }
        if(m.find()) {
            strEnd = m.group();
            strEnd = strEnd.substring(1, strEnd.length()-1);
        }
        if(m.find()) {
            strEndInclusive = m.group();
            strEndInclusive = strEndInclusive.substring(1, strEndInclusive.length()-1);
        }

        beginInclusive = termFactory.getVariable(strBeginInclusive);
        begin = termFactory.getVariable(strBegin);
        end = termFactory.getVariable(strEnd);
        endInclusive = termFactory.getVariable(strEndInclusive);

        return new TemporalMappingIntervalImpl(beginInclusive,endInclusive,begin,end);
    }

//    public TemporalMappingInterval parse(String input){
//
//        BooleanConstant beginInclusive = null;
//        BooleanConstant endInclusive = null;
//        Variable begin;
//        Variable end;
//        String strBegin = "";
//        String strEnd = "";
//        String strBeginInclusive;
//        String strEndInclusive;
//
//        Pattern variablePattern = Pattern.compile("(?i)\\{[\\w.-]+\\}");
//        Matcher m = variablePattern.matcher(input);
//        if(m.find()) {
//            strBegin = m.group();
//            strBegin = strBegin.substring(1, strBegin.length()-1);
//        }
//        if(m.find()) {
//            strEnd = m.group();
//            strEnd = strEnd.substring(1, strEnd.length()-1);
//        }
//        begin = termFactory.getVariable(strBegin);
//        end = termFactory.getVariable(strEnd);
//
//        strBeginInclusive = input.substring(0,1);
//        strEndInclusive = input.substring(input.length()-1, input.length());
//
//        if(strBeginInclusive.equals("["))
//            beginInclusive = new BooleanConstantImpl("true");
//        else if(strBeginInclusive.equals("("))
//            beginInclusive = new BooleanConstantImpl("false");
//
//        if(strEndInclusive.equals("]"))
//            endInclusive = new BooleanConstantImpl("true");
//        else if(strEndInclusive.equals(")"))
//            endInclusive = new BooleanConstantImpl("false");
//
//        return new TemporalMappingIntervalImpl(beginInclusive,endInclusive,begin,end);
//    }
//
//    public static boolean temporalMappingIntervalValidator(String input){
//        Pattern intervalRegexPattern = Pattern.compile("(?i)[\\[\\(]\\s*\\{[\\w.-]+\\}(\\^{2}[\\w.-]+:(dateTimeStamp|dateTime|time))?\\s*,\\s*\\{[\\w.-]+\\}(\\^{2}[\\w.-]+:(dateTimeStamp|dateTime|time))?\\s*[\\)\\]]");
//        Matcher m = intervalRegexPattern.matcher(input);
//        return m.matches();
//    }
}
