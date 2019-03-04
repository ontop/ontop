package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.*;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {

    private final Datalog2QueryMappingConverter datalog2QueryMappingConverter;
    private final SpecificationFactory mappingFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    public LegacyABoxFactIntoMappingConverter(Datalog2QueryMappingConverter datalog2QueryMappingConverter,
                                              SpecificationFactory mappingFactory, AtomFactory atomFactory,
                                              TermFactory termFactory, DatalogFactory datalogFactory,
                                              ImmutabilityTools immutabilityTools) {
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
        this.mappingFactory = mappingFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public Mapping convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled) {

        List<AnnotationAssertion> annotationAssertions = isOntologyAnnotationQueryingEnabled ?
                ontology.getAnnotationAssertions() :
                Collections.emptyList();

        // Mutable !!
//        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(Stream.empty());

        ImmutableList<CQIE> rules = convertAssertions(
                ontology.getClassAssertions(),
                ontology.getObjectPropertyAssertions(),
                ontology.getDataPropertyAssertions(),
                annotationAssertions);

        return datalog2QueryMappingConverter.convertMappingRules(
                rules,
                mappingFactory.createMetadata(
                        //TODO: parse the ontology prefixes ??
                        mappingFactory.createPrefixManager(ImmutableMap.of())));
    }

    /***
     * Adding ontology assertions (ABox) as rules (facts, head with no body).
     */
    private ImmutableList<CQIE> convertAssertions(Iterable<ClassAssertion> cas,
                                                  Iterable<ObjectPropertyAssertion> pas,
                                                  Iterable<DataPropertyAssertion> das,
                                                  Iterable<AnnotationAssertion> aas) {

        List<CQIE> mutableMapping = new ArrayList<>();

        int count = 0;
        for (ClassAssertion ca : cas) {
            // no blank nodes are supported here
            IRIConstant c = (IRIConstant) ca.getIndividual();
            IRI classIRI = ca.getConcept().getIRI();
            Function head = atomFactory.getMutableTripleHeadAtom(c, classIRI);
            CQIE rule = datalogFactory.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", count);

        count = 0;
        for (ObjectPropertyAssertion pa : pas) {
            // no blank nodes are supported here
            IRIConstant s = (IRIConstant) pa.getSubject();
            IRIConstant o = (IRIConstant) pa.getObject();
            IRI propertyIRI = pa.getProperty().getIRI();
            Function head = atomFactory.getMutableTripleHeadAtom(
                    s,
                    propertyIRI,
                    o);
            CQIE rule = datalogFactory.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} object property assertions as fact rules", count);


        count = 0;
        for (DataPropertyAssertion da : das) {
            // no blank nodes are supported here
            IRIConstant s = (IRIConstant) da.getSubject();
            RDFLiteralConstant o = da.getValue();
            IRI propertyIRI = da.getProperty().getIRI();


            Function head = atomFactory.getMutableTripleHeadAtom(s, propertyIRI, o);
            CQIE rule = datalogFactory.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} data property assertions as fact rules", count);

        count = 0;
        for (AnnotationAssertion aa : aas) {
            // no blank nodes are supported here

            IRIConstant s = (IRIConstant) aa.getSubject();
            IRI propertyIRI = aa.getProperty().getIRI();

            Function head;
            if (aa.getValue() instanceof RDFLiteralConstant) {

                RDFLiteralConstant o = (RDFLiteralConstant) aa.getValue();

                head = atomFactory.getMutableTripleHeadAtom(s, propertyIRI, o);
            } else {

                IRIConstant o = (IRIConstant) aa.getValue();
                head = atomFactory.getMutableTripleHeadAtom(s, propertyIRI, o);


            }
            CQIE rule = datalogFactory.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} annotation assertions as fact rules", count);
        return ImmutableList.copyOf(mutableMapping);
    }
}
