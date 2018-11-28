package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class FunctionSymbolFactoryImpl implements FunctionSymbolFactory {

    private static final String BNODE_PREFIX = "_:ontop-bnode-";
    private static final String PLACEHOLDER = "{}";

    private final TypeFactory typeFactory;
    private final RDFTermFunctionSymbol rdfTermFunction;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
    private final Map<String, IRIStringTemplateFunctionSymbol> iriTemplateMap;
    private final Map<String, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap;
    private final ImmutableMap<String, SPARQLFunctionSymbol> sparqlFunctionMap;

    // NB: Multi-threading safety is NOT a concern here
    // (we don't create fresh bnode templates for a SPARQL query)
    private final AtomicInteger counter;

    @Inject
    private FunctionSymbolFactoryImpl(TypeFactory typeFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory,
                                      RDF rdfFactory) {
        this.typeFactory = typeFactory;
        this.rdfTermFunction = new RDFTermFunctionSymbolImpl(
                typeFactory.getDBTypeFactory().getDBStringType(),
                typeFactory.getMetaRDFTermType());
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
        this.iriTemplateMap = new HashMap<>();
        this.bnodeTemplateMap = new HashMap<>();
        this.counter = new AtomicInteger();
        this.sparqlFunctionMap = createSPARQLFunctionSymbolMap(rdfFactory, typeFactory);
    }

    private static ImmutableMap<String, SPARQLFunctionSymbol> createSPARQLFunctionSymbolMap(
            RDF rdfFactory, TypeFactory typeFactory) {
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();

        ImmutableSet<SPARQLFunctionSymbol> functionSymbols = ImmutableSet.of(
            new UcaseSPARQLFunctionSymbolImpl(rdfFactory, xsdString)
        );
        return functionSymbols.stream()
                .collect(ImmutableCollectors.toMap(
                        SPARQLFunctionSymbol::getOfficialName,
                        f -> f));
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
    public DBFunctionSymbolFactory getDBFunctionSymbolFactory() {
        return dbFunctionSymbolFactory;
    }

    @Override
    public SPARQLFunctionSymbol getUCase() {
        throw new RuntimeException("TODO: return ucase");
    }
}
