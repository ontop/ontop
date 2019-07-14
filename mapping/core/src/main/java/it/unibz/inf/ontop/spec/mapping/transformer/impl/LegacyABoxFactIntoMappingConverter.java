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
    public Mapping convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled) {

        ImmutableMultimap<IRI, IQ> classes = ontology.getClassAssertions().stream()
                .collect(ImmutableCollectors.toMultimap(
                        ca -> ca.getConcept().getIRI(),
                        ca -> createFact(
                            ca.getIndividual(),
                            getIRI(RDF.TYPE),
                            getIRI(ca.getConcept().getIRI()))));

        ImmutableMultimap<IRI, IQ> properties = Stream.concat(Stream.concat(
                ontology.getObjectPropertyAssertions().stream()
                        .map(pa -> createFact(
                                pa.getSubject(),
                                getIRI(pa.getProperty().getIRI()),
                                pa.getObject())),

                ontology.getDataPropertyAssertions().stream()
                        .map(da -> createFact(
                                da.getSubject(),
                                getIRI(da.getProperty().getIRI()),
                                da.getValue()))),

                isOntologyAnnotationQueryingEnabled
                        ? ontology.getAnnotationAssertions().stream()
                            .map(aa -> createFact(
                                aa.getSubject(),
                                getIRI(aa.getProperty().getIRI()),
                                aa.getValue()))
                        : Stream.of())
                .collect(ImmutableCollectors.toMultimap(iq -> MappingTools.extractRDFPredicate(iq).getIri(), iq -> iq));

        LOGGER.debug("Appended {} object property assertions as fact rules", ontology.getObjectPropertyAssertions().size());
        LOGGER.debug("Appended {} data property assertions as fact rules", ontology.getDataPropertyAssertions().size());
        LOGGER.debug("Appended {} annotation assertions as fact rules", ontology.getAnnotationAssertions().size());
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", ontology.getClassAssertions().size());

        Mapping a = mappingFactory.createMapping(
                mappingFactory.createMetadata(
                        //TODO: parse the ontology prefixes ??
                        mappingFactory.createPrefixManager(ImmutableMap.of())),
                getTableRepresentation(properties),
                getTableRepresentation(classes));

        return a;
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> getTableRepresentation(ImmutableMultimap<IRI, IQ> index) {

        return index.asMap().entrySet().stream()
                .map(e -> Tables.immutableCell(
                        (RDFAtomPredicate) projectionAtom.getPredicate(),
                        e.getKey(),
                        queryMerger.mergeDefinitions(e.getValue()).get().normalizeForOptimization()))
                .collect(ImmutableCollectors.toTable());
    }


    private IQ createFact(ImmutableTerm subject, ImmutableTerm property, ImmutableTerm object) {

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(
                projectionAtom.getVariables(), substitutionFactory.getSubstitution(
                        projectionAtom.getTerm(0), subject,
                        projectionAtom.getTerm(1), property,
                        projectionAtom.getTerm(2), object));

        IQTree constructionTree = iqFactory.createUnaryIQTree(topConstructionNode, iqFactory.createTrueNode());
        return iqFactory.createIQ(projectionAtom, constructionTree);
    }


    // BNODES are not supported here

    private ImmutableTerm getIRI(IRI iri) {
        return termFactory.getConstantIRI(iri);
    }


}
