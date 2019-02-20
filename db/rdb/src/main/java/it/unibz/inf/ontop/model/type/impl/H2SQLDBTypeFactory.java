package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Map;
import java.util.Optional;

public class H2SQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    @AssistedInject
    private H2SQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createH2SQLTypeMap(rootTermType, typeFactory), createH2SQLCodeMap());
    }

    private static Map<String, DBTermType> createH2SQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createH2SQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        return ImmutableMap.copyOf(map);
    }

    @Override
    public Optional<String> getDBNaNLexicalValue() {
        return Optional.empty();
    }
}
