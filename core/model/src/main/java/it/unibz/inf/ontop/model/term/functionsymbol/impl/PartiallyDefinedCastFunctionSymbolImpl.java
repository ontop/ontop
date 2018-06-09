package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.PartiallyDefinedCastFunctionSymbol;
import it.unibz.inf.ontop.model.type.TermType;

public class PartiallyDefinedCastFunctionSymbolImpl extends CastFunctionSymbolImpl implements PartiallyDefinedCastFunctionSymbol {

    protected PartiallyDefinedCastFunctionSymbolImpl(TermType inputBaseType, TermType targetType) {
        super(targetType.toString()+ "-cast", inputBaseType, targetType);
    }
}
