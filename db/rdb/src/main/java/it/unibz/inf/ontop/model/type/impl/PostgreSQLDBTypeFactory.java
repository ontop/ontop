package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.FLOAT_DOUBLE;
import static it.unibz.inf.ontop.model.type.DBTermType.Category.INTEGER;

public class PostgreSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String VARBIT_STR = "VARBIT";
    protected static final String BIT_STR = "BIT";
    protected static final String INT2_STR = "INT2";
    protected static final String INT4_STR = "INT4";
    protected static final String INT8_STR = "INT8";
    protected static final String FLOAT4_STR = "FLOAT4";
    protected static final String FLOAT8_STR = "FLOAT8";
    protected static final String SERIAL_STR = "SERIAL";
    protected static final String BIGSERIAL_STR = "BIGSERIAL";
    protected static final String BPCHAR_STR = "BPCHAR";
    protected static final String NAME_STR = "NAME";
    protected static final String TIMESTAMPTZ_STR = "TIMESTAMPTZ";
    public static final String TIMETZ_STR = "TIMETZ";
    public static final String BOOL_STR = "BOOL";

    @AssistedInject
    protected PostgreSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createPostgreSQLTypeMap(rootTermType, typeFactory), createPostgreSQLCodeMap());
    }

    protected static Map<String, DBTermType> createPostgreSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFDatatype xsdDouble = typeFactory.getXsdDoubleDatatype();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();

        // TODO: treat it as a proper binary type
        BooleanDBTermType bitType = new BooleanDBTermType(BIT_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

        // TODO: treat it as a proper binary type
        BooleanDBTermType varBitType = new BooleanDBTermType(BIT_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

        NumberDBTermType int2Type = new NumberDBTermType(INT2_STR, rootAncestry, xsdInteger, INTEGER);
        NumberDBTermType int4Type = new NumberDBTermType(INT4_STR, rootAncestry, xsdInteger, INTEGER);
        NumberDBTermType int8Type = new NumberDBTermType(INT8_STR, rootAncestry, xsdInteger, INTEGER);

        NumberDBTermType serialType = new NumberDBTermType(SERIAL_STR, rootAncestry, xsdInteger, INTEGER);
        NumberDBTermType bigSerialType = new NumberDBTermType(BIGSERIAL_STR, rootAncestry, xsdInteger, INTEGER);

        NumberDBTermType float4Type = new NumberDBTermType(FLOAT4_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE);
        NumberDBTermType float8Type = new NumberDBTermType(FLOAT8_STR, rootTermType.getAncestry(), xsdDouble, FLOAT_DOUBLE);

        StringDBTermType bpCharType = new StringDBTermType(BPCHAR_STR, rootAncestry, xsdString);
        StringDBTermType nameType = new StringDBTermType(NAME_STR, rootAncestry, xsdString);

        // TODO: shall we map it to xsd.datetimeStamp ? (would not follow strictly R2RML but be more precise)
        DatetimeDBTermType timestampTz = new DatetimeDBTermType(TIMESTAMPTZ_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        DBTermType timeTzType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(TIMETZ_STR, rootAncestry,
                typeFactory.getDatatype(XSD.TIME));

        DBTermType boolType = new BooleanDBTermType(BOOL_STR, rootTermType.getAncestry(), xsdBoolean);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(BIT_STR, bitType);
        map.put(INT2_STR, int2Type);
        map.put(INT4_STR, int4Type);
        map.put(INT8_STR, int8Type);
        map.put(VARBIT_STR, varBitType);
        map.put(FLOAT4_STR, float4Type);
        map.put(FLOAT8_STR, float8Type);
        map.put(SERIAL_STR, serialType);
        map.put(BIGSERIAL_STR, bigSerialType);
        map.put(BPCHAR_STR, bpCharType);
        map.put(NAME_STR, nameType);
        map.put(TIMESTAMPTZ_STR, timestampTz);
        map.put(TIMETZ_STR, timeTzType);
        map.put(BOOL_STR, boolType);
        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createPostgreSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.DOUBLE, DOUBLE_PREC_STR);
        return ImmutableMap.copyOf(map);
    }
}
