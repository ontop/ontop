package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Map;

public class H2SQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    private static final String BOOL_STR = "BOOL";

    @AssistedInject
    private H2SQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createH2SQLTypeMap(rootTermType, typeFactory), createH2SQLCodeMap());
    }

    private static Map<String, DBTermType> createH2SQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        // TODO: add the other alias BIT?
        map.put(BOOL_STR, new BooleanDBTermType(BOOL_STR, rootTermType.getAncestry(), typeFactory.getXsdBooleanDatatype()));
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createH2SQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.BOOLEAN, BOOL_STR);
        return ImmutableMap.copyOf(map);
    }
}
