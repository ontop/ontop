package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.HashMap;
import java.util.Map;

public class FunctionSymbolFactoryImpl implements FunctionSymbolFactory {

    private final TypeFactory typeFactory;
    private final FunctionSymbol rdfTermFunction;
    private final Map<String, IRIStringTemplateFunctionSymbol> iriTemplateMap;

    @Inject
    private FunctionSymbolFactoryImpl(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.rdfTermFunction = new RDFTermFunctionImpl(
                typeFactory.getAbstractRDFTermType(),
                typeFactory.getMetaRDFTermType());
        this.iriTemplateMap = new HashMap<>();
    }


    @Override
    public FunctionSymbol getRDFTermFunctionSymbol() {
        return rdfTermFunction;
    }

    @Override
    public IRIStringTemplateFunctionSymbol getIRIStringTemplateFunctionSymbol(String iriTemplate) {
        return iriTemplateMap
                .computeIfAbsent(iriTemplate,
                        t -> IRIStringTemplateFunctionSymbolImpl.createFunctionSymbol(t, typeFactory));
    }
}
