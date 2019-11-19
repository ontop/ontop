package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.WITH_ALL;

public class SQLServerDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String BIT_STR = "BIT";
    public static final String DATETIME_STR = "DATETIME";
    public static final String DATETIME2_STR = "DATETIME2";
    public static final String DATETIMEOFFSET_STR = "DATETIMEOFFSET";
    public static final String UNIQUEIDENTIFIER_STR = "UNIQUEIDENTIFIER";


    @AssistedInject
    private SQLServerDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createSQLServerTypeMap(rootTermType, typeFactory), createSQLServerCodeMap());
    }

    private static Map<String, DBTermType> createSQLServerTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        // Overloads NVARCHAR to insert the precision
        StringDBTermType nvarcharType = new StringDBTermType(NVARCHAR_STR, "NVARCHAR(max)", rootAncestry,
                typeFactory.getXsdStringDatatype());

        // Non-standard (not part of the R2RML standard).
        BooleanDBTermType bitType = new BooleanDBTermType(BIT_STR, rootAncestry,
                typeFactory.getXsdBooleanDatatype());
        // Name for TIMESTAMP
        DatetimeDBTermType datetimeType = new DatetimeDBTermType(DATETIME_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());
        DatetimeDBTermType datetime2Type = new DatetimeDBTermType(DATETIME2_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());
        DatetimeDBTermType dateTimeOffset = new DatetimeDBTermType(DATETIMEOFFSET_STR, rootTermType.getAncestry(),
                typeFactory.getXsdDatetimeDatatype());

        DBTermType uniqueIdType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(UNIQUEIDENTIFIER_STR,
                rootTermType.getAncestry(), WITH_ALL);

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(NVARCHAR_STR, nvarcharType);
        map.put(BIT_STR, bitType);
        map.put(DATETIME_STR, datetimeType);
        map.put(DATETIME2_STR, datetime2Type);
        map.put(DATETIMEOFFSET_STR, dateTimeOffset);
        map.put(UNIQUEIDENTIFIER_STR, uniqueIdType);
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createSQLServerCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.BOOLEAN, BIT_STR);
        map.put(DefaultTypeCode.STRING, NVARCHAR_STR);
        map.put(DefaultTypeCode.DATETIMESTAMP, DATETIME2_STR);
        // By default float is float(53) which is a float with double precision
        map.put(DefaultTypeCode.DOUBLE, FLOAT_STR);
        return ImmutableMap.copyOf(map);
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
