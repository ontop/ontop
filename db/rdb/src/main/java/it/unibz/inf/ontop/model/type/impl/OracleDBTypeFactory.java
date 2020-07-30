package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;
import static it.unibz.inf.ontop.model.type.DBTermType.Category.FLOAT_DOUBLE;
import static it.unibz.inf.ontop.model.type.DBTermType.Category.INTEGER;

/**
 *
 *
 * Remarks:
 *   - NUMBER is used both for integers and decimals
 *   - Oracle does not have a proper boolean type... A common practice is to use NUMBER(1)
 *
 *  Assumption: the default character set of VARCHAR2 on the Oracle instance is expected to contain
 *  all the possible characters that can be encountered by the application. NB: on modern instances,
 *  it is usually UTF-8, which is safe.
 *
 *
 */
public class OracleDBTypeFactory extends DefaultSQLDBTypeFactory {

    public static final String NUMBER_STR = "NUMBER";
    public static final String NUMBER_19_STR = "NUMBER(19)";
    public static final String VARCHAR2_STR = "VARCHAR2";
    public static final String NVARCHAR2_STR = "NVARCHAR2";
    public static final String BINARY_FLOAT_STR = "BINARY_FLOAT";
    public static final String BINARY_DOUBLE_STR = "BINARY_DOUBLE";
    public static final String TIMESTAMP_LOCAL_TZ_STR = "TIMESTAMP WITH LOCAL TIME ZONE";
    public static final String TIMESTAMP_TZ_STR = "TIMESTAMP WITH TIME ZONE";

    @AssistedInject
    private OracleDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createOracleTypeMap(rootTermType, typeFactory), createOracleCodeMap());
    }

    private static Map<String, DBTermType> createOracleTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        DBTermType numberType = new NumberDBTermType(NUMBER_STR, rootAncestry, typeFactory.getXsdDecimalDatatype(), DECIMAL);

        // BIGINT in other DBMSes have a precision close to NUMBER(19) while Oracle could go in theory up to NUMBER(38)
        // NB: 2019-04: This type is NOT extracted by the DBMetadataExtractor -> integers are always inferred as decimal in the mapping
        DBTermType number19Type = new NumberDBTermType(NUMBER_19_STR, rootAncestry, typeFactory.getXsdIntegerDatatype(), INTEGER);
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        DBTermType varchar2Type = new StringDBTermType(VARCHAR2_STR, rootAncestry, xsdString);
        DBTermType nVarchar2Type = new StringDBTermType(NVARCHAR2_STR, rootAncestry, xsdString);

        RDFDatatype xsdDouble = typeFactory.getXsdDoubleDatatype();
        NumberDBTermType binaryFloatType = new NumberDBTermType(BINARY_FLOAT_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE);
        NumberDBTermType binaryDoubleType = new NumberDBTermType(BINARY_DOUBLE_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE);

        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        DatetimeDBTermType timestampLTZType = new DatetimeDBTermType(TIMESTAMP_LOCAL_TZ_STR, rootAncestry, xsdDatetime);
        DatetimeDBTermType timestampTZType = new DatetimeDBTermType(TIMESTAMP_TZ_STR, rootAncestry, xsdDatetime);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(NUMBER_STR, numberType);
        map.put(NUMBER_19_STR, number19Type);
        map.put(VARCHAR2_STR, varchar2Type);
        map.put(NVARCHAR2_STR, nVarchar2Type);
        map.put(BINARY_FLOAT_STR, binaryFloatType);
        map.put(BINARY_DOUBLE_STR, binaryDoubleType);
        map.put(TIMESTAMP_LOCAL_TZ_STR, timestampLTZType);
        map.put(TIMESTAMP_TZ_STR, timestampTZType);
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createOracleCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.DECIMAL, NUMBER_STR);
        map.put(DefaultTypeCode.LARGE_INTEGER, NUMBER_19_STR);
        map.put(DefaultTypeCode.DOUBLE, BINARY_DOUBLE_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMP_TZ_STR);
        // NB: Conversions to VARCHAR2 use the TO_CHAR(...) function
        map.put(DefaultTypeCode.STRING, VARCHAR2_STR);
        return ImmutableMap.copyOf(map);
    }

    @Override
    public String getDBTrueLexicalValue() {
        return "1";
    }

    @Override
    public String getDBFalseLexicalValue() {
        return "0";
    }
}
