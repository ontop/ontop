package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;


public class IndexInFunctionSymbol extends UnaryDBFunctionSymbolWithSerializerImpl {

    protected IndexInFunctionSymbol(DBTermType rootDBType, DBTermType dbIntegerType, DBFunctionSymbolSerializer serializer) {
        super("DB_INDEXIN", rootDBType, dbIntegerType, true, serializer);
    }
}
