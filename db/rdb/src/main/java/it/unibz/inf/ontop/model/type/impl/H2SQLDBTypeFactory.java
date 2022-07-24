package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Map;
import java.util.Optional;

public class H2SQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String GEOMETRY_STR = "GEOMETRY";


    @AssistedInject
    private H2SQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createH2SQLTypeMap(rootTermType, typeFactory), createH2SQLCodeMap());
    }

    private static Map<String, DBTermType> createH2SQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);

        /*
         * H2GIS types
         */
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        map.put(GEOMETRY_STR, new NonStringNonNumberNonBooleanNonDatetimeDBTermType(GEOMETRY_STR, rootAncestry, xsdString));

        return map;
    }

    private static ImmutableMap<DefaultTypeCode, String> createH2SQLCodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();

        /*
         * H2GIS types
         */
        map.put(DefaultTypeCode.GEOMETRY, GEOMETRY_STR);

        return ImmutableMap.copyOf(map);
    }

    @Override
    public Optional<String> getDBNaNLexicalValue() {
        return Optional.empty();
    }

    @Override
    public boolean supportsDBGeometryType() {
        return true;
    }
}
