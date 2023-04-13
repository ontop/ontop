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

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.NOTHING;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.SAME_TYPE_NO_CONSTANT;

public class RedshiftDBTypeFactory extends DefaultSQLDBTypeFactory {
    protected static final String INT2_STR = "INT2";
    protected static final String INT4_STR = "INT4";
    protected static final String INT8_STR = "INT8";
    protected static final String FLOAT4_STR = "FLOAT4";
    protected static final String FLOAT8_STR = "FLOAT8";
    protected static final String BPCHAR_STR = "BPCHAR";
    public static final String TIMESTAMPTZ_STR = "TIMESTAMPTZ";
    public static final String TIMETZ_STR = "TIMETZ";
    public static final String BOOL_STR = "BOOL";
    private static final String DEFAULT_DECIMAL_STR = "DECIMAL(38, 18)";

    protected static final String GEOMETRY_STR = "GEOMETRY";
    protected static final String GEOGRAPHY_STR = "GEOGRAPHY";

    protected static final String SUPER_STR = "SUPER";


    protected RedshiftDBTypeFactory(Map<String, DBTermType> typeMap, ImmutableMap<DefaultTypeCode, String> defaultTypeCodeMap) {
        super(typeMap, defaultTypeCodeMap);
    }
    @AssistedInject
    protected RedshiftDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createRedshiftTypeMap(rootTermType, typeFactory), createRedshiftCodeMap(), createGenericAbstractTypeMap(rootTermType, typeFactory));
    }

    private static ImmutableList<GenericDBTermType> createGenericAbstractTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        /* The type `SUPER` can be used for all sorts of nested data structures in Redshift, but currently,
         * we only use it for arrays, so it is marked as an array here.
         */
        GenericDBTermType abstractArrayType = new ArrayDBTermType(SUPER_STR, rootAncestry, s -> {
            if(s.equals(SUPER_STR))
                return Optional.of(typeFactory.getDBTypeFactory().getDBStringType());
            return Optional.empty();
        });

        List<GenericDBTermType> list = new ArrayList<>();
        list.add(abstractArrayType);
        return ImmutableList.copyOf(list);
    }

    protected static Map<String, DBTermType> createRedshiftTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();

        StringDBTermType bpCharType = new StringDBTermType(BPCHAR_STR, rootAncestry, xsdString);

        // TODO: shall we map it to xsd.datetimeStamp ? (would not follow strictly R2RML but be more precise)
        DatetimeDBTermType timestampTz = new DatetimeDBTermType(TIMESTAMPTZ_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        DBTermType timeTzType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(TIMETZ_STR, rootAncestry,
                typeFactory.getDatatype(XSD.TIME), NOTHING);

        DBTermType dateType = new DateDBTermType(DATE_STR, rootAncestry,
                typeFactory.getDatatype(XSD.DATE));

        NumberDBTermType defaultDecimalType = new NumberDBTermType(DEFAULT_DECIMAL_STR, rootAncestry,
                typeFactory.getXsdDecimalDatatype(), DECIMAL);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(INT2_STR, map.get(SMALLINT_STR));
        map.put(INT4_STR, map.get(INTEGER_STR));
        map.put(INT8_STR, map.get(BIGINT_STR));
        map.put(FLOAT4_STR, map.get(REAL_STR));
        map.put(FLOAT8_STR, map.get(DOUBLE_PREC_STR));
        map.put(DEFAULT_DECIMAL_STR, defaultDecimalType);
        map.put(BPCHAR_STR, bpCharType);
        map.put(TIMESTAMPTZ_STR, timestampTz);
        map.put(TIMETZ_STR, timeTzType);
        map.put(DATE_STR, dateType);
        map.put(BOOL_STR, map.get(BOOLEAN_STR));

        /*
         * POSTGIS types
         *
         */
        map.put(GEOMETRY_STR, new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOMETRY_STR, rootAncestry, xsdString));
        map.put(GEOGRAPHY_STR, new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOGRAPHY_STR, rootAncestry, xsdString));

        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createRedshiftCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_PREC_STR);
        //Redshift does not support some functions to have inputs of type TIMESTAMP WITH TIME ZONE, so we
        //change the default type for TIMESTAMPS to TIMESTAMP (without time zone)
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMP_STR);
        map.put(DefaultTypeCode.DECIMAL, DEFAULT_DECIMAL_STR);
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
        return false;
    }

    @Override
    public boolean supportsJson() {
        return false;
    }

    @Override
    public boolean supportsArrayType() {
        return false;
    }


    @Override
    public String getDBTrueLexicalValue() {
        return "true";
    }

    @Override
    public String getDBFalseLexicalValue() { return "false"; }

}
