package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Map;

public class DremioSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    @AssistedInject
    protected DremioSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createDremioSQLTypeMap(rootTermType, typeFactory), createDremioSQLCodeMap());
    }

    private static Map<String, DBTermType> createDremioSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createDremioSQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.STRING, VARCHAR_STR);
        return ImmutableMap.copyOf(map);
    }

    @Override
    public String getDBTrueLexicalValue() {
        return "true";
    }

    @Override
    public String getDBFalseLexicalValue() {
        return "false";
    }
}
