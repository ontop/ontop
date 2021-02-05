package it.unibz.inf.ontop.model.vocabulary;

public class SPARQL {

    public static final String LANG = "LANG";
    public static final String LANG_MATCHES = "langMatches";
    public static final String STR = "STR";
    public static final String DATATYPE = "DATATYPE";
    public static final String IS_IRI = "isIRI";
    public static final String IS_BLANK = "isBlank";
    public static final String IS_LITERAL = "isLiteral";
    public static final String IS_NUMERIC = "isNumeric";
    public static final String STRUUID = "STRUUID";
    public static final String UUID = "UUID";
    public static final String REGEX = "REGEX";
    public static final String LOGICAL_AND = "&&";
    public static final String LOGICAL_OR = "||";
    public static final String BOUND = "BOUND";
    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA1";
    public static final String SHA256 = "SHA256";
    public static final String SHA512 = "SHA512";
    public static final String NUMERIC_MULTIPLY = "*";
    public static final String NUMERIC_DIVIDE = "/";
    public static final String NUMERIC_ADD = "+";
    public static final String NUMERIC_SUBTRACT = "-";
    public static final String EQ = "=";
    public static final String LESS_THAN = "<";
    public static final String GREATER_THAN = ">";
    public static final String RAND = "RAND";
    public static final String TZ = "TZ";
    public static final String NOW = "NOW";
    public static final String SAME_TERM = "sameTerm";
    public static final String COALESCE = "COALESCE";
    public static final String COUNT = "COUNT";
    public static final String AVG = "AVG";
    public static final String SUM = "SUM";
    public static final String MIN = "MIN";
    public static final String MAX = "MAX";
    public static final String SAMPLE = "SAMPLE";
    public static final String GROUP_CONCAT = "GROUP_CONCAT";
    public static final String BNODE = "BNODE";
    public static final String IRI = "IRI";
    public static final String IF = "IF";
    /**
     * Although it is directly mapped in SPARQL 1.1 to fn:year-from-dateTime, in practice all SPARQL implementations
     * extend it to also support xsd:date, not just xsd:dateTime. Therefore it cannot be mapped directly to such a
     * restrictive function.
     */
    public static final String YEAR = "YEAR";
    public static final String MONTH = "MONTH";
    public static final String DAY = "DAY";
}
