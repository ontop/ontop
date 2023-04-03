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

public class TrinoDBTypeFactory extends DefaultSQLDBTypeFactory {
    protected static final String INT2_STR = "INT2";
    protected static final String INT4_STR = "INT4";
    protected static final String INT8_STR = "INT8";
    protected static final String FLOAT4_STR = "FLOAT4";
    protected static final String FLOAT8_STR = "FLOAT8";
    public static final String TIMESTAMPTZ_STR = "TIMESTAMP WITH TIME ZONE";
    public static final String TIMETZ_STR = "TIME WITH TIME ZONE";
    public static final String BOOL_STR = "BOOL";
    public static final String UUID_STR = "UUID";
    public static final String JSON_STR = "JSON";

    protected static final String GEOMETRY_STR = "GEOMETRY";

    private static final String DEFAULT_DECIMAL_STR = "DECIMAL(38, 18)";

    public static final String ARRAY_STR = "ARRAY(T)";


    @AssistedInject
    protected TrinoDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createTrinoTypeMap(rootTermType, typeFactory), createTrinoCodeMap(), createGenericAbstractTypeMap(rootTermType, typeFactory, "()"));
    }

    protected TrinoDBTypeFactory(TermType rootTermType, TypeFactory typeFactory, ImmutableList<GenericDBTermType> genericAbstractTypeMap) {
        super(createTrinoTypeMap(rootTermType, typeFactory), createTrinoCodeMap(), genericAbstractTypeMap);
    }

    protected static ImmutableList<GenericDBTermType> createGenericAbstractTypeMap(TermType rootTermType, TypeFactory typeFactory, String brackets) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        final char openingBracket = brackets.charAt(0);
        final char closingBracket = brackets.charAt(1);

        GenericDBTermType abstractArrayType = new ArrayDBTermType(ARRAY_STR, rootAncestry, s -> {
            if(s.equals("ARRAY"))
                return Optional.of(typeFactory.getDBTypeFactory().getDBStringType());
            if(!s.startsWith("ARRAY" + openingBracket) || !s.endsWith(closingBracket + "")) {
                return Optional.empty();
            }
            String contents = s.substring(6, s.length() - 1);

            int depth = 0;
            for(int i = 0; i < contents.length(); i++) {
                if(contents.charAt(i) == openingBracket)
                    depth += 1;
                else if(contents.charAt(i) == closingBracket)
                    depth -= 1;
                else if(contents.charAt(i) == ',' && depth == 0)
                    return Optional.empty();
            }
            if(depth != 0)
                return Optional.empty();

            return Optional.of(typeFactory.getDBTypeFactory().getDBTermType(contents));
        });

        List<GenericDBTermType> list = new ArrayList<>();
        list.add(abstractArrayType);
        return ImmutableList.copyOf(list);
    }

    protected static Map<String, DBTermType> createTrinoTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();

        // TODO: shall we map it to xsd.datetimeStamp ? (would not follow strictly R2RML but be more precise)
        DatetimeDBTermType timestampTz = new DatetimeDBTermType(TIMESTAMPTZ_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        DBTermType timeTzType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(TIMETZ_STR, rootAncestry,
                typeFactory.getDatatype(XSD.TIME), NOTHING);

        DBTermType dateType = new DateDBTermType(DATE_STR, rootAncestry,
                typeFactory.getDatatype(XSD.DATE));


        DBTermType uuidType = new UUIDDBTermType(UUID_STR, rootTermType.getAncestry(), xsdString);

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
        map.put(TIMESTAMPTZ_STR, timestampTz);
        map.put(TIMETZ_STR, timeTzType);
        map.put(DATE_STR, dateType);
        map.put(BOOL_STR, map.get(BOOLEAN_STR));
        map.put(UUID_STR, uuidType);
        /*
         * POSTGIS types
         *
         */
        map.put(GEOMETRY_STR, new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOMETRY_STR, rootAncestry, xsdString));

        /*
         * JSON
         */
        map.put(JSON_STR, new JsonDBTermTypeImpl(JSON_STR, rootAncestry));

        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createTrinoCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_PREC_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMPTZ_STR);
        map.put(DefaultTypeCode.HEXBINARY, VARBINARY_STR);
        map.put(DefaultTypeCode.STRING, VARCHAR_STR);
        map.put(DefaultTypeCode.GEOMETRY, GEOMETRY_STR);
        map.put(DefaultTypeCode.JSON, JSON_STR);
        map.put(DefaultTypeCode.DECIMAL, DEFAULT_DECIMAL_STR);

        return ImmutableMap.copyOf(map);
    }

    @Override
    public boolean supportsDBGeometryType() {
        return true;
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
        return true;
    }

    @Override
    public String getDBTrueLexicalValue() {
        return "true";
    }

    @Override
    public String getDBFalseLexicalValue() { return "false"; }

}
