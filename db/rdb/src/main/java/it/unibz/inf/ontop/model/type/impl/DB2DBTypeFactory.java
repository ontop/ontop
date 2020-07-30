package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;

import java.util.Map;


public class DB2DBTypeFactory extends DefaultSQLDBTypeFactory {

    @AssistedInject
    protected DB2DBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createDB2TypeMap(rootTermType, typeFactory), createDB2CodeMap());
    }

    protected static Map<String, DBTermType> createDB2TypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFDatatype xsdDouble = typeFactory.getXsdDoubleDatatype();
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();

        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        return map;
    }

    protected static ImmutableMap<DefaultTypeCode, String> createDB2CodeMap() {
        Map<DefaultTypeCode, String> map = createDefaultSQLCodeMap();
        map.put(DefaultTypeCode.STRING, VARCHAR_STR);
        return ImmutableMap.copyOf(map);
    }
}
