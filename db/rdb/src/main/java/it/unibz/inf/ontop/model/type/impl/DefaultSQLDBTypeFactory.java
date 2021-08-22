package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;
import static it.unibz.inf.ontop.model.type.DBTermType.Category.FLOAT_DOUBLE;
import static it.unibz.inf.ontop.model.type.DBTermType.Category.INTEGER;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.SAME_TYPE_NO_CONSTANT;

/**
 * See https://www.w3.org/TR/r2rml/#natural-mapping
 */
public class DefaultSQLDBTypeFactory implements SQLDBTypeFactory {

    protected static final String ABSTRACT_DB_TYPE_STR = "AbstractDBType";

    public static final String TEXT_STR = "TEXT";
    public static final String CHAR_STR = "CHAR";
    protected static final String CHARACTER_STR = "CHARACTER";
    protected static final String VARCHAR_STR = "VARCHAR";
    protected static final String CHAR_VAR_STR = "CHARACTER VARYING";
    protected static final String CLOB_STR = "CLOB";
    protected static final String CHAR_LARGE_STR = "CHARACTER LARGE OBJECT";
    public static final String NATIONAL_TEXT_STR = "NATIONAL TEXT";
    public static final String NTEXT_STR = "NTEXT";
    protected static final String NATIONAL_CHAR_STR = "NATIONAL CHARACTER";
    protected static final String NCHAR_STR = "NCHAR";
    protected static final String NATIONAL_CHAR_VAR_STR = "NATIONAL CHARACTER VARYING";
    protected static final String NVARCHAR_STR = "NVARCHAR";
    protected static final String NATIONAL_CHAR_LARGE_STR = "NATIONAL CHARACTER LARGE OBJECT";
    public static final String INTEGER_STR = "INTEGER";
    protected static final String INT_STR = "INT";
    public static final String TINYINT_STR = "TINYINT";
    public static final String SMALLINT_STR = "SMALLINT";
    protected static final String BIGINT_STR = "BIGINT";
    protected static final String NUMERIC_STR = "NUMERIC";
    protected static final String DECIMAL_STR = "DECIMAL";
    protected static final String FLOAT_STR = "FLOAT";
    protected static final String REAL_STR = "REAL";
    protected static final String DOUBLE_STR = "DOUBLE";
    protected static final String DOUBLE_PREC_STR = "DOUBLE PRECISION";
    protected static final String BOOLEAN_STR = "BOOLEAN";
    public static final String DATE_STR = "DATE";
    public static final String TIME_STR = "TIME";
    public static final String TIMESTAMP_STR = "TIMESTAMP";
    protected static final String BINARY_STR = "BINARY";
    protected static final String BINARY_VAR_STR = "BINARY VARYING";
    protected static final String VARBINARY_STR = "VARBINARY";
    protected static final String BINARY_LARGE_STR = "BINARY LARGE OBJECT";
    protected static final String BLOB_STR = "BLOB";
    protected final NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport defaultStrictEqSupport;

    protected enum DefaultTypeCode {
        STRING,
        HEXBINARY,
        LARGE_INTEGER,
        DECIMAL,
        DOUBLE,
        BOOLEAN,
        DATE,
        TIME,
        DATETIMESTAMP,
        GEOMETRY,
        GEOGRAPHY
    }

    // MUTABLE
    private final Map<String, DBTermType> sqlTypeMap;
    private final ImmutableMap<DefaultTypeCode, String> defaultTypeCodeMap;

    @AssistedInject
    private DefaultSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        this(createDefaultSQLTypeMap(rootTermType, typeFactory), ImmutableMap.copyOf(createDefaultSQLCodeMap()));
    }

    protected DefaultSQLDBTypeFactory(Map<String, DBTermType> typeMap,
                                      ImmutableMap<DefaultTypeCode, String> defaultTypeCodeMap) {
        sqlTypeMap = typeMap;
        this.defaultTypeCodeMap = defaultTypeCodeMap;
        // TODO: get it from the settings
        this.defaultStrictEqSupport = SAME_TYPE_NO_CONSTANT;
    }

    /**
     * Returns a mutable map so that it can be modified by sub-classes
     */
    protected static Map<String, DBTermType> createDefaultSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        DBTermType rootDBType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(ABSTRACT_DB_TYPE_STR,
                rootTermType.getAncestry(), true);

        TermTypeAncestry rootAncestry = rootDBType.getAncestry();

        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype hexBinary = typeFactory.getDatatype(XSD.HEXBINARY);
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFDatatype xsdDecimal = typeFactory.getXsdDecimalDatatype();
        RDFDatatype xsdDouble = typeFactory.getXsdDoubleDatatype();
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();

        // TODO: complete
        return Stream.of(rootDBType,
                    new StringDBTermType(TEXT_STR, rootAncestry, xsdString),
                    new StringDBTermType(CHAR_STR, rootAncestry, xsdString),
                    // TODO: group aliases?
                    new StringDBTermType(CHARACTER_STR, rootAncestry, xsdString),
                    new StringDBTermType(VARCHAR_STR, rootAncestry, xsdString),
                    new StringDBTermType(CHAR_VAR_STR, rootAncestry, xsdString),
                    new StringDBTermType(CHAR_LARGE_STR, rootAncestry, xsdString),
                    new StringDBTermType(CLOB_STR, rootAncestry, xsdString),
                    new StringDBTermType(NATIONAL_TEXT_STR, rootAncestry, xsdString),
                    new StringDBTermType(NTEXT_STR, rootAncestry, xsdString),
                    new StringDBTermType(NATIONAL_CHAR_STR, rootAncestry, xsdString),
                    new StringDBTermType(NCHAR_STR, rootAncestry, xsdString),
                    new StringDBTermType(NATIONAL_CHAR_VAR_STR, rootAncestry, xsdString),
                    new StringDBTermType(NVARCHAR_STR, rootAncestry, xsdString),
                    new StringDBTermType(NATIONAL_CHAR_LARGE_STR, rootAncestry, xsdString),
                    new NonStringNonNumberNonBooleanNonDatetimeDBTermType(BINARY_STR, rootAncestry, hexBinary),
                    new NonStringNonNumberNonBooleanNonDatetimeDBTermType(BINARY_VAR_STR, rootAncestry, hexBinary),
                    new NonStringNonNumberNonBooleanNonDatetimeDBTermType(VARBINARY_STR, rootAncestry, hexBinary),
                    new NonStringNonNumberNonBooleanNonDatetimeDBTermType(BINARY_LARGE_STR, rootAncestry, hexBinary),
                    new NonStringNonNumberNonBooleanNonDatetimeDBTermType(BLOB_STR, rootAncestry, hexBinary),
                    new NumberDBTermType(INTEGER_STR, rootAncestry, xsdInteger, INTEGER),
                    new NumberDBTermType(INT_STR, rootAncestry, xsdInteger, INTEGER),
                    // Non-standard (not part of the R2RML standard). Range changing from a DB engine to the otherk
                    new NumberDBTermType(TINYINT_STR, rootAncestry, xsdInteger, INTEGER),
                    new NumberDBTermType(SMALLINT_STR, rootAncestry, xsdInteger, INTEGER),
                    new NumberDBTermType(BIGINT_STR, rootAncestry, xsdInteger, INTEGER),
                    new NumberDBTermType(NUMERIC_STR, rootAncestry, xsdDecimal, DECIMAL),
                    new NumberDBTermType(DECIMAL_STR, rootAncestry, xsdDecimal, DECIMAL),
                    new NumberDBTermType(FLOAT_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE),
                    new NumberDBTermType(REAL_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE),
                    new NumberDBTermType(DOUBLE_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE),
                    new NumberDBTermType(DOUBLE_PREC_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE),
                    new BooleanDBTermType(BOOLEAN_STR, rootTermType.getAncestry(), xsdBoolean),
                    new NonStringNonNumberNonBooleanNonDatetimeDBTermType(DATE_STR, rootAncestry, typeFactory.getDatatype(XSD.DATE)),
                    new NonStringNonNumberNonBooleanNonDatetimeDBTermType(TIME_STR, rootTermType.getAncestry(), typeFactory.getDatatype(XSD.TIME)),
                    new DatetimeDBTermType(TIMESTAMP_STR, rootTermType.getAncestry(), typeFactory.getXsdDatetimeDatatype())
                )
                .collect(Collectors.toMap(
                        DBTermType::getName,
                        t -> t));
    }

    /**
     * Returns a mutable map so that it can be modified by sub-classes
     *
     * NB: we use the largest option among the DB datatypes mapped to the same XSD type.
     *
     */
    protected static Map<DefaultTypeCode, String> createDefaultSQLCodeMap() {
        Map<DefaultTypeCode, String> map = new HashMap<>();
        map.put(DefaultTypeCode.STRING, TEXT_STR);
        map.put(DefaultTypeCode.HEXBINARY, BINARY_LARGE_STR);
        map.put(DefaultTypeCode.LARGE_INTEGER, BIGINT_STR);
        map.put(DefaultTypeCode.DECIMAL, DECIMAL_STR);
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_STR);
        map.put(DefaultTypeCode.BOOLEAN, BOOLEAN_STR);
        map.put(DefaultTypeCode.DATE, DATE_STR);
        map.put(DefaultTypeCode.TIME, TIME_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMP_STR);
        return map;
    }

    @Override
    public DBTermType getDBTermType(String typeName) {
        String typeString = preprocessTypeName(typeName);

        /*
         * Creates a new term type if not known
         */
        return sqlTypeMap.computeIfAbsent(typeString,
                s -> new NonStringNonNumberNonBooleanNonDatetimeDBTermType(s, sqlTypeMap.get(ABSTRACT_DB_TYPE_STR).getAncestry(),
                        defaultStrictEqSupport));
    }

    @Override
    public DBTermType getDBTermType(String typeName, int columnSize) {
        String typeString = preprocessTypeName(typeName, columnSize);

        /*
         * Creates a new term type if not known
         */
        return sqlTypeMap.computeIfAbsent(typeString,
                s -> new NonStringNonNumberNonBooleanNonDatetimeDBTermType(s, sqlTypeMap.get(ABSTRACT_DB_TYPE_STR).getAncestry(),
                        defaultStrictEqSupport));
    }

    @Override
    public String getDBTrueLexicalValue() {
        return "TRUE";
    }

    @Override
    public String getDBFalseLexicalValue() {
        return "FALSE";
    }

    @Override
    public String getNullLexicalValue() {
        return "NULL";
    }

    @Override
    public Optional<String> getDBNaNLexicalValue() {
        return Optional.of("NaN");
    }

    private static final Pattern OPTIONAL_LENGTH = Pattern.compile("\\([\\d, ]+\\)");

    /**
     * Can be overridden
     */
    protected String preprocessTypeName(String typeName) {
        return OPTIONAL_LENGTH.matcher(typeName).replaceAll("")
                .toUpperCase();
    }

    /**
     * By default, ignore the column size
     * Can be overridden
     */
    protected String preprocessTypeName(String typeName, int columnSize) {
        return OPTIONAL_LENGTH.matcher(typeName).replaceAll("")
                .toUpperCase();
    }

    @Override
    public DBTermType getDBStringType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.STRING));
    }

    @Override
    public DBTermType getDBLargeIntegerType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.LARGE_INTEGER));
    }

    @Override
    public DBTermType getDBDecimalType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.DECIMAL));
    }

    @Override
    public DBTermType getDBBooleanType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.BOOLEAN));
    }

    @Override
    public DBTermType getDBDateType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.DATE));
    }

    @Override
    public DBTermType getDBTimeType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.TIME));
    }

    @Override
    public DBTermType getDBDateTimestampType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.DATETIMESTAMP));
    }

    @Override
    public DBTermType getDBDoubleType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.DOUBLE));
    }

    @Override
    public DBTermType getDBGeometryType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.GEOMETRY));
    }

    @Override
    public DBTermType getDBGeographyType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.GEOGRAPHY));
    }

    @Override
    public boolean supportsDBGeometryType() {
        return false;
    }

    @Override
    public boolean supportsDBGeographyType() {
        return false;
    }

    @Override
    public boolean supportsDBDistanceSphere() {
        return false;
    }

    @Override
    public DBTermType getDBHexBinaryType() {
        return sqlTypeMap.get(defaultTypeCodeMap.get(DefaultTypeCode.HEXBINARY));
    }

    @Override
    public DBTermType getAbstractRootDBType() {
        return sqlTypeMap.get(ABSTRACT_DB_TYPE_STR);
    }
}
