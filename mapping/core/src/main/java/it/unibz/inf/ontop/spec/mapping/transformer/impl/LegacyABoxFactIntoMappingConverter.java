package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;


public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {

    private final Datalog2QueryMappingConverter datalog2QueryMappingConverter;
    private final SpecificationFactory mappingFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);
    private final AtomFactory atomFactory;
    private final DatalogFactory datalogFactory;

    @Inject
    public LegacyABoxFactIntoMappingConverter(Datalog2QueryMappingConverter datalog2QueryMappingConverter,
                                              SpecificationFactory mappingFactory, AtomFactory atomFactory,
                                              DatalogFactory datalogFactory) {
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
        this.mappingFactory = mappingFactory;
        this.atomFactory = atomFactory;
        this.datalogFactory = datalogFactory;
    }

    @Override
    public Mapping convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled) {

        // ROMAN (22 Sep 2018): no streams - uriTemplateMatcher is mutable

        // blank nodes are NOT supported here

        ImmutableList.Builder<Function> heads = ImmutableList.builder();

        for (ClassAssertion ca : ontology.getClassAssertions()) {
            heads.add(atomFactory.getMutableTripleHeadAtom(
                    ca.getIndividual(), ca.getConcept().getIRI()));
        }
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", ontology.getClassAssertions().size());

        for (ObjectPropertyAssertion pa : ontology.getObjectPropertyAssertions()) {
            IRIConstant s = (IRIConstant) pa.getSubject();
            IRIConstant o = (IRIConstant) pa.getObject();
            IRI propertyIRI = pa.getProperty().getIRI();
            heads.add(atomFactory.getMutableTripleHeadAtom(
                    s,
                    propertyIRI,
                    o));
        }
        LOGGER.debug("Appended {} object property assertions as fact rules", ontology.getObjectPropertyAssertions().size());

        for (DataPropertyAssertion da : ontology.getDataPropertyAssertions()) {
            // no blank nodes are supported here
            IRIConstant s = (IRIConstant) da.getSubject();
            RDFLiteralConstant o = da.getValue();
            IRI propertyIRI = da.getProperty().getIRI();

            heads.add(atomFactory.getMutableTripleHeadAtom(s, propertyIRI, o));
        }
        LOGGER.debug("Appended {} data property assertions as fact rules", ontology.getDataPropertyAssertions().size());

        if (isOntologyAnnotationQueryingEnabled) {
            for (AnnotationAssertion aa : ontology.getAnnotationAssertions()) {
                IRIConstant s = (IRIConstant) aa.getSubject();
                Constant v = aa.getValue();
                IRI propertyIRI = aa.getProperty().getIRI();

                Function head = (v instanceof RDFLiteralConstant)
                        ? atomFactory.getMutableTripleHeadAtom(s, propertyIRI, (RDFLiteralConstant) v)
                        : atomFactory.getMutableTripleHeadAtom(s, propertyIRI, (IRIConstant) v);

                heads.add(head);
            }
            LOGGER.debug("Appended {} annotation assertions as fact rules", ontology.getAnnotationAssertions().size());
        }

        ImmutableList<CQIE> rules = heads.build().stream()
                .map(h -> datalogFactory.getCQIE(h, Collections.emptyList()))
                .collect(ImmutableCollectors.toList());

        return datalog2QueryMappingConverter.convertMappingRules(
                rules,
                mappingFactory.createMetadata(
                        //TODO: parse the ontology prefixes ??
                        mappingFactory.createPrefixManager(ImmutableMap.of())));
    }
}
