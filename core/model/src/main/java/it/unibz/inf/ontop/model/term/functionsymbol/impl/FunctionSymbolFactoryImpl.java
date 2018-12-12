package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class FunctionSymbolFactoryImpl implements FunctionSymbolFactory {

    private static final String BNODE_PREFIX = "_:ontop-bnode-";
    private static final String PLACEHOLDER = "{}";

    private final TypeFactory typeFactory;
    private final RDFTermFunctionSymbol rdfTermFunctionSymbol;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
    private final Map<String, IRIStringTemplateFunctionSymbol> iriTemplateMap;
    private final Map<String, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap;
    private final ImmutableTable<String, Integer, SPARQLFunctionSymbol> regularSparqlFunctionTable;
    private final Map<Integer, FunctionSymbol> commonDenominatorMap;
    private final Map<Integer, SPARQLFunctionSymbol> concatMap;
    private final Map<RDFTermType, BooleanFunctionSymbol> isAMap;

    private final MetaRDFTermType metaRDFType;
    private final DBTermType dbBooleanType;

    // NB: Multi-threading safety is NOT a concern here
    // (we don't create fresh bnode templates for a SPARQL query)
    private final AtomicInteger counter;

    @Inject
    private FunctionSymbolFactoryImpl(TypeFactory typeFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory) {
        this.typeFactory = typeFactory;
        this.rdfTermFunctionSymbol = new RDFTermFunctionSymbolImpl(
                typeFactory.getDBTypeFactory().getDBStringType(),
                typeFactory.getMetaRDFTermType());
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
        this.iriTemplateMap = new HashMap<>();
        this.bnodeTemplateMap = new HashMap<>();
        this.counter = new AtomicInteger();

        this.dbBooleanType = typeFactory.getDBTypeFactory().getDBBooleanType();
        this.metaRDFType = typeFactory.getMetaRDFTermType();

        this.regularSparqlFunctionTable = createSPARQLFunctionSymbolTable(typeFactory, dbFunctionSymbolFactory);
        this.commonDenominatorMap = new HashMap<>();
        this.concatMap = new HashMap<>();
        this.isAMap = new HashMap<>();
    }

    private static ImmutableTable<String, Integer, SPARQLFunctionSymbol> createSPARQLFunctionSymbolTable(
            TypeFactory typeFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory) {
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();

        ImmutableSet<SPARQLFunctionSymbol> functionSymbols = ImmutableSet.of(
            new UcaseSPARQLFunctionSymbolImpl(xsdString, dbFunctionSymbolFactory)
        );

        ImmutableTable.Builder<String, Integer, SPARQLFunctionSymbol> tableBuilder = ImmutableTable.builder();

        for(SPARQLFunctionSymbol functionSymbol : functionSymbols) {
            tableBuilder.put(functionSymbol.getOfficialName(), functionSymbol.getArity(), functionSymbol);
        }
        return tableBuilder.build();
    }


    @Override
    public RDFTermFunctionSymbol getRDFTermFunctionSymbol() {
        return rdfTermFunctionSymbol;
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
    public BooleanFunctionSymbol getIsARDFTermTypeFunctionSymbol(RDFTermType rdfTermType) {
        return isAMap
                .computeIfAbsent(rdfTermType, t -> new IsARDFTermTypeFunctionSymbolImpl(metaRDFType, dbBooleanType, t));
    }

    @Override
    public RDFTermTypeFunctionSymbol getRDFTermTypeFunctionSymbol(TypeConstantDictionary dictionary,
                                                                  ImmutableSet<RDFTermTypeConstant> possibleConstants) {
        ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap = dictionary.createConversionMap(possibleConstants);
        return new RDFTermTypeFunctionSymbolImpl(typeFactory, dictionary, conversionMap);
    }

    @Override
    public Optional<SPARQLFunctionSymbol> getSPARQLFunctionSymbol(String officialName, int arity) {
        return officialName.equals(XPathFunction.CONCAT.getIRIString())
                ? getSPARQLConcatFunctionSymbol(arity)
                : Optional.ofNullable(regularSparqlFunctionTable.get(officialName, arity));
    }

    /**
     * For smoother integration, return Optional.empty() for arity < 2
     */
    private Optional<SPARQLFunctionSymbol> getSPARQLConcatFunctionSymbol(int arity) {
        return arity < 2
                ? Optional.empty()
                : Optional.of(concatMap
                        .computeIfAbsent(arity, a -> new ConcatSPARQLFunctionSymbolImpl(a, typeFactory.getXsdStringDatatype())));
    }

    @Override
    public FunctionSymbol getCommonDenominatorFunctionSymbol(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Expected arity >= 2 for a common denominator");
        return commonDenominatorMap
                .computeIfAbsent(arity, a -> new CommonDenominatorFunctionSymbolImpl(a, typeFactory.getMetaRDFTermType()));
    }

    protected SPARQLFunctionSymbol getRequiredSPARQLFunctionSymbol(String officialName, int arity) {
        return getSPARQLFunctionSymbol(officialName, arity)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        String.format("Not able to get the SPARQL function %s with arity %d", officialName, arity)));
    }
}
