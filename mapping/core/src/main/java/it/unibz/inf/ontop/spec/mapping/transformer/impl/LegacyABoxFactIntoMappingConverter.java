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
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
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
    public Mapping convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled,
                           UriTemplateMatcher uriTemplateMatcher) {

        ImmutableMultimap<IRI, IQ> classes = ontology.getClassAssertions().stream()
                .collect(ImmutableCollectors.toMultimap(
                        ca -> ca.getConcept().getIRI(),
                        ca -> createFact(
                            getObject(ca.getIndividual(), uriTemplateMatcher),
                            getIRI(RDF.TYPE),
                            getIRI(ca.getConcept().getIRI()))));

        ImmutableMultimap<IRI, IQ> properties = Stream.concat(Stream.concat(
                ontology.getObjectPropertyAssertions().stream()
                        .map(pa -> createFact(
                                getObject(pa.getSubject(), uriTemplateMatcher),
                                getIRI(pa.getProperty().getIRI()),
                                getObject(pa.getObject(), uriTemplateMatcher))),

                ontology.getDataPropertyAssertions().stream()
                        .map(da -> createFact(
                                getObject(da.getSubject(), uriTemplateMatcher),
                                getIRI(da.getProperty().getIRI()),
                                getValueConstant(da.getValue())))),

                isOntologyAnnotationQueryingEnabled
                        ? ontology.getAnnotationAssertions().stream()
                            .map(aa -> createFact(
                                getObject(aa.getSubject(), uriTemplateMatcher),
                                getIRI(aa.getProperty().getIRI()),
                                (aa.getValue() instanceof ValueConstant)
                                        ? getValueConstant((ValueConstant) aa.getValue())
                                        : getObject((ObjectConstant) aa.getValue(), uriTemplateMatcher)))
                        : Stream.of())
                .collect(ImmutableCollectors.toMultimap(iq -> MappingTools.extractRDFPredicate(iq).getIri(), iq -> iq));

        LOGGER.debug("Appended {} object property assertions as fact rules", ontology.getObjectPropertyAssertions().size());
        LOGGER.debug("Appended {} data property assertions as fact rules", ontology.getDataPropertyAssertions().size());
        LOGGER.debug("Appended {} annotation assertions as fact rules", ontology.getAnnotationAssertions().size());
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", ontology.getClassAssertions().size());

        Mapping a = mappingFactory.createMapping(
                mappingFactory.createMetadata(
                        //TODO: parse the ontology prefixes ??
                        mappingFactory.createPrefixManager(ImmutableMap.of()),
                        uriTemplateMatcher),
                getTableRepresentation(properties),
                getTableRepresentation(classes));

        return a;
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> getTableRepresentation(ImmutableMultimap<IRI, IQ> index) {

        return index.asMap().entrySet().stream()
                .map(e -> Tables.immutableCell(
                        (RDFAtomPredicate) projectionAtom.getPredicate(),
                        e.getKey(),
                        queryMerger.mergeDefinitions(e.getValue()).get().liftBinding()))
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
    private ImmutableTerm getObject(ObjectConstant o, UriTemplateMatcher uriTemplateMatcher) {
        IRIConstant iri = (IRIConstant) o;
        return uriTemplateMatcher.generateURIFunction(iri.getIRI().getIRIString());
    }

    private ImmutableTerm getIRI(IRI iri) {
        return termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(iri.getIRIString()));
    }

    private ImmutableTerm getValueConstant(ValueConstant o) {
        return o.getType().getLanguageTag()
                .map(lang ->
                        termFactory.getImmutableTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString()))
                .orElseGet(() ->
                        termFactory.getImmutableTypedTerm(o, o.getType()));
    }


}
