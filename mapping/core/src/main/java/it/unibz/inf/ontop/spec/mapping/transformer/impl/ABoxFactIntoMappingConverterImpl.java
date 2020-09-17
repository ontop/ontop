package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;


public class ABoxFactIntoMappingConverterImpl implements ABoxFactIntoMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABoxFactIntoMappingConverterImpl.class);

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final AtomFactory atomFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final VariableGenerator projectedVariableGenerator;
    private final FunctionSymbolFactory functionSymbolFactory;

    private final DistinctVariableOnlyDataAtom tripleAtom;
    private final RDFAtomPredicate rdfAtomPredicate;
    // LAZY
    private DistinctVariableOnlyDataAtom quadAtom;

    @Inject
    protected ABoxFactIntoMappingConverterImpl(TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                               SubstitutionFactory substitutionFactory, AtomFactory atomFactory,
                                               CoreUtilsFactory coreUtilsFactory, FunctionSymbolFactory functionSymbolFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.atomFactory = atomFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.functionSymbolFactory = functionSymbolFactory;

        projectedVariableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
        tripleAtom = atomFactory.getDistinctTripleAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());
        rdfAtomPredicate = (RDFAtomPredicate) tripleAtom.getPredicate();
    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableSet<RDFFact> facts, boolean isOntologyAnnotationQueryingEnabled) {

        ImmutableMap<ObjectConstant, ImmutableList<RDFFact>> dict = facts.stream()
                .filter(RDFFact::isClassAssertion)
                .collect(ImmutableCollectors.toMap(RDFFact::getClassOrProperty, ImmutableList::of,
                        (a, b) -> Stream.concat(a.stream(), b.stream()).collect(ImmutableCollectors.toList())));

        ImmutableList<MappingAssertion> assertions = dict.entrySet().stream()
                .map(entry -> new MappingAssertion(
                        MappingAssertionIndex.ofClass(rdfAtomPredicate,
                                Optional.of(entry.getKey())
                                        .filter(c -> c instanceof IRIConstant)
                                        .map(c -> ((IRIConstant) c).getIRI())
                                        .orElseThrow(() -> new RuntimeException(
                                                "TODO: support bnode for classes as mapping assertion index"))),
                                createIQ(entry.getValue()),
                                new ABoxFactProvenance(entry.getValue())))
                .collect(ImmutableCollectors.toList());

        LOGGER.debug("Appended {} assertions as fact rules", facts.size());

        return assertions;
    }

    private IQ createIQ(ImmutableList<RDFFact> rdfFacts) {
        if (rdfFacts.isEmpty())
            throw new RuntimeException("rdfFacts empty in createIQ");
        return rdfFacts.get(0).getGraph()
                .map(g -> createQuad(rdfFacts, g))
                .orElseGet(() -> createTriple(rdfFacts));
    }

    private IQ createTriple(ImmutableList<RDFFact> rdfFacts) {

        ValuesNode valuesNode = iqFactory.createValuesNode(
                ImmutableList.of(projectedVariableGenerator.generateNewVariable(), projectedVariableGenerator.generateNewVariable()),
                rdfFacts.stream()
                        .map(rdfFact -> ImmutableList.of(
                                rdfFact.getSubject(),
                                (Constant) rdfFact.getObject()
                        ))
                        .collect(ImmutableCollectors.toList()));

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                tripleAtom.getVariables(), substitutionFactory.getSubstitution( // For RDF function: Use getRDFFunctionalTerm in TermFactory
                                                                                // There is also RDFTermType.getClosestDBType()
                        tripleAtom.getTerm(0), rdfFacts.get(0).getSubject(),//valuesNode.getOrderedVariables().get(0),
                        tripleAtom.getTerm(1), rdfFacts.get(0).getProperty(),
                        tripleAtom.getTerm(2), rdfFacts.get(0).getObject()));//valuesNode.getOrderedVariables().get(1)));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }

    private IQ createQuad(ImmutableList<RDFFact> rdfFacts, ObjectConstant graph) {
        ValuesNode valuesNode = iqFactory.createValuesNode(
                ImmutableList.of(projectedVariableGenerator.generateNewVariable(), projectedVariableGenerator.generateNewVariable()),
                rdfFacts.stream()
                        .map(rdfFact -> ImmutableList.of(
                                rdfFact.getSubject(),
                                (Constant) rdfFact.getObject()
                        ))
                        .collect(ImmutableCollectors.toList()));

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                quadAtom.getVariables(), substitutionFactory.getSubstitution(
                        quadAtom.getTerm(0), rdfFacts.get(0).getSubject(),//valuesNode.getOrderedVariables().get(0),
                        quadAtom.getTerm(1), rdfFacts.get(0).getProperty(),
                        quadAtom.getTerm(2), rdfFacts.get(0).getObject(),//valuesNode.getOrderedVariables().get(1),
                        quadAtom.getTerm(3), graph));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(quadAtom, iqTree);
    }

    private static class ABoxFactProvenance implements PPMappingAssertionProvenance {
        private final String provenance;

        private ABoxFactProvenance(ImmutableList<RDFFact> rdfFacts) {
            provenance = rdfFacts.toString();
        }

        @Override
        public String getProvenanceInfo() {
            return provenance;
        }
    }

}
