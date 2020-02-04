package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Stream;


public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {


    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);

    private final SpecificationFactory mappingFactory;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final SubstitutionFactory substitutionFactory;

    private final DistinctVariableOnlyDataAtom projectionAtom;

    @Inject
    public LegacyABoxFactIntoMappingConverter(SpecificationFactory mappingFactory, AtomFactory atomFactory,
                                              TermFactory termFactory,
                                              IntermediateQueryFactory iqFactory,
                                              UnionBasedQueryMerger queryMerger,
                                              CoreUtilsFactory coreUtilsFactory,
                                              SubstitutionFactory substitutionFactory) {
        this.mappingFactory = mappingFactory;
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.queryMerger = queryMerger;
        this.substitutionFactory = substitutionFactory;

        VariableGenerator projectedVariableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
        projectionAtom = atomFactory.getDistinctTripleAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());
    }

    @Override
    public MappingInTransformation convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled) {

        ImmutableMultimap<MappingAssertionIndex, IQ> assertions = Stream.concat(
                ontology.getClassAssertions().stream()
                    .map(ca -> Maps.immutableEntry(ca.getConcept().getIRI(),
                        createFact(ca.getIndividual(), RDF.TYPE, getIRI(ca.getConcept().getIRI()))))
                    .map(e -> Maps.immutableEntry(
                        MappingAssertionIndex.ofClass((RDFAtomPredicate) e.getValue().getProjectionAtom().getPredicate(), e.getKey()),
                        e.getValue())),

        Stream.concat(Stream.concat(
                ontology.getObjectPropertyAssertions().stream()
                        .map(pa -> Maps.immutableEntry(pa.getProperty().getIRI(),
                                createFact(pa.getSubject(), pa.getProperty().getIRI(), pa.getObject()))),

                ontology.getDataPropertyAssertions().stream()
                        .map(da -> Maps.immutableEntry(da.getProperty().getIRI(),
                                createFact(da.getSubject(), da.getProperty().getIRI(), da.getValue())))),

                (isOntologyAnnotationQueryingEnabled ? ontology.getAnnotationAssertions().stream() : Stream.<AnnotationAssertion>of())
                        .map(aa -> Maps.immutableEntry(aa.getProperty().getIRI(),
                                createFact(aa.getSubject(), aa.getProperty().getIRI(), aa.getValue()))))

                .map(e -> Maps.immutableEntry(
                        MappingAssertionIndex.ofProperty((RDFAtomPredicate) e.getValue().getProjectionAtom().getPredicate(), e.getKey()),
                        e.getValue())))

                .collect(ImmutableCollectors.toMultimap());

        LOGGER.debug("Appended {} object property assertions as fact rules", ontology.getObjectPropertyAssertions().size());
        LOGGER.debug("Appended {} data property assertions as fact rules", ontology.getDataPropertyAssertions().size());
        LOGGER.debug("Appended {} annotation assertions as fact rules", ontology.getAnnotationAssertions().size());
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", ontology.getClassAssertions().size());

        return mappingFactory.createMapping(assertions.asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> queryMerger.mergeDefinitions(e.getValue()).get()
                                .normalizeForOptimization())));
    }

    private IQ createFact(ImmutableTerm subject, IRI property, ImmutableTerm object) {

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                projectionAtom.getVariables(), substitutionFactory.getSubstitution(
                        projectionAtom.getTerm(0), subject,
                        projectionAtom.getTerm(1), getIRI(property),
                        projectionAtom.getTerm(2), object));

        IQTree constructionTree = iqFactory.createUnaryIQTree(topConstructionNode, iqFactory.createTrueNode());
        return iqFactory.createIQ(projectionAtom, constructionTree);
    }


    // BNODES are not supported here

    private ImmutableTerm getIRI(IRI iri) {
        return termFactory.getConstantIRI(iri);
    }


}
