package it.unibz.inf.ontop.spec.mapping.transformer.impl;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.DBCastFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCaster;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MappingCasterImpl implements MappingCaster {

    private final FunctionSymbolFactory functionSymbolFactory;
    private final ProvenanceMappingFactory mappingFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final UniqueTermTypeExtractor typeExtractor;
    private final TermFactory termFactory;
    private final RDF2DBTypeMapper typeMapper;
    private final DBTermType dBStringType;

    @Inject
    private MappingCasterImpl(FunctionSymbolFactory functionSymbolFactory,
                              ProvenanceMappingFactory mappingFactory,
                              TypeFactory typeFactory, IntermediateQueryFactory iqFactory,
                              SubstitutionFactory substitutionFactory, UniqueTermTypeExtractor typeExtractor,
                              TermFactory termFactory, RDF2DBTypeMapper typeMapper) {
        this.functionSymbolFactory = functionSymbolFactory;
        this.mappingFactory = mappingFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.typeExtractor = typeExtractor;
        this.termFactory = termFactory;
        this.typeMapper = typeMapper;
        this.dBStringType = typeFactory.getDBTypeFactory().getDBStringType();
    }

    @Override
    public MappingWithProvenance transform(MappingWithProvenance mapping) {
        ImmutableMap.Builder<IQ, PPMappingAssertionProvenance> newProvenanceMapBuilder = ImmutableMap.builder();

        for (Map.Entry<IQ, PPMappingAssertionProvenance> entry : mapping.getProvenanceMap().entrySet()) {
            PPMappingAssertionProvenance provenance = entry.getValue();
            IQ newIQ = transformMappingAssertion(entry.getKey(), provenance);
            newProvenanceMapBuilder.put(newIQ, provenance);
        }
        return mappingFactory.create(newProvenanceMapBuilder.build(), mapping.getMetadata());
    }

    private IQ transformMappingAssertion(IQ mappingAssertion, PPMappingAssertionProvenance provenance) {
        ImmutableSubstitution<ImmutableTerm> topSubstitution = Optional.of(mappingAssertion.getTree())
                .filter(t -> t.getRootNode() instanceof ConstructionNode)
                .map(IQTree::getRootNode)
                .map(n -> (ConstructionNode) n)
                .map(ConstructionNode::getSubstitution)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "The mapping assertion was expecting to start with a construction node\n" + mappingAssertion));

        ImmutableSet<Variable> projectedVariables = mappingAssertion.getTree().getVariables();

        RDFTermFunctionSymbol rdfTermFunctionSymbol = functionSymbolFactory.getRDFTermFunctionSymbol();

        if (!projectedVariables.stream()
                .map(topSubstitution::apply)
                .allMatch(t -> (t instanceof ImmutableFunctionalTerm) &&
                        ((ImmutableFunctionalTerm) t).getFunctionSymbol().equals(rdfTermFunctionSymbol))) {
            throw new MinorOntopInternalBugException(
                    "The root construction node is not defining all the variables with a functional term\n"
                            + mappingAssertion);
        }
        IQTree childTree = ((UnaryIQTree)mappingAssertion.getTree()).getChild();

        ImmutableSubstitution<ImmutableTerm> newSubstitution = transformTopSubstitution(
                (ImmutableMap<Variable, ImmutableFunctionalTerm>)(ImmutableMap<Variable, ?>)topSubstitution.getImmutableMap(),
                childTree);

        ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables, newSubstitution);

        return iqFactory.createIQ(mappingAssertion.getProjectionAtom(),
                iqFactory.createUnaryIQTree(newRootNode, childTree));
    }

    /**
     * TODO: explain why all the transformation is done in the top construction node substitution
     */
    private ImmutableSubstitution<ImmutableTerm> transformTopSubstitution(
            ImmutableMap<Variable, ImmutableFunctionalTerm> substitutionMap,
            IQTree childTree) {

        return substitutionFactory.getSubstitution(
                substitutionMap.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> (ImmutableTerm) transformDefinition(e.getValue(), childTree)
                        )));
    }

    private ImmutableFunctionalTerm transformDefinition(ImmutableFunctionalTerm rdfTermDefinition, IQTree childTree) {
        ImmutableTerm uncastLexicalTerm = uncast(rdfTermDefinition.getTerm(0));
        ImmutableTerm rdfTypeTerm = rdfTermDefinition.getTerm(1);

        Optional<DBTermType> dbType = extractLexicalDBType(uncastLexicalTerm, childTree);
        RDFTermType rdfType = extractRDFTermType(rdfTypeTerm);

        ImmutableTerm newLexicalTerm = transformLexicalTerm(uncastLexicalTerm, dbType, rdfType);

        return termFactory.getRDFFunctionalTerm(newLexicalTerm, rdfTypeTerm);
    }

    private Optional<DBTermType> extractLexicalDBType(ImmutableTerm uncastLexicalTerm, IQTree childTree) {
        Optional<TermType> type = typeExtractor.extractUniqueTermType(uncastLexicalTerm, childTree);
        if (type
                .filter(t -> !(t instanceof DBTermType))
                .isPresent()) {
            throw new MinorOntopInternalBugException("Was expecting to get a DBTermType, not a "
                    + type.get().getClass() + " (" + type.get() + ")");
        }
        return type
                .map(t -> (DBTermType)t);
    }

    private RDFTermType extractRDFTermType(ImmutableTerm rdfTypeTerm) {
        if (rdfTypeTerm instanceof RDFTermTypeConstant) {
            return ((RDFTermTypeConstant) rdfTypeTerm).getRDFTermType();
        }
        throw new MinorOntopInternalBugException("Was expecting a RDFTermTypeConstant in the RDF term function, " +
                    "not " + rdfTypeTerm);
    }

    private ImmutableTerm transformLexicalTerm(ImmutableTerm uncastLexicalTerm, Optional<DBTermType> dbType,
                                               RDFTermType rdfType) {
        Optional<DBTermType> naturalType = typeMapper.getNaturalNonStringDBType(rdfType);

        ImmutableTerm naturalizedTerm = naturalType
                .map(n -> castTerm(dbType, n, uncastLexicalTerm))
                .orElse(uncastLexicalTerm);

        return castTerm(naturalType, dBStringType, naturalizedTerm);
    }

    private ImmutableTerm castTerm(Optional<DBTermType> inputType, DBTermType targetType, ImmutableTerm term) {
        return Optional.of(targetType)
                .filter(t1 -> !inputType.filter(t1::equals).isPresent())
                .map(n -> inputType
                        .map(i -> termFactory.getDBCastFunctionalTerm(i, n, term))
                        .orElseGet(() -> termFactory.getDBCastFunctionalTerm(n, term)))
                .map(t -> (ImmutableTerm) t)
                .orElse(term);
    }


    /**
     * Uncast the term only if it is temporally cast
     */
    private ImmutableTerm uncast(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof DBCastFunctionSymbol)
                && (((DBCastFunctionSymbol) ((ImmutableFunctionalTerm) term).getFunctionSymbol()).isTemporary())
                ? ((ImmutableFunctionalTerm) term).getTerm(0)
                : term;
    }


}
