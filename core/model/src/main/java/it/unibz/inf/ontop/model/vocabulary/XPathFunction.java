package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class XPathFunction {

    public static final String PREFIX = "http://www.w3.org/2005/xpath-functions#";

    public final static IRI CONCAT;
    public final static IRI UPPER_CASE;
    public final static IRI LOWER_CASE;
    public final static IRI STARTS_WITH;
    public static final IRI ENDS_WITH;
    public static final IRI CONTAINS;
    public static final IRI SUBSTRING;
    public static final IRI STRING_LENGTH;
    public static final IRI ENCODE_FOR_URI;
    public static final IRI REPLACE;
    public static final IRI SUBSTRING_BEFORE;
    public static final IRI SUBSTRING_AFTER;
    public static final IRI NOT;
    public static final IRI NUMERIC_ABS;
    public static final IRI NUMERIC_ROUND;
    public static final IRI NUMERIC_FLOOR;
    public static final IRI NUMERIC_CEIL;
    public static final IRI YEAR_FROM_DATETIME;
    public static final IRI MONTH_FROM_DATETIME;
    public static final IRI DAY_FROM_DATETIME;
    public static final IRI HOURS_FROM_DATETIME;
    public static final IRI MINUTES_FROM_DATETIME;
    public static final IRI SECONDS_FROM_DATETIME;
    public static final IRI TIMEZONE_FROM_DATETIME;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        CONCAT = factory.createIRI(PREFIX + "concat");
        UPPER_CASE = factory.createIRI(PREFIX + "upper-case");
        LOWER_CASE = factory.createIRI(PREFIX + "lower-case");
        STARTS_WITH = factory.createIRI(PREFIX + "starts-with");
        ENDS_WITH = factory.createIRI(PREFIX + "ends-with");
        CONTAINS = factory.createIRI(PREFIX + "contains");
        SUBSTRING = factory.createIRI(PREFIX + "substring");
        STRING_LENGTH = factory.createIRI(PREFIX + "string-length");
        ENCODE_FOR_URI = factory.createIRI(PREFIX + "encode-for-uri");
        REPLACE = factory.createIRI(PREFIX + "replace");
        SUBSTRING_BEFORE = factory.createIRI(PREFIX + "substring-before");
        SUBSTRING_AFTER = factory.createIRI(PREFIX + "substring-after");
        NOT = factory.createIRI(PREFIX + "not");
        NUMERIC_ABS = factory.createIRI(PREFIX + "numeric-abs");
        NUMERIC_ROUND = factory.createIRI(PREFIX + "numeric-round");
        NUMERIC_FLOOR = factory.createIRI(PREFIX + "numeric-floor");
        NUMERIC_CEIL = factory.createIRI(PREFIX + "numeric-ceil");
        YEAR_FROM_DATETIME = factory.createIRI(PREFIX + "year-from-dateTime");
        MONTH_FROM_DATETIME = factory.createIRI(PREFIX + "month-from-dateTime");
        DAY_FROM_DATETIME = factory.createIRI(PREFIX + "day-from-dateTime");
        HOURS_FROM_DATETIME = factory.createIRI(PREFIX + "hours-from-dateTime");
        MINUTES_FROM_DATETIME = factory.createIRI(PREFIX + "minutes-from-dateTime");
        SECONDS_FROM_DATETIME = factory.createIRI(PREFIX + "seconds-from-dateTime");
        TIMEZONE_FROM_DATETIME = factory.createIRI(PREFIX + "timezone-from-dateTime");
    }
}
