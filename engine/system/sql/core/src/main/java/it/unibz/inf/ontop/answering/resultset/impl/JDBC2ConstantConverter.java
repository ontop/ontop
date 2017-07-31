package it.unibz.inf.ontop.answering.resultset.impl;

import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


public class JDBC2ConstantConverter {

    private AtomicInteger bnodeCounter;
    private IRIDictionary iriDictionary;

    // TODO(xiao): isolate the following DB specific code
    private final boolean isMsSQL;
    private final boolean isOracle;
    private final DateFormat dateFormat;

    private final Map<String, String> bnodeMap;

    private static final DecimalFormat formatter = new DecimalFormat("0.0###E0");

    private static List<DateFormat> possibleDateFormats = new ArrayList<>();

    static {
        DecimalFormatSymbols symbol = DecimalFormatSymbols.getInstance();
        symbol.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(symbol);


        possibleDateFormats.add(new SimpleDateFormat("dd-MMM-yy HH.mm.ss.SSSSSS aa", Locale.ENGLISH)); // For oracle driver v.11 and less
        possibleDateFormats.add(new SimpleDateFormat("dd-MMM-yy HH:mm:ss,SSSSSS", Locale.ENGLISH)); // THIS WORKS FOR ORACLE DRIVER 12.1.0.2
        possibleDateFormats.add(new SimpleDateFormat("MMM dd yyyy hh:mmaa", Locale.ENGLISH)); // For MSSQL
        possibleDateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)); // ISO with 'T'
        possibleDateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)); // ISO without 'T'
        possibleDateFormats.add(new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)); // another common case
    }

    public JDBC2ConstantConverter(DBMetadata dbMetadata,
                                  Optional<IRIDictionary> iriDictionary) {
        this.iriDictionary = iriDictionary.orElse(null);
        String vendor = dbMetadata.getDriverName();
        isOracle = vendor.contains("Oracle");
        isMsSQL = vendor.contains("SQL Server");

        if (isOracle) {
            String version = dbMetadata.getDriverVersion();
            int versionInt = Integer.parseInt(version.substring(0, version.indexOf(".")));

            if (versionInt >= 12)
                dateFormat = new SimpleDateFormat("dd-MMM-yy HH:mm:ss,SSSSSS", Locale.ENGLISH); // THIS WORKS FOR ORACLE DRIVER 12.1.0.2
            else
                dateFormat = new SimpleDateFormat("dd-MMM-yy HH.mm.ss.SSSSSS aa", Locale.ENGLISH); // For oracle driver v.11 and less
        } else if (isMsSQL) {
            dateFormat = new SimpleDateFormat("MMM dd yyyy hh:mmaa", Locale.ENGLISH);
        } else
            dateFormat = null;

        this.bnodeCounter = new AtomicInteger();
        bnodeMap = new HashMap<>(1000);
    }

    public Constant getConstantFromJDBC(MainTypeLangValues cell) throws OntopResultConversionException {

        Constant result = null;
        Object value = "";
        String stringValue = "";

        try {
            value = cell.getMainValue();

            if (value == null) {
                return null;
            } else {
                stringValue = String.valueOf(value);

                int t = cell.getTypeValue();
                Predicate.COL_TYPE type = Predicate.COL_TYPE.getQuestType(t);
                if (type == null)
                    throw new OntopResultConversionException("typeCode unknown: " + t);

                switch (type) {
                    case NULL:
                        result = null;
                        break;

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
                        result = TERM_FACTORY.getConstantURI(stringValue.trim());
                        break;

                    case BNODE:
                        String scopedLabel = this.bnodeMap.get(stringValue);
                        if (scopedLabel == null) {
                            scopedLabel = "b" + bnodeCounter.getAndIncrement();
                            bnodeMap.put(stringValue, scopedLabel);
                        }
                        result = TERM_FACTORY.getConstantBNode(scopedLabel);
                        break;
                    /**
                     * TODO: the language tag should be reserved to LITERAL_LANG
                     */
                    case LITERAL:
                    case LITERAL_LANG:
                        // The constant is a literal, we need to find if its
                        // rdfs:Literal or a normal literal and construct it
                        // properly.
                        String language = cell.getLangValue();
                        if (language == null || language.trim().equals(""))
                            result = TERM_FACTORY.getConstantLiteral(stringValue);
                        else
                            result = TERM_FACTORY.getConstantLiteral(stringValue, language);
                        break;

                    case BOOLEAN:
                        // TODO(xiao): more careful
                        boolean bvalue = Boolean.valueOf(stringValue);
                        //boolean bvalue = (Boolean)value;
                        result = TERM_FACTORY.getBooleanConstant(bvalue);
                        break;

                    case DOUBLE:
                        double d = Double.valueOf(stringValue);
                        String s = formatter.format(d); // format name into correct double representation
                        result = TERM_FACTORY.getConstantLiteral(s, type);
                        break;

                    case INT:
                        //int intValue = (Integer)value;
                        result = TERM_FACTORY.getConstantLiteral(stringValue, type);
                        break;

                    case LONG:
                    case UNSIGNED_INT:
                        //long longValue = (Long)value;
                        result = TERM_FACTORY.getConstantLiteral(stringValue, type);
                        break;

                    case INTEGER:
                    case NEGATIVE_INTEGER:
                    case NON_NEGATIVE_INTEGER:
                    case POSITIVE_INTEGER:
                    case NON_POSITIVE_INTEGER:
                        /**
                         * Sometimes the integer may have been converted as DECIMAL, FLOAT or DOUBLE
                         */
                        int dotIndex = stringValue.indexOf(".");
                        String integerString = dotIndex >= 0
                                ? stringValue.substring(0, dotIndex)
                                : stringValue;
                        result = TERM_FACTORY.getConstantLiteral(integerString, type);
                        break;

                    case DATETIME:
                        result = convertDatetimeConstant(value);

                        break;

                    case DATETIME_STAMP:
                        if (!isOracle) {
                            result = TERM_FACTORY.getConstantLiteral(stringValue.replaceFirst(" ", "T").replaceAll(" ", ""), Predicate.COL_TYPE.DATETIME_STAMP);
                        } else {
                        /* oracle has the type timestamptz. The format returned by getString is not a valid xml format
                        we need to transform it. We first take the information about the timezone value, that is lost
						during the conversion in java.util.Date and then we proceed with the conversion. */
                            try {
                                int indexTimezone = stringValue.lastIndexOf(" ");
                                String timezone = stringValue.substring(indexTimezone + 1);
                                String datetime = stringValue.substring(0, indexTimezone);

                                java.util.Date date = dateFormat.parse(datetime);
                                Timestamp ts = new Timestamp(date.getTime());
                                result = TERM_FACTORY.getConstantLiteral(ts.toString().replaceFirst(" ", "T").replaceAll(" ", "") + timezone, Predicate.COL_TYPE.DATETIME_STAMP);
                            } catch (ParseException pe) {
                                throw new OntopResultConversionException(pe);
                            }
                        }
                        break;

                    case DATE:
                        if (!isOracle) {
                            //Date dvalue = (Date)value;
                            result = TERM_FACTORY.getConstantLiteral(stringValue, Predicate.COL_TYPE.DATE);
                        } else {
                            try {
                                DateFormat df = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
                                java.util.Date date = df.parse(stringValue);
                            } catch (ParseException e) {
                                throw new OntopResultConversionException(e);
                            }
                            result = TERM_FACTORY.getConstantLiteral(value.toString(), Predicate.COL_TYPE.DATE);
                        }
                        break;

                    case TIME:
                        //Time tvalue = (Time)value;
                        result = TERM_FACTORY.getConstantLiteral(stringValue.replace(' ', 'T'), Predicate.COL_TYPE.TIME);
                        break;

                    default:
                        result = TERM_FACTORY.getConstantLiteral(stringValue, type);
                }
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

        return result;
    }

    private Constant convertDatetimeConstant(Object value) throws OntopResultConversionException {
        java.util.Date dateValue = null;

        if (value instanceof java.util.Date) {
            // If JDBC gives us proper Java object, we are good
            dateValue = (java.util.Date) value;
        } else {
            // Otherwise, we need to deal with possible String representation of datetime
            String stringValue = String.valueOf(value);
            for (DateFormat format : possibleDateFormats) {
                try {
                    dateValue = format.parse(stringValue);
                    break;
                } catch (ParseException e) {
                    // continue with the next try
                }
            }

            if (dateValue == null) {
                throw new OntopResultConversionException("unparseable datetime: " + stringValue);
            }
        }
        final Timestamp ts = new Timestamp(dateValue.getTime());
        final String dateTimeLexicalValue = ts.toString().replace(' ', 'T');

        return TERM_FACTORY.getConstantLiteral(dateTimeLexicalValue, Predicate.COL_TYPE.DATETIME);
    }
}
