package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.NOTHING;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.WITH_ALL;

public class PostgreSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String VARBIT_STR = "VARBIT";
    protected static final String BIT_STR = "BIT";
    protected static final String INT2_STR = "INT2";
    protected static final String INT4_STR = "INT4";
    protected static final String INT8_STR = "INT8";
    protected static final String FLOAT4_STR = "FLOAT4";
    protected static final String FLOAT8_STR = "FLOAT8";
    protected static final String SMALLSERIAL_STR = "SMALLSERIAL";
    public static final String SERIAL_STR = "SERIAL";
    protected static final String BIGSERIAL_STR = "BIGSERIAL";
    protected static final String BPCHAR_STR = "BPCHAR";
    protected static final String NAME_STR = "NAME";
    public static final String TIMESTAMPTZ_STR = "TIMESTAMPTZ";
    public static final String TIMETZ_STR = "TIMETZ";
    public static final String BOOL_STR = "BOOL";
    public static final String UUID_STR = "UUID";

    protected static final String GEOMETRY_STR = "GEOMETRY";
    protected static final String GEOGRAPHY_STR = "GEOGRAPHY";



    @AssistedInject
    protected PostgreSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createPostgreSQLTypeMap(rootTermType, typeFactory), createPostgreSQLCodeMap());
    }

    protected static Map<String, DBTermType> createPostgreSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();

        // TODO: treat it as a proper binary type
        BooleanDBTermType bitType = new BooleanDBTermType(BIT_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

        // TODO: treat it as a proper binary type
        BooleanDBTermType varBitType = new BooleanDBTermType(VARBIT_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

        StringDBTermType bpCharType = new StringDBTermType(BPCHAR_STR, rootAncestry, xsdString);
        StringDBTermType nameType = new StringDBTermType(NAME_STR, rootAncestry, xsdString);

        // TODO: shall we map it to xsd.datetimeStamp ? (would not follow strictly R2RML but be more precise)
        DatetimeDBTermType timestampTz = new DatetimeDBTermType(TIMESTAMPTZ_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        DBTermType timeTzType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(TIMETZ_STR, rootAncestry,
                typeFactory.getDatatype(XSD.TIME), NOTHING);

        DBTermType dateType = new DateDBTermType(DATE_STR, rootAncestry,
                typeFactory.getDatatype(XSD.DATE));

        DBTermType uuidType = new UUIDDBTermType(UUID_STR, rootTermType.getAncestry(), xsdString);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(BIT_STR, bitType);
        map.put(INT2_STR, map.get(SMALLINT_STR));
        map.put(INT4_STR, map.get(INTEGER_STR));
        map.put(INT8_STR, map.get(BIGINT_STR));
        map.put(VARBIT_STR, varBitType);
        map.put(FLOAT4_STR, map.get(REAL_STR));
        map.put(FLOAT8_STR, map.get(DOUBLE_PREC_STR));
        /*
         * <a href='https://www.postgresql.org/docs/current/datatype-numeric.html'>8.1. Numeric Types</a>
         * The data types smallserial, serial and bigserial are not true types, but merely a notational convenience for
         * creating unique identifier columns (similar to the AUTO_INCREMENT property supported by some other databases).
         */
        map.put(SMALLSERIAL_STR, map.get(SMALLINT_STR));
        map.put(SERIAL_STR, map.get(INTEGER_STR));
        map.put(BIGSERIAL_STR, map.get(BIGINT_STR));
        map.put(BPCHAR_STR, bpCharType);
        map.put(NAME_STR, nameType);
        map.put(TIMESTAMPTZ_STR, timestampTz);
        map.put(TIMETZ_STR, timeTzType);
        map.put(DATE_STR, dateType);
        map.put(BOOL_STR, map.get(BOOLEAN_STR));
        map.put(UUID_STR, uuidType);

        /*
         * POSTGIS types
         */
        map.put(GEOMETRY_STR, new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOMETRY_STR, rootAncestry, xsdString));
        map.put(GEOGRAPHY_STR, new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOGRAPHY_STR, rootAncestry, xsdString));

        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createPostgreSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_PREC_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMPTZ_STR);
        /*
         * POSTGIS types
         */
        map.put(DefaultTypeCode.GEOGRAPHY, GEOGRAPHY_STR);
        map.put(DefaultTypeCode.GEOMETRY, GEOMETRY_STR);

        return ImmutableMap.copyOf(map);
    }

    @Override
    public boolean supportsDBGeometryType() {
        return true;
    }

    @Override
    public boolean supportsDBGeographyType() {
        return true;
    }

    @Override
    public boolean supportsDBDistanceSphere() {
        return true;
    }
}
