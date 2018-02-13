package it.unibz.inf.ontop.temporal.mapping;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemporalMappingIntervalValidatorTest extends TestCase {

//    @Test
//    public void test1() throws Exception {
//        String s = "[{ts_begin}, {ts_end})";
//        assertEquals(true, IntervalQueryParser.temporalMappingIntervalValidator(s));
//    }
//
//    @Test
//    public void test2() throws Exception {
//        String s = "[{ts_begin}^^xsd:datetime, {ts_end}^^xsd:datetime)";
//        assertEquals(true, IntervalQueryParser.temporalMappingIntervalValidator(s));
//    }
//
//    @Test
//    public void test3() throws Exception {
//        String s = "(   {ts_begin}^^xsd:datetime   ,   {ts_end}^^xsd:datetime  ]";
//        assertEquals(true, IntervalQueryParser.temporalMappingIntervalValidator(s));
//    }
//
//    @Test
//    public void test4(){
//        String s = "{ts_begin}, {ts_end}";
//        Pattern variablePattern = Pattern.compile("(?i)\\{[\\w.-]+\\}");
//        Matcher m = variablePattern.matcher(s);
//        while(m.find()) {
//            System.out.println(m.group());
//        }
//    }
//
//    @Test
//    public void test5(){
//        String s = "{ts_begin}, {ts_end}";
//        Pattern variablePattern = Pattern.compile("(?i)\\{[\\w.-]+\\}");
//        Matcher m = variablePattern.matcher(s);
//        if (m.find()) {
//            System.out.println(m.group());
//        }
//        if (m.find()) {
//            System.out.println(m.group());
//        }
//    }

//    @Test
//    public void test6(){
//        String s = "[{ts_begin}, {ts_end})";
//        IntervalQueryParser.parse(s);
//    }

}
