package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.term.functionsymbol.db.FlattenFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

public class FlattenArrayFunctionSymbol extends UnaryDBFunctionSymbolWithSerializerImpl implements FlattenFunctionSymbol {

    private final boolean isStrict;

    protected FlattenArrayFunctionSymbol(DBTermType rootDBType, DBTermType targetType, DBFunctionSymbolSerializer serializer, boolean isStrict) {
        super(computeName(isStrict, targetType), rootDBType, targetType, true, serializer);
        this.isStrict = isStrict;
    }

    private static String computeName(boolean isStrict, DBTermType targetType) {
        return String.format(
                "DB_%sFLATTENARRAY_to%s",
                (isStrict?"STRICT":""),
                targetType.toString()
        );
    }

    @Override
    public boolean isStrict() {
        return isStrict;
    }
}
