package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.*;

/**
 * https://docs.snowflake.com/en/sql-reference/intro-summary-data-types.html
 *
 * Remarks:
 *   - NUMBER is used both for integers and decimals
 *
 */
public class SnowflakeDBTypeFactory extends DefaultSQLDBTypeFactory {

    public static final String NUMBER_STR = "NUMBER";
    public static final String NUMBER_19_STR = "NUMBER(19)";
    public static final String TIMESTAMP_LOCAL_TZ_STR = "TIMESTAMP_LTZ";
    public static final String TIMESTAMPLTZ_STR = "TIMESTAMPLTZ";
    public static final String TIMESTAMP_TZ_STR = "TIMESTAMP_TZ";
    public static final String TIMESTAMPTZ_STR = "TIMESTAMPTZ";

    public static final String TIMESTAMP_NO_TZ_STR = "TIMESTAMP_NTZ";
    public static final String TIMESTAMPNTZ_STR = "TIMESTAMPNTZ";

    @AssistedInject
    private SnowflakeDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createSnowflakeTypeMap(rootTermType, typeFactory), createSnowflakeCodeMap());
    }

    private static Map<String, DBTermType> createSnowflakeTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        DBTermType numberType = new NumberDBTermType(NUMBER_STR, rootAncestry, typeFactory.getXsdDecimalDatatype(), DECIMAL);

        // BIGINT in other DBMSes have a precision close to NUMBER(19) while Oracle could go in theory up to NUMBER(38)
        // NB: 2019-04: This type is NOT extracted by the DBMetadataExtractor -> integers are always inferred as decimal in the mapping
        DBTermType number19Type = new NumberDBTermType(NUMBER_19_STR, rootAncestry, typeFactory.getXsdIntegerDatatype(), INTEGER);

        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        DatetimeDBTermType timestampLTZType = new DatetimeDBTermType(TIMESTAMP_LOCAL_TZ_STR, rootAncestry, xsdDatetime);
        DatetimeDBTermType timestampTZType = new DatetimeDBTermType(TIMESTAMP_TZ_STR, rootAncestry, xsdDatetime);
        DatetimeDBTermType timestampNTZType = new DatetimeDBTermType(TIMESTAMP_NO_TZ_STR, rootAncestry, xsdDatetime);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(NUMBER_STR, numberType);
        map.put(NUMBER_19_STR, number19Type);
        map.put(TIMESTAMP_LOCAL_TZ_STR, timestampLTZType);
        map.put(TIMESTAMP_TZ_STR, timestampTZType);
        map.put(TIMESTAMP_NO_TZ_STR, timestampNTZType);
        map.put(TIMESTAMPLTZ_STR, timestampLTZType);
        map.put(TIMESTAMPTZ_STR, timestampTZType);
        map.put(TIMESTAMPNTZ_STR, timestampNTZType);
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createSnowflakeCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.STRING, VARCHAR_STR);
        map.put(DefaultTypeCode.HEXBINARY, BINARY_STR);
        map.put(DefaultTypeCode.DECIMAL, NUMBER_STR);
        map.put(DefaultTypeCode.LARGE_INTEGER, NUMBER_19_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMP_TZ_STR);
        return ImmutableMap.copyOf(map);
    }
}
