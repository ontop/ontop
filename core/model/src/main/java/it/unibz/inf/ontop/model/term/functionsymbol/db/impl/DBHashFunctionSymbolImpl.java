package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DBHashFunctionSymbolImpl extends UnaryDBFunctionSymbolWithSerializerImpl {

    protected DBHashFunctionSymbolImpl(String name, DBTermType rootDBType, DBTermType dbStringType,
                                       DBFunctionSymbolSerializer serializer) {
        super(name, rootDBType, dbStringType,
                // False because collision are theoretically possible
                false,
                serializer);
    }

    /**
     * The class would have to be made abstract for supporting post-processing
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
