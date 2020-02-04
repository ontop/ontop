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
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.AnnotationAssertion;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.spec.ontology.OntologyABox;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;


public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {


    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    private final DistinctVariableOnlyDataAtom projectionAtom;
    private final RDFAtomPredicate rdfAtomPredicate;

    @Inject
    public LegacyABoxFactIntoMappingConverter(CoreSingletons coreSingletons) {

        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();

        VariableGenerator projectedVariableGenerator = coreSingletons
                .getCoreUtilsFactory().createVariableGenerator(ImmutableSet.of());
        projectionAtom = coreSingletons.getAtomFactory().getDistinctTripleAtom(
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable(),
                projectedVariableGenerator.generateNewVariable());

        rdfAtomPredicate = (RDFAtomPredicate)projectionAtom.getPredicate();
    }

    @Override
    public ImmutableList<MappingAssertion> convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled) {

        ImmutableList<MappingAssertion> assertions = Stream.concat(Stream.concat(Stream.concat(
                ontology.getClassAssertions().stream()
                    .map(ca -> new MappingAssertion(
                            MappingAssertionIndex.ofClass(rdfAtomPredicate,ca.getConcept().getIRI()),
                            createFact(ca.getIndividual(), RDF.TYPE, getIRI(ca.getConcept().getIRI())),
                            new ABoxFactProvenance(ca))),

                ontology.getObjectPropertyAssertions().stream()
                        .map(pa -> new MappingAssertion(
                                    MappingAssertionIndex.ofProperty(rdfAtomPredicate, pa.getProperty().getIRI()),
                                    createFact(pa.getSubject(), pa.getProperty().getIRI(), pa.getObject()),
                                    new ABoxFactProvenance(pa)))),

                ontology.getDataPropertyAssertions().stream()
                        .map(da -> new MappingAssertion(
                                    MappingAssertionIndex.ofProperty(rdfAtomPredicate, da.getProperty().getIRI()),
                                    createFact(da.getSubject(), da.getProperty().getIRI(), da.getValue()),
                                    new ABoxFactProvenance(da)))),

                (isOntologyAnnotationQueryingEnabled ? ontology.getAnnotationAssertions().stream() : Stream.<AnnotationAssertion>of())
                        .map(aa -> new MappingAssertion(
                                    MappingAssertionIndex.ofProperty(rdfAtomPredicate, aa.getProperty().getIRI()),
                                    createFact(aa.getSubject(), aa.getProperty().getIRI(), aa.getValue()),
                                    new ABoxFactProvenance(aa))))

                .collect(ImmutableCollectors.toList());

        LOGGER.debug("Appended {} object property assertions as fact rules", ontology.getObjectPropertyAssertions().size());
        LOGGER.debug("Appended {} data property assertions as fact rules", ontology.getDataPropertyAssertions().size());
        LOGGER.debug("Appended {} annotation assertions as fact rules", ontology.getAnnotationAssertions().size());
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", ontology.getClassAssertions().size());

        return assertions;
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

    private class ABoxFactProvenance implements PPMappingAssertionProvenance {
        private final String provenance;

        private ABoxFactProvenance(Assertion a) {
            provenance = a.toString();
        }

        @Override
        public String getProvenanceInfo() {
            return provenance;
        }
    }

}
