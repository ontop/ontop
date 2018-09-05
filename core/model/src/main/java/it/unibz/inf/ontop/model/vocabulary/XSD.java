package it.unibz.inf.ontop.model.vocabulary;


import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

public class XSD {

    public static final String PREFIX = "http://www.w3.org/2001/XMLSchema#";
    
    public final static IRI DURATION;
    
    public final static IRI DATETIME;

    public final static IRI DATETIMESTAMP;
    
    public static final IRI DAYTIMEDURATION;
    
    public final static IRI TIME;
    
    public final static IRI DATE;
    
    public final static IRI GYEARMONTH;
    
    public final static IRI GYEAR;
    
    public final static IRI GMONTHDAY;
    
    public final static IRI GDAY;
    
    public final static IRI GMONTH;
    
    public final static IRI STRING;
    
    public final static IRI BOOLEAN;
    
    public final static IRI BASE64BINARY;
    
    public final static IRI HEXBINARY;
    
    public final static IRI FLOAT;
    
    public final static IRI DECIMAL;

    public final static IRI DOUBLE;

    public final static IRI ANYURI;
    
    public final static IRI NORMALIZEDSTRING;

    public final static IRI TOKEN;

    public final static IRI NMTOKEN;

    public final static IRI NMTOKENS;

    public final static IRI INTEGER;

    public final static IRI LONG;

    public final static IRI INT;

    public final static IRI SHORT;

    public final static IRI BYTE;

    public final static IRI NON_POSITIVE_INTEGER;

    public final static IRI NEGATIVE_INTEGER;

    public final static IRI NON_NEGATIVE_INTEGER;

    public final static IRI POSITIVE_INTEGER;

    public final static IRI UNSIGNED_LONG;

    public final static IRI UNSIGNED_INT;

    public final static IRI UNSIGNED_SHORT;

    public final static IRI UNSIGNED_BYTE;

    public static final IRI YEARMONTHDURATION;

    public static final IRI NAME;
    public static final IRI NCNAME;

    static {
        
        RDF factory = new SimpleRDF();

        DURATION = factory.createIRI(PREFIX +  "duration");

        DATETIME = factory.createIRI(PREFIX +  "dateTime");

        DATETIMESTAMP = factory.createIRI(PREFIX +  "dateTimeStamp");

        DAYTIMEDURATION = factory.createIRI(PREFIX + "dayTimeDuration");

        TIME = factory.createIRI(PREFIX +  "time");

        DATE = factory.createIRI(PREFIX +  "date");

        GYEARMONTH = factory.createIRI(PREFIX +  "gYearMonth");

        GYEAR = factory.createIRI(PREFIX +  "gYear");

        GMONTHDAY = factory.createIRI(PREFIX +  "gMonthDay");

        GDAY = factory.createIRI(PREFIX +  "gDay");

        GMONTH = factory.createIRI(PREFIX +  "gMonth");

        STRING = factory.createIRI(PREFIX +  "string");

        BOOLEAN = factory.createIRI(PREFIX +  "boolean");

        BASE64BINARY = factory.createIRI(PREFIX +  "base64Binary");

        HEXBINARY = factory.createIRI(PREFIX +  "hexBinary");

        FLOAT = factory.createIRI(PREFIX +  "float");

        DECIMAL = factory.createIRI(PREFIX +  "decimal");

        DOUBLE = factory.createIRI(PREFIX +  "double");

        ANYURI = factory.createIRI(PREFIX +  "anyURI");

        NORMALIZEDSTRING = factory.createIRI(PREFIX +  "normalizedString");

        TOKEN = factory.createIRI(PREFIX +  "token");

        NMTOKEN = factory.createIRI(PREFIX +  "NMTOKEN");

        NMTOKENS = factory.createIRI(PREFIX +  "NMTOKENS");

        INTEGER = factory.createIRI(PREFIX +  "integer");

        LONG = factory.createIRI(PREFIX +  "long");

        INT = factory.createIRI(PREFIX +  "int");

        SHORT = factory.createIRI(PREFIX +  "short");

        BYTE = factory.createIRI(PREFIX +  "byte");

        NON_POSITIVE_INTEGER = factory.createIRI(PREFIX +  "nonPositiveInteger");

        NEGATIVE_INTEGER = factory.createIRI(PREFIX +  "negativeInteger");

        NON_NEGATIVE_INTEGER = factory.createIRI(PREFIX +  "nonNegativeInteger");

        POSITIVE_INTEGER = factory.createIRI(PREFIX +  "positiveInteger");

        UNSIGNED_LONG = factory.createIRI(PREFIX +  "unsignedLong");

        UNSIGNED_INT = factory.createIRI(PREFIX +  "unsignedInt");

        UNSIGNED_SHORT = factory.createIRI(PREFIX +  "unsignedShort");

        UNSIGNED_BYTE = factory.createIRI(PREFIX +  "unsignedByte");

        YEARMONTHDURATION = factory.createIRI(PREFIX + "yearMonthDuration");

        NAME = factory.createIRI(PREFIX +  "Name");

        NCNAME = factory.createIRI(PREFIX + "NCName");
    }

}