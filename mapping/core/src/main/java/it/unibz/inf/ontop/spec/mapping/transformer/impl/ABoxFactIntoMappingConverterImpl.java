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
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
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
    private final DBTypeFactory dbTypeFactory;

    private final DistinctVariableOnlyDataAtom tripleAtom;
    private final RDFAtomPredicate rdfAtomPredicate;
    // LAZY
    private DistinctVariableOnlyDataAtom quadAtom;

    @Inject
    protected ABoxFactIntoMappingConverterImpl(TermFactory termFactory, IntermediateQueryFactory iqFactory,
                                               SubstitutionFactory substitutionFactory, AtomFactory atomFactory,
                                               CoreUtilsFactory coreUtilsFactory, FunctionSymbolFactory functionSymbolFactory,
                                               TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.atomFactory = atomFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.dbTypeFactory = typeFactory.getDBTypeFactory();

        projectedVariableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
        tripleAtom = atomFactory.getDistinctTripleAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());
        rdfAtomPredicate = (RDFAtomPredicate) tripleAtom.getPredicate();
    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableSet<RDFFact> facts, boolean isOntologyAnnotationQueryingEnabled) {
        // Group facts by class name or property name (for properties != rdf:type)
        ImmutableMap<ObjectConstant, ImmutableList<RDFFact>> dict = facts.stream()
                .collect(ImmutableCollectors.toMap(RDFFact::getClassOrProperty, ImmutableList::of,
                        (a, b) -> Stream.concat(a.stream(), b.stream()).collect(ImmutableCollectors.toList())));

        ImmutableList<MappingAssertion> assertions = dict.entrySet().stream()
                .map(entry -> new MappingAssertion(
                                entry.getValue().get(0).isClassAssertion()
                                    ? MappingAssertionIndex.ofClass(rdfAtomPredicate,
                                        Optional.of(entry.getKey())
                                            .filter(c -> c instanceof IRIConstant)
                                            .map(c -> ((IRIConstant) c).getIRI())
                                            .orElseThrow(() -> new RuntimeException(
                                                "TODO: support bnode for classes as mapping assertion index")))
                                    : MappingAssertionIndex.ofProperty(rdfAtomPredicate, entry.getValue().get(0)
                                        .getProperty().getIRI()),
                                createIQ(entry.getValue()),
                                new ABoxFactProvenance(entry.getValue())))
                .collect(ImmutableCollectors.toList());

        LOGGER.debug("Transformed {} rdfFacts into {} mappingAssertions", facts.size(), assertions.size());

        return assertions;
    }

    private IQ createIQ(ImmutableList<RDFFact> rdfFacts) {
        return rdfFacts.get(0).isClassAssertion()
                    ? rdfFacts.get(0).getGraph()
                        .map(g -> createQuad(rdfFacts, g))
                        .orElseGet(() -> createTriple(rdfFacts))
                    : rdfFacts.get(0).getGraph()
                        .map(g -> createQuadProperty(rdfFacts, g))
                        .orElseGet(() -> createTripleProperty(rdfFacts));
    }

    private IQ createTriple(ImmutableList<RDFFact> rdfFacts) {
        ValuesNode valuesNode = createDBValuesNode(rdfFacts);

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                        tripleAtom.getTerm(0),
                        termFactory.getRDFFunctionalTerm(
                                termFactory.getConversion2RDFLexical(valuesNode.getOrderedVariables().get(0),
                                        termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getSubject().getType()).getRDFTermType()),
                                termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getObject().getType())),
                        tripleAtom.getTerm(1), rdfFacts.get(0).getProperty(),
                        tripleAtom.getTerm(2), rdfFacts.get(0).getObject()));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }

    private IQ createQuad(ImmutableList<RDFFact> rdfFacts, ObjectConstant graph) {
        ValuesNode valuesNode = createDBValuesNode(rdfFacts);

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                quadAtom.getVariables(), substitutionFactory.getSubstitution(
                        quadAtom.getTerm(0),
                            termFactory.getRDFFunctionalTerm(
                                termFactory.getConversion2RDFLexical(valuesNode.getOrderedVariables().get(0),
                                        termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getSubject().getType()).getRDFTermType()),
                                termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getObject().getType())),
                        quadAtom.getTerm(1), rdfFacts.get(0).getProperty(),
                        quadAtom.getTerm(2), rdfFacts.get(0).getObject(),
                        quadAtom.getTerm(3), graph));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(quadAtom, iqTree);
    }

    private IQ createTripleProperty(ImmutableList<RDFFact> rdfFacts) {
        ValuesNode valuesNode = createDBValuesNode(rdfFacts);

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                        tripleAtom.getTerm(0),
                            termFactory.getRDFFunctionalTerm(
                                termFactory.getConversion2RDFLexical(valuesNode.getOrderedVariables().get(0),
                                        termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getSubject().getType()).getRDFTermType()),
                                termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getObject().getType())),
                        tripleAtom.getTerm(1), rdfFacts.get(0).getProperty(),
                        tripleAtom.getTerm(2),
                            termFactory.getRDFFunctionalTerm(
                                termFactory.getConversion2RDFLexical(valuesNode.getOrderedVariables().get(1),
                                        termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getSubject().getType()).getRDFTermType()),
                                termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getObject().getType()))));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(tripleAtom, iqTree);
    }

    private IQ createQuadProperty(ImmutableList<RDFFact> rdfFacts, ObjectConstant graph) {
        ValuesNode valuesNode = createDBValuesNode(rdfFacts);

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                quadAtom.getVariables(), substitutionFactory.getSubstitution(
                        quadAtom.getTerm(0),
                            termFactory.getRDFFunctionalTerm(
                                termFactory.getConversion2RDFLexical(valuesNode.getOrderedVariables().get(0),
                                        termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getSubject().getType()).getRDFTermType()),
                                termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getObject().getType())),
                        quadAtom.getTerm(1),
                            rdfFacts.get(0).getProperty(),
                        quadAtom.getTerm(2),
                            termFactory.getRDFFunctionalTerm(
                                    termFactory.getConversion2RDFLexical(valuesNode.getOrderedVariables().get(1),
                                            termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getSubject().getType()).getRDFTermType()),
                                    termFactory.getRDFTermTypeConstant(rdfFacts.get(0).getObject().getType())),
                        quadAtom.getTerm(3), graph));

        IQTree iqTree = iqFactory.createUnaryIQTree(topConstructionNode, valuesNode);
        return iqFactory.createIQ(quadAtom, iqTree);
    }

    private ValuesNode createDBValuesNode(ImmutableList<RDFFact> rdfFacts) {
        // Two cases, class assertion or not
        return rdfFacts.get(0).isClassAssertion()
                ?   iqFactory.createValuesNode(
                        ImmutableList.of(projectedVariableGenerator.generateNewVariable()),
                        rdfFacts.stream()
                            .map(rdfFact -> ImmutableList.of(
                                    (Constant) termFactory.getDBConstant(rdfFact.getSubject().getValue(),
                                            rdfFact.getSubject().getType().getClosestDBType(dbTypeFactory))))
                            .collect(ImmutableCollectors.toList()))
                :   iqFactory.createValuesNode(
                        ImmutableList.of(projectedVariableGenerator.generateNewVariable(),
                                projectedVariableGenerator.generateNewVariable()),
                        rdfFacts.stream()
                            .map(rdfFact -> ImmutableList.of(
                                    (Constant) termFactory.getDBConstant(rdfFact.getSubject().getValue(),
                                            rdfFact.getSubject().getType().getClosestDBType(dbTypeFactory)),
                                    termFactory.getDBConstant(rdfFact.getObject().getValue(),
                                            rdfFact.getObject().getType().getClosestDBType(dbTypeFactory))))
                            .collect(ImmutableCollectors.toList()));

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
