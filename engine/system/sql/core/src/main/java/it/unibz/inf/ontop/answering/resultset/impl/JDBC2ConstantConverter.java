package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.generation.utils.COL_TYPE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.rdf.api.RDF;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static it.unibz.inf.ontop.answering.reformulation.generation.utils.COL_TYPE.WKT;
import static it.unibz.inf.ontop.answering.resultset.impl.JDBC2ConstantConverter.System.*;


public class JDBC2ConstantConverter {

    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final RDF rdfFactory;

    enum System {ORACLE, MSSQL, DEFAULT}

    private static ImmutableList<DateTimeFormatter> defaultDateTimeFormatter;
    private static ImmutableMap<System, ImmutableList<DateTimeFormatter>> system2DateTimeFormatter;
    private static ImmutableMap<System, ImmutableList<DateTimeFormatter>> system2TimeFormatter;

    private AtomicInteger bnodeCounter;
    private IRIDictionary iriDictionary;

    private final Map<String, String> bnodeMap;

    private final System systemDB;

    static {
        defaultDateTimeFormatter = buildDefaultDateTimeFormatter();
        system2DateTimeFormatter = buildDateTimeFormatterMap();
        system2TimeFormatter = buildDefaultTimeFormatterMap();
    }

    //java 8 date format
    //default possible date time format
    private static ImmutableList<DateTimeFormatter> buildDefaultDateTimeFormatter() {

        return ImmutableList.<DateTimeFormatter>builder()
                    .add(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // ISO with 'T'
                    .add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[[.SSSSSSSSS][.SSSSSSS][.SSSSSS][.SSS][.SS][.S][XXXXX][XXXX][x]")) // ISO without 'T'
//               new DateTimeFormatterBuilder().parseLenient().appendPattern("yyyy-MM-dd HH:mm:ss[XXXXX][XXXX][x]").appendFraction(ChronoField.MICRO_OF_SECOND, 0, 9, true)
                    .add(DateTimeFormatter.ISO_DATE) // ISO with or without time
                    .add(new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yy[ HH:mm:ss]").toFormatter()) // another common case
                    .build();
    }

    //special cases on different systems
    private static ImmutableMap<System,ImmutableList<DateTimeFormatter>> buildDateTimeFormatterMap() {
        return ImmutableMap.of(

                DEFAULT, ImmutableList.<DateTimeFormatter>builder()
                        .addAll(defaultDateTimeFormatter) // another common case
                        .build(),
                ORACLE, ImmutableList.<DateTimeFormatter>builder()
                        .addAll(defaultDateTimeFormatter)
                        .add(new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yy[ hh[.][:]mm[.][:]ss[.][,][n][ a][ ZZZZZ][ VV]]").toFormatter())
                        .add(new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yy[ HH[.][:]mm[.][:]ss[.][,][n][ ZZZZZ][ VV]]").toFormatter())
                        .build(),
                MSSQL, ImmutableList.<DateTimeFormatter>builder()
                        .addAll(defaultDateTimeFormatter)
                        .add(new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM dd yyyy[ hh:mm[a]]").toFormatter())
                        .build()
        );
    }

    private static ImmutableMap<System,ImmutableList<DateTimeFormatter>> buildDefaultTimeFormatterMap() {
        return ImmutableMap.of(

                DEFAULT, ImmutableList.<DateTimeFormatter>builder()
                        .add(DateTimeFormatter.ofPattern("['T']HH:mm:ss[Z][x]")) // ISO timezone with 'T'
                        .add(DateTimeFormatter.ISO_TIME) // ISO time or timezone without 'T'
                        .build()
        );
    }


    public JDBC2ConstantConverter(DBMetadata dbMetadata, Optional<IRIDictionary> iriDictionary,
                                  TermFactory termFactory, TypeFactory typeFactory, RDF rdfFactory) {
        this.iriDictionary = iriDictionary.orElse(null);
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.rdfFactory = rdfFactory;
        String vendor = dbMetadata.getDriverName();
        systemDB = identifySystem(vendor);
        this.bnodeCounter = new AtomicInteger();
        bnodeMap = new HashMap<>(1000);
    }

    private System identifySystem(String vendor) {
        if(vendor.contains("Oracle"))
            return ORACLE;
        if(vendor.contains("SQL Server"))
            return MSSQL;
        return DEFAULT;
    }

    public Constant getConstantFromJDBC(MainTypeLangValues cell) throws OntopResultConversionException {

        Object value = "";
        String stringValue;

        try {
            value = cell.getMainValue();

            if (value == null) {
                return null;
            }
            stringValue = String.valueOf(value);

            int t = cell.getTypeValue();
            COL_TYPE type = COL_TYPE.getQuestType(t);
            if (type == null)
                throw new OntopResultConversionException("typeCode unknown: " + t);

            switch (type) {
                case UNSUPPORTED:
                    return termFactory.getConstantLiteral(stringValue, typeFactory.getUnsupportedDatatype());
                case NULL:
                    return null;

                case OBJECT:
                    if (iriDictionary != null) {
                        try {
                            Integer id = Integer.parseInt(stringValue);
                            stringValue = iriDictionary.getURI(id);
                        } catch (NumberFormatException e) {
                            // If its not a number, then it has to be a URI, so
                            // we leave realValue as it is.
                        }
                    }
                    return termFactory.getConstantIRI(rdfFactory.createIRI(stringValue.trim()));

                case BNODE:
                    String scopedLabel = this.bnodeMap.get(stringValue);
                    if (scopedLabel == null) {
                        scopedLabel = "b" + bnodeCounter.getAndIncrement();
                        bnodeMap.put(stringValue, scopedLabel);
                    }
                    return termFactory.getConstantBNode(scopedLabel);
                case LANG_STRING:
                    // The constant is a literal, we need to find if its
                    // rdfs:Literal or a normal literal and construct it
                    // properly.
                    String language = cell.getLangValue();
                    if (language == null || language.trim().equals(""))
                        return termFactory.getConstantLiteral(stringValue);
                    else
                        return termFactory.getConstantLiteral(stringValue, language);

                case BOOLEAN:

                    boolean bvalue = Boolean.parseBoolean(stringValue);
                    return termFactory.getBooleanConstant(bvalue);

                case DECIMAL:
                    return termFactory.getConstantLiteral(stringValue, XSD.DECIMAL);
                case FLOAT:
                    return termFactory.getConstantLiteral(extractFloatingValue(stringValue), XSD.FLOAT);
                case DOUBLE:
                    return termFactory.getConstantLiteral(extractFloatingValue(stringValue), XSD.DOUBLE);

                case INT:
                    return termFactory.getConstantLiteral(stringValue, XSD.INT);

                case LONG:
                    return termFactory.getConstantLiteral(stringValue, XSD.LONG);
                case UNSIGNED_INT:
                    return termFactory.getConstantLiteral(stringValue, XSD.UNSIGNED_INT);

                case INTEGER:
                    return termFactory.getConstantLiteral(extractIntegerValue(stringValue), XSD.INTEGER);
                case NEGATIVE_INTEGER:
                    return termFactory.getConstantLiteral(extractIntegerValue(stringValue), XSD.NEGATIVE_INTEGER);
                case NON_NEGATIVE_INTEGER:
                    return termFactory.getConstantLiteral(extractIntegerValue(stringValue), XSD.NON_NEGATIVE_INTEGER);
                case POSITIVE_INTEGER:
                    return termFactory.getConstantLiteral(extractIntegerValue(stringValue), XSD.POSITIVE_INTEGER);
                case NON_POSITIVE_INTEGER:
                    return termFactory.getConstantLiteral(extractIntegerValue(stringValue), XSD.NON_POSITIVE_INTEGER);

                case STRING:
                    return termFactory.getConstantLiteral(stringValue, XSD.STRING);
                case DATETIME:
                    return termFactory.getConstantLiteral(extractDatetimeValue(value), XSD.DATETIME);
                case DATETIME_STAMP:
                    return termFactory.getConstantLiteral(extractDatetimeValue(value), XSD.DATETIMESTAMP);

                case DATE:
                    return termFactory.getConstantLiteral( DateTimeFormatter.ISO_DATE.format(convertToJavaDate(value)), XSD.DATE);

                case TIME:

                    return termFactory.getConstantLiteral(DateTimeFormatter.ISO_TIME.format(convertToTime(value)), XSD.TIME);

                case YEAR:
                    return termFactory.getConstantLiteral(stringValue, XSD.GYEAR);
                case WKT:
                    return termFactory.getConstantLiteral(stringValue, WKT.getIri().get());
                default:
                    throw new IllegalStateException("Unexpected colType: " + type);

            }
        } catch (IllegalArgumentException e) {
            Throwable cause = e.getCause();
            if (cause instanceof URISyntaxException) {
                OntopResultConversionException ex = new OntopResultConversionException(
                        "Error creating an object's URI. This is often due to mapping with URI templates that refer to "
                                + "columns in which illegal values may appear, e.g., white spaces and special characters.\n"
                                + "To avoid this error do not use these columns for URI templates in your mappings, or process "
                                + "them using SQL functions (e.g., string replacement) in the SQL queries of your mappings.\n\n"
                                + "Note that this last option can be bad for performance, future versions of Quest will allow to "
                                + "string manipulation functions in URI templates to avoid these performance problems.\n\n"
                                + "Detailed message: " + cause.getMessage());
                ex.setStackTrace(e.getStackTrace());
                throw ex;
            } else {
                OntopResultConversionException ex = new OntopResultConversionException("Quest couldn't parse the data value to Java object: " + value + "\n"
                        + "Please review the mapping rules to have the datatype assigned properly.");
                ex.setStackTrace(e.getStackTrace());
                throw ex;
            }
        } catch (Exception e) {
            throw new OntopResultConversionException(e);
        }
    }

    private String extractFloatingValue(String stringValue) {
        BigDecimal bigDecimal;
        try {
            bigDecimal = new BigDecimal (stringValue);

        }
        catch (NumberFormatException e){
            return stringValue;
        }
        DecimalFormat formatter = new DecimalFormat("0.0E0");
        formatter.setRoundingMode(RoundingMode.UNNECESSARY);
        formatter.setMaximumFractionDigits((bigDecimal.scale() > 0) ? bigDecimal.precision() -1 : bigDecimal.precision() -1 + bigDecimal.scale() *-1);
        return  formatter.format(bigDecimal);
    }

    private String extractDatetimeValue(Object value) throws OntopResultConversionException {
        TemporalAccessor temporal = convertToJavaDate(value);
        if(temporal instanceof LocalDate){
            temporal = LocalDateTime.of((LocalDate)temporal, LocalTime.MIDNIGHT);
        }
        return DateTimeFormatter.ISO_DATE_TIME.format(temporal);
    }

    /**
     * Sometimes the integer may have been converted as DECIMAL, FLOAT or DOUBLE
     */
    private String extractIntegerValue(String stringValue) {
        return String.valueOf(new BigDecimal(stringValue).toBigInteger());
    }

    private TemporalAccessor convertToJavaDate(Object value) throws OntopResultConversionException {
        TemporalAccessor dateValue = null;

        if (value instanceof Date ) {
            // If JDBC gives us proper Java object, we simply return the formatted version of the datatype
            Calendar calendar = DateUtils.toCalendar(((Date) value));
            TimeZone timeZone = calendar.getTimeZone();
            dateValue = OffsetDateTime.from(calendar.toInstant().atZone(timeZone.toZoneId()));
        } else {
            // Otherwise, we need to deal with possible String representation of datetime
            String stringValue = String.valueOf(value);
            for (DateTimeFormatter format : system2DateTimeFormatter.get(systemDB)) {
                try {
                    dateValue = format.parseBest(stringValue, OffsetDateTime::from, LocalDateTime::from, LocalDate::from);

                    break;
                } catch (DateTimeParseException e) {
                    // continue with the next try
                }
            }

            if (dateValue == null) {
                throw new OntopResultConversionException("unparseable datetime: " + stringValue);
            }

        }
        return dateValue;

    }

    private TemporalAccessor convertToTime(Object value) throws OntopResultConversionException {
        TemporalAccessor timeValue = null;

        if (value instanceof Date ) {
            // If JDBC gives us proper Java object, we simply return the formatted version of the datatype
            Calendar calendar = DateUtils.toCalendar(((Date) value));
            TimeZone timeZone = calendar.getTimeZone();
            timeValue = OffsetTime.from(calendar.toInstant().atZone(timeZone.toZoneId()));
        } else {
            // Otherwise, we need to deal with possible String representation of datetime
            String stringValue = String.valueOf(value);
            for (DateTimeFormatter format : system2TimeFormatter.get(DEFAULT)) {
                try {
                    timeValue = format.parseBest(stringValue, OffsetTime::from, LocalTime::from);

                    break;
                } catch (DateTimeParseException e) {
                    // continue with the next try
                }
            }

            if (timeValue == null) {
                throw new OntopResultConversionException("unparseable time: " + stringValue);
            }
        }
        return timeValue;

    }


}
