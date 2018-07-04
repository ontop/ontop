package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class FunctionSymbolFactoryImpl implements FunctionSymbolFactory {

    private static final String BNODE_PREFIX = "_:ontop-bnode-";
    private static final String PLACEHOLDER = "{}";

    private final TypeFactory typeFactory;
    private final RDFTermFunctionSymbol rdfTermFunction;
    private final Map<String, IRIStringTemplateFunctionSymbol> iriTemplateMap;
    private final Map<String, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap;
    private final PartiallyDefinedCastFunctionSymbol temporaryToStringCastFunctionSymbol;

    // NB: Multi-threading safety is NOT a concern here
    // (we don't create fresh bnode templates for a SPARQL query)
    private final AtomicInteger counter;

    @Inject
    private FunctionSymbolFactoryImpl(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
        this.rdfTermFunction = new RDFTermFunctionSymbolImpl(
                typeFactory.getDBTypeFactory().getDBStringType(),
                typeFactory.getMetaRDFTermType());
        this.iriTemplateMap = new HashMap<>();
        this.bnodeTemplateMap = new HashMap<>();
        this.counter = new AtomicInteger();
        // TODO: use more precise types
        this.temporaryToStringCastFunctionSymbol = new PartiallyDefinedCastFunctionSymbolImpl(
                typeFactory.getAbstractAtomicTermType(), typeFactory.getXsdStringDatatype());
    }


    @Override
    public RDFTermFunctionSymbol getRDFTermFunctionSymbol() {
        return rdfTermFunction;
    }

    @Override
    public IRIStringTemplateFunctionSymbol getIRIStringTemplateFunctionSymbol(String iriTemplate) {
        return iriTemplateMap
                .computeIfAbsent(iriTemplate,
                        t -> IRIStringTemplateFunctionSymbolImpl.createFunctionSymbol(t, typeFactory));
    }

    @Override
    public BnodeStringTemplateFunctionSymbol getBnodeStringTemplateFunctionSymbol(String bnodeTemplate) {
        return bnodeTemplateMap
                .computeIfAbsent(bnodeTemplate,
                        t -> BnodeStringTemplateFunctionSymbolImpl.createFunctionSymbol(t, typeFactory));
    }

    @Override
    public BnodeStringTemplateFunctionSymbol getFreshBnodeStringTemplateFunctionSymbol(int arity) {
       String bnodeTemplate = IntStream.range(0, arity)
               .boxed()
               .map(i -> PLACEHOLDER)
               .reduce(
                       BNODE_PREFIX + counter.incrementAndGet(),
                       (prefix, suffix) -> prefix + "/" + suffix);

       return getBnodeStringTemplateFunctionSymbol(bnodeTemplate);
    }

    @Override
    public PartiallyDefinedCastFunctionSymbol getPartiallyDefinedToStringCastFunctionSymbol() {
        return temporaryToStringCastFunctionSymbol;
    }
}
