package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.FLOAT_DOUBLE;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.NOTHING;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.WITH_ALL;

public class DenodoDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String VARBIT_STR = "VARBIT";
    protected static final String BIT_STR = "BIT";
//    protected static final String INTEGER = "INTEGER";
    protected static final String FLOAT4_STR = "FLOAT4";
    protected static final String FLOAT8_STR = "FLOAT8";
    public static final String SERIAL_STR = "SERIAL";
    protected static final String BIGSERIAL_STR = "BIGSERIAL";
    protected static final String BPCHAR_STR = "BPCHAR";
    protected static final String NAME_STR = "NAME";
    public static final String TIMESTAMPTZ_STR = "TIMESTAMP_WITH_TIMEZONE";
    public static final String TIMETZ_STR = "TIMETZ";
    public static final String BOOL_STR = "BOOL";
    public static final String UUID_STR = "UUID";

    @AssistedInject
    protected DenodoDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createDenodoTypeMap(rootTermType, typeFactory), createDenodoCodeMap());
    }

    protected static Map<String, DBTermType> createDenodoTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFDatatype xsdDouble = typeFactory.getXsdDoubleDatatype();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();

        // TODO: treat it as a proper binary type
        BooleanDBTermType bitType = new BooleanDBTermType(BIT_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

        // TODO: treat it as a proper binary type
        BooleanDBTermType varBitType = new BooleanDBTermType(VARBIT_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

//        NumberDBTermType integerType = new NumberDBTermType(INTEGER, rootAncestry, xsdInteger, DBTermType.Category.INTEGER);

        NumberDBTermType serialType = new NumberDBTermType(SERIAL_STR, rootAncestry, xsdInteger, DBTermType.Category.INTEGER);
        NumberDBTermType bigSerialType = new NumberDBTermType(BIGSERIAL_STR, rootAncestry, xsdInteger, DBTermType.Category.INTEGER);

        NumberDBTermType float4Type = new NumberDBTermType(FLOAT4_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE);
        NumberDBTermType float8Type = new NumberDBTermType(FLOAT8_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE);

        StringDBTermType bpCharType = new StringDBTermType(BPCHAR_STR, rootAncestry, xsdString);
        StringDBTermType nameType = new StringDBTermType(NAME_STR, rootAncestry, xsdString);

        // TODO: shall we map it to xsd.datetimeStamp ? (would not follow strictly R2RML but be more precise)
        DatetimeDBTermType timestampTz = new DatetimeDBTermType(TIMESTAMPTZ_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        DBTermType timeTzType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(TIMETZ_STR, rootAncestry,
                typeFactory.getDatatype(XSD.TIME), NOTHING);

        // TODO: check if lexical values can be considered as unique
        DBTermType boolType = new BooleanDBTermType(BOOL_STR, rootTermType.getAncestry(), xsdBoolean);

        DBTermType uuidType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(UUID_STR, rootTermType.getAncestry(),
                WITH_ALL);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
//        map.put(BIT_STR, bitType);
//        map.put(INTEGER, integerType);
//        map.put(VARBIT_STR, varBitType);
//        map.put(FLOAT4_STR, float4Type);
//        map.put(FLOAT8_STR, float8Type);
//        map.put(SERIAL_STR, serialType);
//        map.put(BIGSERIAL_STR, bigSerialType);
//        map.put(BPCHAR_STR, bpCharType);
//        map.put(NAME_STR, nameType);
        map.put(TIMESTAMPTZ_STR, timestampTz);
//        map.put(TIMETZ_STR, timeTzType);
//        map.put(BOOL_STR, boolType);
//        map.put(UUID_STR, uuidType);
        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createDenodoCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
//        map.put(DefaultTypeCode.DOUBLE, DOUBLE_PREC_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMPTZ_STR);
        return ImmutableMap.copyOf(map);
    }
}
