package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.model.type.DBTermType.Category.DECIMAL;

public class DremioSQLDBTypeFactory extends DefaultSQLDBTypeFactory {

    private static final String ARRAY_STR = "LIST";

    @AssistedInject
    protected DremioSQLDBTypeFactory(@Assisted TermType rootTermType, @Assisted TypeFactory typeFactory) {
        super(createDremioSQLTypeMap(rootTermType, typeFactory), createDremioSQLCodeMap(), createGenericAbstractTypeMap(rootTermType, typeFactory));
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

    private static ImmutableList<GenericDBTermType> createGenericAbstractTypeMap(TermType rootTermType, TypeFactory typeFactory) {
        TermTypeAncestry rootAncestry = rootTermType.getAncestry();

        GenericDBTermType abstractArrayType = new ArrayDBTermType(ARRAY_STR, rootAncestry, s -> {
            if(s.equals(ARRAY_STR))
                return Optional.of(typeFactory.getDBTypeFactory().getDBStringType());
            return Optional.empty();
        });

        List<GenericDBTermType> list = new ArrayList<>();
        list.add(abstractArrayType);
        return ImmutableList.copyOf(list);
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
