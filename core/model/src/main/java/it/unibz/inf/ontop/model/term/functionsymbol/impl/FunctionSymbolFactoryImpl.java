package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FunctionSymbolFactoryImpl implements FunctionSymbolFactory {

    private final TypeFactory typeFactory;
    private final RDFTermFunctionSymbol rdfTermFunctionSymbol;
    private final BooleanFunctionSymbol areCompatibleRDFStringFunctionSymbol;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
    private final ImmutableTable<String, Integer, SPARQLFunctionSymbol> regularSparqlFunctionTable;
    private final Map<Integer, FunctionSymbol> commonDenominatorMap;
    private final Map<Integer, SPARQLFunctionSymbol> concatMap;
    private final Map<RDFTermType, BooleanFunctionSymbol> isAMap;
    private final BooleanFunctionSymbol rdf2DBBooleanFunctionSymbol;
    private final FunctionSymbol langTypeFunctionSymbol;
    private final FunctionSymbol rdfDatatypeFunctionSymbol;
    private final BooleanFunctionSymbol lexicalLangMatchesFunctionSymbol;

    private final MetaRDFTermType metaRDFType;
    private final DBTermType dbBooleanType;

    @Inject
    private FunctionSymbolFactoryImpl(TypeFactory typeFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory) {
        this.typeFactory = typeFactory;
        this.rdfTermFunctionSymbol = new RDFTermFunctionSymbolImpl(
                typeFactory.getDBTypeFactory().getDBStringType(),
                typeFactory.getMetaRDFTermType());
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        DBTermType dbStringType = typeFactory.getDBTypeFactory().getDBStringType();

        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.metaRDFType = typeFactory.getMetaRDFTermType();

        this.regularSparqlFunctionTable = createSPARQLFunctionSymbolTable(typeFactory);
        this.commonDenominatorMap = new HashMap<>();
        this.concatMap = new HashMap<>();
        this.isAMap = new HashMap<>();
        this.areCompatibleRDFStringFunctionSymbol = new AreCompatibleRDFStringFunctionSymbolImpl(metaRDFType, dbBooleanType);
        rdf2DBBooleanFunctionSymbol = new RDF2DBBooleanFunctionSymbolImpl(typeFactory.getXsdBooleanDatatype(),
                dbBooleanType, dbStringType);
        this.langTypeFunctionSymbol = new LangTagFunctionSymbolImpl(metaRDFType, dbStringType);
        this.rdfDatatypeFunctionSymbol = new RDFDatatypeStringFunctionSymbolImpl(metaRDFType, dbStringType);
        this.lexicalLangMatchesFunctionSymbol = new LexicalLangMatchesFunctionSymbolImpl(dbStringType, dbBooleanType);
    }

    private static ImmutableTable<String, Integer, SPARQLFunctionSymbol> createSPARQLFunctionSymbolTable(
            TypeFactory typeFactory) {
        RDFDatatype xsdString = typeFactory.getXsdStringDatatype();
        RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();
        RDFDatatype xsdInteger = typeFactory.getXsdIntegerDatatype();
        RDFDatatype rdfsLiteral = typeFactory.getAbstractRDFSLiteral();
        RDFTermType abstractRDFType = typeFactory.getAbstractRDFTermType();
        ObjectRDFType bnodeType = typeFactory.getBlankNodeType();
        ObjectRDFType iriType = typeFactory.getIRITermType();
        RDFDatatype abstractNumericType = typeFactory.getAbstractOntopNumericDatatype();

        ImmutableSet<SPARQLFunctionSymbol> functionSymbols = ImmutableSet.of(
                new UcaseSPARQLFunctionSymbolImpl(xsdString),
                new LcaseSPARQLFunctionSymbolImpl(xsdString),
                new EncodeForUriAbstractUnaryStringSPARQLFunctionSymbolImpl(xsdString),
                new StartsWithSPARQLFunctionSymbolImpl(xsdString, xsdBoolean),
                new EndsWithSPARQLFunctionSymbolImpl(xsdString, xsdBoolean),
                new ContainsSPARQLFunctionSymbolImpl(xsdString, xsdBoolean),
                new SubStr2SPARQLFunctionSymbolImpl(xsdString, xsdInteger),
                new SubStr3SPARQLFunctionSymbolImpl(xsdString, xsdInteger),
                new StrlenSPARQLFunctionSymbolImpl(xsdString, xsdInteger),
                new LangSPARQLFunctionSymbolImpl(rdfsLiteral, xsdString),
                new LangMatchesSPARQLFunctionSymbolImpl(xsdString, xsdBoolean),
                new StrSPARQLFunctionSymbolImpl(abstractRDFType, xsdString, bnodeType),
                new DatatypeSPARQLFunctionSymbolImpl(rdfsLiteral, iriType),
                new IsIRISPARQLFunctionSymbolImpl(iriType, abstractRDFType, xsdBoolean),
                new IsBlankSPARQLFunctionSymbolImpl(bnodeType, abstractRDFType, xsdBoolean),
                new IsLiteralSPARQLFunctionSymbolImpl(rdfsLiteral, abstractRDFType, xsdBoolean),
                new IsNumericSPARQLFunctionSymbolImpl(abstractNumericType, abstractRDFType, xsdBoolean),
                new UUIDSPARQLFunctionSymbolImpl(iriType),
                new StrUUIDSPARQLFunctionSymbolImpl(xsdString),
                new ReplaceSPARQLFunctionSymbolImpl(3, xsdString),
                new ReplaceSPARQLFunctionSymbolImpl(4, xsdString),
                new RegexSPARQLFunctionSymbolImpl(2, xsdString, xsdBoolean),
                new RegexSPARQLFunctionSymbolImpl(3, xsdString, xsdBoolean)
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
    public DBFunctionSymbolFactory getDBFunctionSymbolFactory() {
        return dbFunctionSymbolFactory;
    }

    @Override
    public BooleanFunctionSymbol getIsARDFTermTypeFunctionSymbol(RDFTermType rdfTermType) {
        return isAMap
                .computeIfAbsent(rdfTermType, t -> new IsARDFTermTypeFunctionSymbolImpl(metaRDFType, dbBooleanType, t));
    }

    @Override
    public BooleanFunctionSymbol getAreCompatibleRDFStringFunctionSymbol() {
        return areCompatibleRDFStringFunctionSymbol;
    }

    @Override
    public BooleanFunctionSymbol getRDF2DBBooleanFunctionSymbol() {
        return rdf2DBBooleanFunctionSymbol;
    }

    @Override
    public RDFTermTypeFunctionSymbol getRDFTermTypeFunctionSymbol(TypeConstantDictionary dictionary,
                                                                  ImmutableSet<RDFTermTypeConstant> possibleConstants) {
        ImmutableBiMap<DBConstant, RDFTermTypeConstant> conversionMap = dictionary.createConversionMap(possibleConstants);
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

    @Override
    public FunctionSymbol getRDFDatatypeStringFunctionSymbol() {
        return rdfDatatypeFunctionSymbol;
    }

    @Override
    public FunctionSymbol getLangTagFunctionSymbol() {
        return langTypeFunctionSymbol;
    }

    @Override
    public BooleanFunctionSymbol getLexicalLangMatches() {
        return lexicalLangMatchesFunctionSymbol;
    }

    protected SPARQLFunctionSymbol getRequiredSPARQLFunctionSymbol(String officialName, int arity) {
        return getSPARQLFunctionSymbol(officialName, arity)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        String.format("Not able to get the SPARQL function %s with arity %d", officialName, arity)));
    }
}
