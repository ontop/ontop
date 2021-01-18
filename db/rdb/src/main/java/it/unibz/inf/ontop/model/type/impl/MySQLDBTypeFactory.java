package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;
import static it.unibz.inf.ontop.model.type.DBTermType.Category.INTEGER;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.SAME_TYPE;

public class MySQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    public static final String BIT_STR = "BIT";
    public static final String BIT_ONE_STR = "BIT(1)";
    private static final String TINY_INT_ONE_STR = "TINYINT(1)";
    protected static final String TINYBLOB_STR = "TINYBLOB";
    protected static final String MEDIUMBLOB_STR = "MEDIUMBLOB";
    protected static final String LONGBLOB_STR = "LONGBLOB";
    protected static final String TINYTEXT_STR = "TINYTEXT";
    protected static final String MEDIUMTEXT_STR = "MEDIUMTEXT";
    protected static final String LONGTEXT_STR = "LONGTEXT";
    protected static final String SET_STR = "SET";
    protected static final String ENUM_STR = "ENUM";
    protected static final String MEDIUMINT_STR = "MEDIUMINT";
    protected static final String UNSIGNED_SUFFIX = " UNSIGNED";
    public static final String YEAR_STR = "YEAR";

    public static final String DATETIME_STR = "DATETIME";

    @AssistedInject
    protected MySQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createMySQLTypeMap(rootTermType, typeFactory), createMySQLCodeMap());
    }

    protected static Map<String, DBTermType> createMySQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();

        // Overloads BIGINT to use SIGNED for casting purposes
        NumberDBTermType bigIntType = new NumberDBTermType(BIGINT_STR, "SIGNED", rootAncestry, xsdInteger, INTEGER);

        // Overloads NVARCHAR to insert the precision
        StringDBTermType textType = new StringDBTermType(TEXT_STR, "CHAR CHARACTER SET utf8", rootAncestry,
                typeFactory.getXsdStringDatatype());

        // Overloads DECIMAL to specify a precision for casting purposes
        NumberDBTermType decimalType = new NumberDBTermType(DECIMAL_STR, "DECIMAL(60,30)", rootAncestry,
                typeFactory.getXsdDecimalDatatype(), DECIMAL);

        // Non-standard (not part of the R2RML standard).
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        StringDBTermType tinyBlobType = new StringDBTermType(TINYBLOB_STR, rootAncestry, xsdString);
        StringDBTermType mediumBlobType = new StringDBTermType(MEDIUMBLOB_STR, rootAncestry, xsdString);
        StringDBTermType longBlobType = new StringDBTermType(LONGBLOB_STR, rootAncestry, xsdString);
        StringDBTermType tinyTextType = new StringDBTermType(TINYTEXT_STR, rootAncestry, xsdString);
        StringDBTermType mediumTextType = new StringDBTermType(MEDIUMTEXT_STR, rootAncestry, xsdString);
        StringDBTermType longTextType = new StringDBTermType(LONGTEXT_STR, rootAncestry, xsdString);
        StringDBTermType setTextType = new StringDBTermType(SET_STR, rootAncestry, xsdString);
        StringDBTermType enumTextType = new StringDBTermType(ENUM_STR, rootAncestry, xsdString);

        NumberDBTermType mediumIntType = new NumberDBTermType(MEDIUMINT_STR, rootAncestry, xsdInteger, INTEGER);
        // Unsigned
        NumberDBTermType unsignedTinyIntType = new NumberDBTermType(TINYINT_STR + UNSIGNED_SUFFIX, rootAncestry, xsdInteger, INTEGER);
        NumberDBTermType unsignedSmallIntType = new NumberDBTermType(SMALLINT_STR + UNSIGNED_SUFFIX, rootAncestry, xsdInteger, INTEGER);
        NumberDBTermType unsignedMediumIntType = new NumberDBTermType(MEDIUMINT_STR + UNSIGNED_SUFFIX, rootAncestry, xsdInteger, INTEGER);
        NumberDBTermType unsignedIntType = new NumberDBTermType(INT_STR + UNSIGNED_SUFFIX, rootAncestry, xsdInteger, INTEGER);
        NumberDBTermType unsignedBigIntType = new NumberDBTermType(BIGINT_STR + UNSIGNED_SUFFIX, rootAncestry, xsdInteger, INTEGER);


        // NB: TIMESTAMP also exists
        // TODO: check if lexical values can be considered as unique
        DatetimeDBTermType datetimeType = new DatetimeDBTermType(DATETIME_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        // TODO: shall we treat BIT as a number? Then, we would have to serialize it differently (e.g. b'011111')
        // TODO: check if lexical values can be considered as unique
        DBTermType defaultBitType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(BIT_STR, rootAncestry, false);

        // Special cases that are interpreted as booleans
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();
        BooleanDBTermType bitOneType = new BooleanDBTermType(BIT_ONE_STR, rootTermType.getAncestry(), xsdBoolean);

        // TODO: check the strict eq support level
        DBTermType yearType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(YEAR_STR, rootAncestry,
                typeFactory.getDatatype(XSD.GYEAR), SAME_TYPE);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(BIT_ONE_STR, bitOneType);
        map.put(BIT_STR, defaultBitType);
        map.put(TINYBLOB_STR, tinyBlobType);
        map.put(MEDIUMBLOB_STR, mediumBlobType);
        map.put(LONGBLOB_STR, longBlobType);
        map.put(TINYTEXT_STR, tinyTextType);
        map.put(MEDIUMTEXT_STR, mediumTextType);
        map.put(LONGTEXT_STR, longTextType);
        map.put(SET_STR, setTextType);
        map.put(ENUM_STR, enumTextType);
        map.put(TEXT_STR, textType);
        map.put(MEDIUMINT_STR, mediumIntType);
        map.put(BIGINT_STR, bigIntType);
        map.put(TINYINT_STR + UNSIGNED_SUFFIX, unsignedTinyIntType);
        map.put(SMALLINT_STR + UNSIGNED_SUFFIX, unsignedSmallIntType);
        map.put(MEDIUMINT_STR + UNSIGNED_SUFFIX, unsignedMediumIntType);
        map.put(INT_STR + UNSIGNED_SUFFIX, unsignedIntType);
        map.put(BIGINT_STR + UNSIGNED_SUFFIX, unsignedBigIntType);
        map.put(DECIMAL_STR, decimalType);
        map.put(DATETIME_STR, datetimeType);
        map.put(YEAR_STR, yearType);
        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createMySQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        // Because CAST to DOUBLE is not supported by MySQL but cast to DECIMAL is.
        map.put(DefaultTypeCode.DOUBLE, DECIMAL_STR);
        // Only CAST to DATETIME is supported by MySQL, not CAST to TIMESTAMP
        map.put(DefaultTypeCode.DATETIMESTAMP, DATETIME_STR);
        return ImmutableMap.copyOf(map);
    }

    @Override
    public Optional<String> getDBNaNLexicalValue() {
        return Optional.empty();
    }

    /**
     * Keeps the parameters for BIT(1).
     * Transforms TINYINT(1) into BOOLEAN (alias)
     *
     * For the other type strings, performs the standard pre-processing.
     */
    @Override
    protected String preprocessTypeName(String typeName) {
        String capitalizedTypeName = typeName.toUpperCase();
        switch (capitalizedTypeName) {
            case TINY_INT_ONE_STR:
                return BOOLEAN_STR;
            case BIT_ONE_STR:
                return capitalizedTypeName;
            default:
                return super.preprocessTypeName(capitalizedTypeName);
        }
    }

    /**
     * Transforms (BIT, 1) and (TINYINT, 1) into BIT(1) and BOOLEAN (alias)
     */
    @Override
    protected String preprocessTypeName(String typeName, int columnSize) {
        String capitalizedTypeName = typeName.toUpperCase();
        switch (capitalizedTypeName) {
            case TINYINT_STR:
                if (columnSize == 1)
                    return BOOLEAN_STR;
                break;
            case BIT_STR:
                if (columnSize == 1)
                    return capitalizedTypeName + "(1)";
                break;
        }
        return super.preprocessTypeName(capitalizedTypeName);
    }

    /**
     * NB: TRUE is an alias of 1
     */
    @Override
    public String getDBTrueLexicalValue() {
        return "1";
    }

    /**
     * NB: FALSE is an alias of 0
     */
    @Override
    public String getDBFalseLexicalValue() {
        return "0";
    }
}
