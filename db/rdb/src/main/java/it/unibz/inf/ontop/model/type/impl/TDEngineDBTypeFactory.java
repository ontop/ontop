package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.*;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.SAME_TYPE_NO_CONSTANT;

public class TDEngineDBTypeFactory extends DefaultSQLDBTypeFactory {
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String BYTEA_STR = "BYTEA";
    private static final String DEFAULT_DECIMAL_STR = "DECIMAL(38, 18)";
    public static final String TEXT_STR = "TEXT";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String BOOL_STR = "BOOL";


    protected TDEngineDBTypeFactory(Map<String, DBTermType> typeMap, ImmutableMap<DefaultTypeCode, String> defaultTypeCodeMap) {
        super(typeMap, defaultTypeCodeMap);
    }
    @AssistedInject
    protected TDEngineDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createTDengineTypeMap(rootTermType, typeFactory), createTDEngineCodeMap());
    }

    protected static Map<String, DBTermType> createTDengineTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);

        DatetimeDBTermType timestamp = new DatetimeDBTermType(TIMESTAMP, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype(), false);

        DBTermType byteAType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(BYTEA_STR, rootAncestry,
                typeFactory.getDatatype(XSD.HEXBINARY), SAME_TYPE_NO_CONSTANT);

        NumberDBTermType defaultDecimalType = new NumberDBTermType(DEFAULT_DECIMAL_STR, rootAncestry,
                typeFactory.getXsdDecimalDatatype(), DECIMAL);

        NumberDBTermType defaultDoubleType = new NumberDBTermType(DOUBLE_PREC_STR, rootAncestry,
                typeFactory.getXsdDoubleDatatype(), FLOAT_DOUBLE);

        BooleanDBTermType defaultBooleanType = new BooleanDBTermType(BOOL_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

        DBTermType textType = new StringDBTermType(TEXT_STR, "VARCHAR(500)", rootAncestry, typeFactory.getXsdStringDatatype());

        map.put(BYTEA_STR, byteAType);
        map.put(DEFAULT_DECIMAL_STR, defaultDecimalType);
        map.put(TEXT_STR, textType);
        map.put(TIMESTAMP, timestamp);
        map.put(DOUBLE_PREC_STR, defaultDoubleType);
        map.put(BOOL_STR, defaultBooleanType);

        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createTDEngineCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.HEXBINARY, BYTEA_STR);
        map.put(DefaultTypeCode.STRING, TEXT_STR);
        map.put(DefaultTypeCode.DECIMAL, DEFAULT_DECIMAL_STR);

        map.put(DefaultTypeCode.BOOLEAN, BOOLEAN);
        map.put(DefaultTypeCode.DATETIMESTAMP, TIMESTAMP);

        return ImmutableMap.copyOf(map);
    }

    @Override
    public boolean supportsDBGeometryType() {
        return true;
    }

}
