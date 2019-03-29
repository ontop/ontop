package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Map;
import java.util.Optional;

public class MySQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String BIT_STR = "BIT";
    protected static final String TINYBLOB_STR = "TINYBLOB";
    protected static final String MEDIUMBLOB_STR = "MEDIUMBLOB";
    protected static final String LONGBLOB_STR = "LONGBLOB";
    protected static final String TINYTEXT_STR = "TINYTEXT";
    protected static final String MEDIUMTEXT_STR = "MEDIUMTEXT";
    protected static final String LONGTEXT_STR = "LONGTEXT";
    protected static final String SET_STR = "SET";
    protected static final String ENUM_STR = "ENUM";
    protected static final String MEDIUMINT_STR = "MEDIUMINT";

    public static final String DATETIME_STR = "DATETIME";

    @AssistedInject
    protected MySQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createMySQLTypeMap(rootTermType, typeFactory), createMySQLCodeMap());
    }

    private static Map<String, DBTermType> createMySQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();

        // Overloads BIGINT to use SIGNED for casting purposes
        NumberDBTermType bigIntType = new NumberDBTermType(BIGINT_STR, "SIGNED", rootAncestry, xsdInteger);

        // Overloads NVARCHAR to insert the precision
        StringDBTermType textType = new StringDBTermType(TEXT_STR, "CHAR CHARACTER SET utf8", rootAncestry,
                typeFactory.getXsdStringDatatype());

        // Non-standard (not part of the R2RML standard).
        BooleanDBTermType bitType = new BooleanDBTermType(BIT_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        StringDBTermType tinyBlobType = new StringDBTermType(TINYBLOB_STR, rootAncestry, xsdString);
        StringDBTermType mediumBlobType = new StringDBTermType(MEDIUMBLOB_STR, rootAncestry, xsdString);
        StringDBTermType longBlobType = new StringDBTermType(LONGBLOB_STR, rootAncestry, xsdString);
        StringDBTermType tinyTextType = new StringDBTermType(TINYTEXT_STR, rootAncestry, xsdString);
        StringDBTermType mediumTextType = new StringDBTermType(MEDIUMTEXT_STR, rootAncestry, xsdString);
        StringDBTermType longTextType = new StringDBTermType(LONGTEXT_STR, rootAncestry, xsdString);
        StringDBTermType setTextType = new StringDBTermType(SET_STR, rootAncestry, xsdString);
        StringDBTermType enumTextType = new StringDBTermType(ENUM_STR, rootAncestry, xsdString);

        NumberDBTermType mediumIntType = new NumberDBTermType(MEDIUMINT_STR, rootAncestry, xsdInteger);

        // NB: TIMESTAMP also exists
        DatetimeDBTermType datetimeType = new DatetimeDBTermType(DATETIME_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(BIT_STR, bitType);
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
        map.put(DATETIME_STR, datetimeType);
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createMySQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        // Because CAST to DOUBLE is not supported by MySQL but cast to DECIMAL is.
        map.put(DefaultTypeCode.DOUBLE, DECIMAL_STR);
        return ImmutableMap.copyOf(map);
    }

    @Override
    public Optional<String> getDBNaNLexicalValue() {
        return Optional.empty();
    }
}
