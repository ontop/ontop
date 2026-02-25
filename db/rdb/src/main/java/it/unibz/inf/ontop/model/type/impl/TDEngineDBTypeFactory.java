package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.*;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.NOTHING;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.SAME_TYPE_NO_CONSTANT;

public class TDEngineDBTypeFactory extends DefaultSQLDBTypeFactory {
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String TEXT_STR = "TEXT";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String BOOL_STR = "BOOL";
    public static final String GEOMETRY_STR = "GEOMETRY";


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

        DatetimeDBTermType timestamp = new DatetimeDBTermType(TIMESTAMP, rootAncestry,
                typeFactory.getXsdDatetimeDatatype(), false);

        BooleanDBTermType defaultBooleanType = new BooleanDBTermType(BOOL_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());

        DBTermType textType = new StringDBTermType(TEXT_STR, "VARCHAR(500)", rootAncestry, typeFactory.getXsdStringDatatype());

        DBTermType geometryType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOMETRY_STR, rootAncestry,
                typeFactory.getXsdStringDatatype());

        map.put(TEXT_STR, textType);
        map.put(TIMESTAMP, timestamp);
        map.put(BOOL_STR, defaultBooleanType);
        map.put(GEOMETRY_STR, geometryType);

        map.remove(TIMESTAMP_WITH_TIME_ZONE_STR);
        map.remove(BLOB_STR);


        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createTDEngineCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.HEXBINARY, BINARY_STR);
        map.put(DefaultTypeCode.STRING, TEXT_STR);
        map.put(DefaultTypeCode.BOOLEAN, BOOLEAN);
        map.put(DefaultTypeCode.GEOMETRY, GEOMETRY_STR);

        return ImmutableMap.copyOf(map);
    }

    @Override
    public boolean supportsDBGeometryType() {
        return true;
    }

}
