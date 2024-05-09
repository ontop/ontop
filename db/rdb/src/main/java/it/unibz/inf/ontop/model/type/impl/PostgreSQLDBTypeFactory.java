package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.INTEGER;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.NOTHING;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.SAME_TYPE_NO_CONSTANT;

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
    public static final String OID_STR = "OID";
    public static final String JSON_STR = "JSON";
    public static final String JSONB_STR = "JSONB";
    public static final String ARRAY_STR = "T[]";
    public static final String BYTEA_STR = "BYTEA";

    protected static final String GEOMETRY_STR = "GEOMETRY";
    protected static final String GEOGRAPHY_STR = "GEOGRAPHY";

    //For the ARRAY definition style T[size] or T[]
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[[\\d ]*\\]$");

    //For the ARRAY definition style T ARRAY[size] or T ARRAY
    private static final Pattern ARRAY_PATTERN_2 = Pattern.compile(" ARRAY(\\[[\\d ]+\\])?$");



    @AssistedInject
    protected PostgreSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createPostgreSQLTypeMap(rootTermType, typeFactory), createPostgreSQLCodeMap(), createGenericAbstractTypeMap(rootTermType, typeFactory));
    }

    private static ImmutableList<GenericDBTermType> createGenericAbstractTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        GenericDBTermType abstractArrayType = new ArrayDBTermType(ARRAY_STR, rootAncestry, s -> {
            if(s.startsWith("_"))
                return Optional.of(typeFactory.getDBTypeFactory().getDBTermType(s.substring(1)));

            var matcher2 = ARRAY_PATTERN_2.matcher(s);
            if(matcher2.find())
                return Optional.of(typeFactory.getDBTypeFactory().getDBTermType(s.substring(0, matcher2.start())));

            var matcher = ARRAY_PATTERN.matcher(s);
            if(matcher.find())
                return Optional.of(typeFactory.getDBTypeFactory().getDBTermType(s.substring(0, matcher.start())));

            return Optional.empty();
        });

        List<GenericDBTermType> list = new ArrayList<>();
        list.add(abstractArrayType);
        return ImmutableList.copyOf(list);
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

        DBTermType byteAType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(BYTEA_STR, rootAncestry,
                typeFactory.getDatatype(XSD.HEXBINARY), SAME_TYPE_NO_CONSTANT);

        DBTermType uuidType = new UUIDDBTermType(UUID_STR, rootTermType.getAncestry(), xsdString);

        DBTermType oidType = new NumberDBTermType(OID_STR, rootAncestry, typeFactory.getDatatype(XSD.INTEGER), INTEGER);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(BIT_STR, bitType);
        map.put(INT2_STR, map.get(SMALLINT_STR));
        map.put(INT4_STR, map.get(INTEGER_STR));
        map.put(INT8_STR, map.get(BIGINT_STR));
        map.put(OID_STR, oidType);
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
        map.put(BYTEA_STR, byteAType);

        /*
         * POSTGIS types
         *
         */
        map.put(GEOMETRY_STR, new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOMETRY_STR, rootAncestry, xsdString));
        map.put(GEOGRAPHY_STR, new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOGRAPHY_STR, rootAncestry, xsdString));

        /*
         * JSON
         */
        map.put(JSON_STR, new JsonDBTermTypeImpl(JSON_STR, rootAncestry));
        map.put(JSONB_STR, new JsonDBTermTypeImpl(JSONB_STR, rootAncestry));

        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createPostgreSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_PREC_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMPTZ_STR);
        map.put(DefaultTypeCode.HEXBINARY, BYTEA_STR);
        /*
         * POSTGIS types
         */
        map.put(DefaultTypeCode.GEOGRAPHY, GEOGRAPHY_STR);
        map.put(DefaultTypeCode.GEOMETRY, GEOMETRY_STR);
        /*
         * JSON: JSONB is more efficient than JSON
         */
        map.put(DefaultTypeCode.JSON, JSONB_STR);

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

    @Override
    public boolean supportsJson() {
        return true;
    }
}
