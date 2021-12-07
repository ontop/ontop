package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.Map;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;

public class DremioSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    @AssistedInject
    protected DremioSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createDremioSQLTypeMap(rootTermType, typeFactory), createDremioSQLCodeMap());
    }

    private static Map<String, DBTermType> createDremioSQLTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();
        // Overloads DECIMAL to specify a precision for casting purposes
        NumberDBTermType decimalType = new NumberDBTermType(DECIMAL_STR, "DECIMAL(60,30)", rootAncestry,
                typeFactory.getXsdDecimalDatatype(), DECIMAL);
        DBTermType dateType = new DateDBTermType(DATE_STR, rootAncestry,
                typeFactory.getDatatype(XSD.DATE));
        Map<String, DBTermType> map = createDefaultSQLTypeMap(rootTermType, typeFactory);
        map.put(DECIMAL_STR, decimalType);
        map.put(DATE_STR, dateType);
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
