package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class FunctionSymbolFactoryImpl implements FunctionSymbolFactory {

    private final TypeFactory typeFactory;
    private final FunctionSymbol rdfTermFunction;

    @Inject
    private FunctionSymbolFactoryImpl(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.rdfTermFunction = new RDFTermFunctionImpl(
                typeFactory.getAbstractRDFTermType(),
                typeFactory.getMetaRDFTermType());
    }


    @Override
    public FunctionSymbol getRDFTermFunction() {
        return rdfTermFunction;
    }
}
