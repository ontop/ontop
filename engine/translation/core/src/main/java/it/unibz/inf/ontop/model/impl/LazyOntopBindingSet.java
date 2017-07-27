package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.Binding;
import it.unibz.inf.ontop.model.OntopBindingSet;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;

import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;

public class LazyOntopBindingSet implements OntopBindingSet{

    private final ResultSet jdbcResultSet;
    private final List<String> signature;
    private IRIDictionary iriDictionary;
    private Map<String, String> bnodeMap;
    private Map<String, Integer> columnMap;

    //FIXME(xiao): should be shared among multi instances
    private int bnodeCounter;

    private static final DecimalFormat formatter = new DecimalFormat("0.0###E0");

    static {
        DecimalFormatSymbols symbol = DecimalFormatSymbols.getInstance();
        symbol.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(symbol);
    }

    // TODO(xiao): isolate the following DB specific code
    private final boolean isMsSQL;
    private final boolean isOracle;
    private final DateFormat dateFormat;


    public LazyOntopBindingSet(ResultSet jdbcResultSet, List<String> signature, boolean isMsSQL, boolean isOracle,
                               DateFormat dateFormat,
                               IRIDictionary iriDictionary,
                               Map<String, String> bnodeMap, Map<String, Integer> columnMap) {
        this.jdbcResultSet = jdbcResultSet;
        this.signature = signature;
        this.iriDictionary = iriDictionary;
        this.bnodeMap = bnodeMap;
        this.columnMap = columnMap;
        this.isMsSQL = isMsSQL;
        this.isOracle = isOracle;
        this.dateFormat = dateFormat;
    }

    @Override
    public Iterator<Binding> iterator() {
        // TODO(xiao):
        return null;
    }

    @Override
    public List<String> getBidingNames() {
        return this.signature;
    }

    @Override
    public Binding getBinding(int column) throws OntopConnectionException, OntopResultConversionException {
        return null;
    }

    @Override
    public Binding getBinding(String name) throws OntopConnectionException, OntopResultConversionException {
        return null;
    }



    /***
     * Returns the constant at column "column" recall that columns start at index 1.
     */
    @Override
    public Constant getConstant(int column) throws OntopConnectionException, OntopResultConversionException {
        column = column * 3; // recall that the real SQL result set has 3
        // columns per value. From each group of 3 the actual value is the
        // 3rd column, the 2nd is the language, the 1st is the type code (an integer)

        Constant result = null;
        String value = "";

        try {
            value = jdbcResultSet.getString(column);

            if (value == null) {
                return null;
            }
            else {
                int t = jdbcResultSet.getInt(column - 2);
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
                                Integer id = Integer.parseInt(value);
                                value = iriDictionary.getURI(id);
                            }
                            catch (NumberFormatException e) {
                                // If its not a number, then it has to be a URI, so
                                // we leave realValue as it is.
                            }
                        }
                        result = DATA_FACTORY.getConstantURI(value.trim());
                        break;

                    case BNODE:
                        String scopedLabel = this.bnodeMap.get(value);
                        if (scopedLabel == null) {
                            scopedLabel = "b" + bnodeCounter;
                            bnodeCounter += 1;
                            bnodeMap.put(value, scopedLabel);
                        }
                        result = DATA_FACTORY.getConstantBNode(scopedLabel);
                        break;
                    /**
                     * TODO: the language tag should be reserved to LITERAL_LANG
                     */
                    case LITERAL:
                    case LITERAL_LANG:
                        // The constant is a literal, we need to find if its
                        // rdfs:Literal or a normal literal and construct it
                        // properly.
                        String language = jdbcResultSet.getString(column - 1);
                        if (language == null || language.trim().equals(""))
                            result = DATA_FACTORY.getConstantLiteral(value);
                        else
                            result = DATA_FACTORY.getConstantLiteral(value, language);
                        break;

                    case BOOLEAN:
                        boolean bvalue = jdbcResultSet.getBoolean(column);
                        result = DATA_FACTORY.getBooleanConstant(bvalue);
                        break;

                    case DOUBLE:
                        double d = jdbcResultSet.getDouble(column);
                        String s = formatter.format(d); // format name into correct double representation
                        result = DATA_FACTORY.getConstantLiteral(s, type);
                        break;

                    case INT:
                        int intValue = jdbcResultSet.getInt(column);
                        result = DATA_FACTORY.getConstantLiteral(String.valueOf(intValue), type);
                        break;

                    case LONG:
                    case UNSIGNED_INT:
                        long longValue = jdbcResultSet.getLong(column);
                        result = DATA_FACTORY.getConstantLiteral(String.valueOf(longValue), type);
                        break;

                    case INTEGER:
                    case NEGATIVE_INTEGER:
                    case NON_NEGATIVE_INTEGER:
                    case POSITIVE_INTEGER:
                    case NON_POSITIVE_INTEGER:
                        /**
                         * Sometimes the integer may have been converted as DECIMAL, FLOAT or DOUBLE
                         */
                        int dotIndex = value.indexOf(".");
                        String integerString = dotIndex >= 0
                                ? value.substring(0, dotIndex)
                                : value;
                        result = DATA_FACTORY.getConstantLiteral(integerString, type);
                        break;

                    case DATETIME:
                        /** set.getTimestamp() gives problem with MySQL and Oracle drivers we need to specify the dateformat
                         MySQL DateFormat ("MMM DD YYYY HH:mmaa");
                         Oracle DateFormat "dd-MMM-yy HH.mm.ss.SSSSSS aa" For oracle driver v.11 and less
                         Oracle "dd-MMM-yy HH:mm:ss,SSSSSS" FOR ORACLE DRIVER 12.1.0.2
                         To overcome the problem we create a new Timestamp */
                        try {
                            Timestamp tsvalue = jdbcResultSet.getTimestamp(column);
                            result = DATA_FACTORY.getConstantLiteral(tsvalue.toString().replace(' ', 'T'), Predicate.COL_TYPE.DATETIME);
                        }
                        catch (Exception e) {
                            if (isMsSQL || isOracle) {
                                try {
                                    java.util.Date date = dateFormat.parse(value);
                                    Timestamp ts = new Timestamp(date.getTime());
                                    result = DATA_FACTORY.getConstantLiteral(ts.toString().replace(' ', 'T'), Predicate.COL_TYPE.DATETIME);
                                }
                                catch (ParseException pe) {
                                    throw new OntopResultConversionException(pe);
                                }
                            }
                            else
                                throw new OntopResultConversionException(e);
                        }
                        break;

                    case DATETIME_STAMP:
                        if (!isOracle) {
                            result = DATA_FACTORY.getConstantLiteral(value.replaceFirst(" ", "T").replaceAll(" ", ""), Predicate.COL_TYPE.DATETIME_STAMP);
                        }
                        else {
						/* oracle has the type timestamptz. The format returned by getString is not a valid xml format
						we need to transform it. We first take the information about the timezone value, that is lost
						during the conversion in java.util.Date and then we proceed with the conversion. */
                            try {
                                int indexTimezone = value.lastIndexOf(" ");
                                String timezone = value.substring(indexTimezone+1);
                                String datetime = value.substring(0, indexTimezone);

                                java.util.Date date = dateFormat.parse(datetime);
                                Timestamp ts = new Timestamp(date.getTime());
                                result = DATA_FACTORY.getConstantLiteral(ts.toString().replaceFirst(" ", "T").replaceAll(" ", "")+timezone, Predicate.COL_TYPE.DATETIME_STAMP);
                            }
                            catch (ParseException pe) {
                                throw new OntopResultConversionException(pe);
                            }
                        }
                        break;

                    case DATE:
                        if (!isOracle) {
                            Date dvalue = jdbcResultSet.getDate(column);
                            result = DATA_FACTORY.getConstantLiteral(dvalue.toString(), Predicate.COL_TYPE.DATE);
                        }
                        else {
                            try {
                                DateFormat df = new SimpleDateFormat("dd-MMM-yy" ,  Locale.ENGLISH);
                                java.util.Date date = df.parse(value);
                            }
                            catch (ParseException e) {
                                throw new OntopResultConversionException(e);
                            }
                            result = DATA_FACTORY.getConstantLiteral(value.toString(), Predicate.COL_TYPE.DATE);
                        }
                        break;

                    case TIME:
                        Time tvalue = jdbcResultSet.getTime(column);
                        result = DATA_FACTORY.getConstantLiteral(tvalue.toString().replace(' ', 'T'), Predicate.COL_TYPE.TIME);
                        break;

                    default:
                        result = DATA_FACTORY.getConstantLiteral(value, type);
                }
            }
        }
        catch (IllegalArgumentException e) {
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
            }
            else {
                OntopResultConversionException ex = new OntopResultConversionException("Quest couldn't parse the data value to Java object: " + value + "\n"
                        + "Please review the mapping rules to have the datatype assigned properly.");
                ex.setStackTrace(e.getStackTrace());
                throw ex;
            }
        }
        catch (SQLException e) {
            throw new OntopConnectionException(e);
        }

        return result;
    }


    @Override
    public Constant getConstant(String name) throws OntopConnectionException, OntopResultConversionException {
        Integer columnIndex = columnMap.get(name);
        return getConstant(columnIndex);
    }
}
