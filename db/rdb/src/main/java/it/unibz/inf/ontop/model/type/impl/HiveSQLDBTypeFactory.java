package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.NOTHING;
import static it.unibz.inf.ontop.model.type.impl.NonStringNonNumberNonBooleanNonDatetimeDBTermType.StrictEqSupport.WITH_ALL;

public class HiveSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    /**
     * HiveQL datatypes available at : https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-JDBCDataTypes
     */

    protected static final String STRING_STR = "STRING";
    protected static final String INTERVAL_STR = "INTERVAL"; //TODO
    protected static final String ARRAY_STR = "ARRAY"; //TODO
    protected static final String STRUCT_STR = "STRUCT"; //TODO
    protected static final String MAP_STR = "MAP"; //TODO

    @AssistedInject
    protected HiveSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createHiveSQLTypeMap(rootTermType, typeFactory), createHiveSQLCodeMap());
    }

    private static Map<String, DBTermType> createHiveSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(STRING_STR, map.get(TEXT_STR));
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createHiveSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        return ImmutableMap.copyOf(map);
    }

}
