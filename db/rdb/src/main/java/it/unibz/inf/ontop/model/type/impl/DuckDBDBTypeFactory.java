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

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.NOTHING;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.SAME_TYPE_NO_CONSTANT;

public class DuckDBDBTypeFactory extends DefaultSQLDBTypeFactory {
    protected static final String INT2_STR = "INT2";
    protected static final String INT4_STR = "INT4";
    protected static final String INT8_STR = "INT8";

    protected static final String HUGEINT_STR = "HUGEINT";
    protected static final String FLOAT4_STR = "FLOAT4";
    protected static final String FLOAT8_STR = "FLOAT8";
    protected static final String BPCHAR_STR = "BPCHAR";
    public static final String TIMETZ_STR = "TIME WITH TIME ZONE";
    public static final String BOOL_STR = "BOOL";
    public static final String UUID_STR = "UUID";
    public static final String JSON_STR = "JSON";
    public static final String BYTEA_STR = "BYTEA";

    public static final String STRING_STR = "STRING";

    public static final String DEFAULT_DECIMAL_STR = "DECIMAL(38, 18)";

    public static final String ARRAY_STR = "T[]";
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[[\\d ]*\\]$");


    @AssistedInject
    protected DuckDBDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createDuckDBTypeMap(rootTermType, typeFactory), createDuckDBCodeMap(), createGenericAbstractTypeMap(rootTermType, typeFactory));
    }

    private static ImmutableList<GenericDBTermType> createGenericAbstractTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        GenericDBTermType abstractArrayType = new ArrayDBTermType(ARRAY_STR, rootAncestry, s -> {
            var matcher = ARRAY_PATTERN.matcher(s);
            if(matcher.find())
                return Optional.of(typeFactory.getDBTypeFactory().getDBTermType(s.substring(0, matcher.start())));
            return Optional.empty();
        });

        List<GenericDBTermType> list = new ArrayList<>();
        list.add(abstractArrayType);
        return ImmutableList.copyOf(list);
    }

    protected static Map<String, DBTermType> createDuckDBTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();

        StringDBTermType bpCharType = new StringDBTermType(BPCHAR_STR, rootAncestry, xsdString);

        DBTermType timeTzType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(TIMETZ_STR, rootAncestry,
                typeFactory.getDatatype(XSD.TIME), NOTHING);

        DBTermType dateType = new DateDBTermType(DATE_STR, rootAncestry,
                typeFactory.getDatatype(XSD.DATE));

        DBTermType byteAType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(BYTEA_STR, rootAncestry,
                typeFactory.getDatatype(XSD.HEXBINARY), SAME_TYPE_NO_CONSTANT);

        DBTermType uuidType = new UUIDDBTermType(UUID_STR, rootTermType.getAncestry(), xsdString);

        DBTermType stringType = new StringDBTermType(STRING_STR, rootAncestry, xsdString);

        // Default decimal (otherwise, the default value of DECIMAL would be DECIMAL(19,0)
        // with 0 digits after the point). Still arbitrary.
        NumberDBTermType defaultDecimalType = new NumberDBTermType(DEFAULT_DECIMAL_STR, rootAncestry,
                typeFactory.getXsdDecimalDatatype(), DECIMAL);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(INT2_STR, map.get(SMALLINT_STR));
        map.put(INT4_STR, map.get(INTEGER_STR));
        map.put(INT8_STR, map.get(BIGINT_STR));
        map.put(FLOAT4_STR, map.get(REAL_STR));
        map.put(FLOAT8_STR, map.get(DOUBLE_PREC_STR));
        map.put(DEFAULT_DECIMAL_STR, defaultDecimalType);
        map.put(HUGEINT_STR, map.get(BIGINT_STR));
        map.put(STRING_STR, stringType);

        map.put(BPCHAR_STR, bpCharType);
        map.put(TIMETZ_STR, timeTzType);
        map.put(DATE_STR, dateType);
        map.put(BOOL_STR, map.get(BOOLEAN_STR));
        map.put(UUID_STR, uuidType);
        map.put(BYTEA_STR, byteAType);

        /*
         * JSON
         */
        map.put(JSON_STR, new JsonDBTermTypeImpl(JSON_STR, rootAncestry));

        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createDuckDBCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_PREC_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMPTZ_STR);
        map.put(DefaultTypeCode.HEXBINARY, BYTEA_STR);
        map.put(DefaultTypeCode.STRING, VARCHAR_STR);
        map.put(DefaultTypeCode.JSON, JSON_STR);
        map.put(DefaultTypeCode.DECIMAL, DEFAULT_DECIMAL_STR);

        return ImmutableMap.copyOf(map);
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
    public boolean supportsJson() {
        return true;
    }

    @Override
    public boolean supportsArrayType() {
        return false;
    }

    @Override
    public String getDBTrueLexicalValue() { return "true"; }

    @Override
    public String getDBFalseLexicalValue() { return "false"; }

}
