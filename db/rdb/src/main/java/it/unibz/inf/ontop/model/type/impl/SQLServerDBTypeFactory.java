package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Map;
import java.util.Optional;

public class SQLServerDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String BIT_STR = "BIT";
    public static final String DATETIME_STR = "DATETIME";
    public static final String DATETIME2_STR = "DATETIME2";
    public static final String DATETIMEOFFSET_STR = "DATETIMEOFFSET";


    @AssistedInject
    private SQLServerDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createSQLServerTypeMap(rootTermType, typeFactory), createSQLServerCodeMap());
    }

    private static Map<String, DBTermType> createSQLServerTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        // Non-standard (not part of the R2RML standard).
        BooleanDBTermType bitType = new BooleanDBTermType(BIT_STR, rootTermType.getAncestry(),
                typeFactory.getXsdBooleanDatatype());
        // Name for TIMESTAMP
        DatetimeDBTermType datetimeType = new DatetimeDBTermType(DATETIME_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());
        DatetimeDBTermType datetime2Type = new DatetimeDBTermType(DATETIME2_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());
        DatetimeDBTermType dateTimeOffset = new DatetimeDBTermType(DATETIMEOFFSET_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(BIT_STR, bitType);
        map.put(DATETIME_STR, datetimeType);
        map.put(DATETIME2_STR, datetime2Type);
        map.put(DATETIMEOFFSET_STR, dateTimeOffset);
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createSQLServerCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.BOOLEAN, BIT_STR);
        map.put(DefaultTypeCode.STRING, NVARCHAR_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, DATETIME_STR);
        // By default float is float(53) which is a float with double precision
        map.put(DefaultTypeCode.DOUBLE, FLOAT_STR);
        return ImmutableMap.copyOf(map);
    }

    @Override
    public Optional<String> getDBNaNLexicalValue() {
        return Optional.of("NaN");
    }

    @Override
    public String getDBTrueLexicalValue() {
        return "1";
    }

    @Override
    public String getDBFalseLexicalValue() {
        return "0";
    }
}
