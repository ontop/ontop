package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {


    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    private final DistinctVariableOnlyDataAtom tripleAtom;
    private final DistinctVariableOnlyDataAtom quadAtom;
    private final RDFAtomPredicate tripleAtomPredicate;
    private final RDFAtomPredicate quadAtomPredicate;

    @Inject
    public LegacyABoxFactIntoMappingConverter(CoreSingletons coreSingletons) {

        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();

        VariableGenerator projectedVariableGenerator = coreSingletons
                .getCoreUtilsFactory().createVariableGenerator(ImmutableSet.of());
        tripleAtom = coreSingletons.getAtomFactory().getDistinctTripleAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());

        quadAtom = coreSingletons.getAtomFactory().getDistinctQuadAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());

        tripleAtomPredicate = (RDFAtomPredicate) tripleAtom.getPredicate();
        quadAtomPredicate = (RDFAtomPredicate) quadAtom.getPredicate();
    }

    @Override
    public ImmutableList<MappingAssertion> convert(ImmutableSet<RDFFact> facts, boolean isOntologyAnnotationQueryingEnabled) {

        ImmutableList<MappingAssertion> assertions = facts.stream()
                .map(fact -> new MappingAssertion(
                                fact.isClassAssertion()
                                        ? MappingAssertionIndex.ofClass(selectAtomPredicate(fact),
                                            Optional.of(fact.getClassOrProperty())
                                                    .filter(c -> c instanceof IRIConstant)
                                                    .map(c -> ((IRIConstant) c).getIRI())
                                                    .orElseThrow(() -> new RuntimeException(
                                                            "TODO: support bnode for classes as mapping assertion index")))
                                        : MappingAssertionIndex.ofProperty(selectAtomPredicate(fact), fact.getProperty().getIRI()),
                                    createIQ(fact),
                                    new ABoxFactProvenance(fact)))
                .collect(ImmutableCollectors.toList());

        LOGGER.debug("Appended {} assertions as fact rules", facts.size());

        return assertions;
    }

    private RDFAtomPredicate selectAtomPredicate(RDFFact fact) {
        return fact.getGraph()
                .map(g -> quadAtomPredicate)
                .orElse(tripleAtomPredicate);
    }

    private IQ createIQ(RDFFact rdfFact) {
        return rdfFact.getGraph()
                .map(g -> createQuad(rdfFact, g))
                .orElseGet(() -> createTriple(rdfFact));
    }


    private IQ createTriple(RDFFact rdfFact) {
        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                tripleAtom.getVariables(), substitutionFactory.getSubstitution(
                        tripleAtom.getTerm(0), rdfFact.getSubject(),
                        tripleAtom.getTerm(1), rdfFact.getProperty(),
                        tripleAtom.getTerm(2), rdfFact.getObject()));

        IQTree constructionTree = iqFactory.createUnaryIQTree(topConstructionNode, iqFactory.createTrueNode());
        return iqFactory.createIQ(tripleAtom, constructionTree);
    }

    private IQ createQuad(RDFFact rdfFact, ObjectConstant graph) {
        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                quadAtom.getVariables(), substitutionFactory.getSubstitution(
                        quadAtom.getTerm(0), rdfFact.getSubject(),
                        quadAtom.getTerm(1), rdfFact.getProperty(),
                        quadAtom.getTerm(2), rdfFact.getObject(),
                        quadAtom.getTerm(3), graph));

        IQTree constructionTree = iqFactory.createUnaryIQTree(topConstructionNode, iqFactory.createTrueNode());
        return iqFactory.createIQ(quadAtom, constructionTree);
    }

    private class ABoxFactProvenance implements PPMappingAssertionProvenance {
        private final String provenance;

        private ABoxFactProvenance(RDFFact a) {
            provenance = a.toString();
        }

        @Override
        public String getProvenanceInfo() {
            return provenance;
        }
    }

}
