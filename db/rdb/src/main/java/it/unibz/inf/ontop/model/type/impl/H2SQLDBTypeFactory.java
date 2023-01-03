package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.DatabaseInfoSupplier;
import it.unibz.inf.ontop.model.type.*;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;

public class H2SQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    protected static final String GEOMETRY_STR = "GEOMETRY";
    private static final String DEFAULT_DECIMAL_STR = "DECIMAL(20, 6)";
    private final DatabaseInfoSupplier databaseInfoSupplier;


    @AssistedInject
    private H2SQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory,
                               DatabaseInfoSupplier databaseInfoSupplier) {
        super(createH2SQLTypeMap(rootTermType, typeFactory), createH2SQLCodeMap());
        this.databaseInfoSupplier = databaseInfoSupplier;
    }

    private static Map<String, DBTermType> createH2SQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        DBTermType rootDBType = new NonStringNonNumberNonBooleanNonDatetimeDBTermType(ABSTRACT_DB_TYPE_STR,
                rootTermType.getAncestry(), true);
        TermTypeAncestry rootAncestry = rootDBType.getAncestry();
        RDFDatatype xsdDecimal = typeFactory.getXsdDecimalDatatype();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();

        map.put(DECIMAL_STR, new NumberDBTermType(DECIMAL_STR, DEFAULT_DECIMAL_STR, rootAncestry, xsdDecimal, DECIMAL));

        // Overloads CHARACTER LARGE OBJECT to ensure numeric to text conversion is possible
        // Necessary for H2 v2+. Workaround suggested in https://github.com/h2database/h2database/issues/3444
        StringDBTermType textType = new StringDBTermType(TEXT_STR, "CHARACTER VARYING(1000000)", rootAncestry,
                typeFactory.getXsdStringDatatype());
        map.put(TEXT_STR, textType);

        /*
         * H2GIS types
         */
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

    @Override
    public boolean supportsDBDistanceSphere() { return true; }
}
