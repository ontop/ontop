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
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
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
    public Mapping convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled,
                           UriTemplateMatcher uriTemplateMatcher) {

        List<AnnotationAssertion> annotationAssertions = isOntologyAnnotationQueryingEnabled ?
                ontology.getAnnotationAssertions() :
                Collections.emptyList();

        ImmutableList<CQIE> rules = convertAssertions(
                ontology.getClassAssertions(),
                ontology.getObjectPropertyAssertions(),
                ontology.getDataPropertyAssertions(),
                annotationAssertions,
                uriTemplateMatcher // Mutable !!
        );

        return datalog2QueryMappingConverter.convertMappingRules(
                rules,
                mappingFactory.createMetadata(
                        //TODO: parse the ontology prefixes ??
                        mappingFactory.createPrefixManager(ImmutableMap.of()),
                        uriTemplateMatcher
                ));
    }

    private Function convertClassAssertion(IRI object, IRI klass, UriTemplateMatcher uriTemplateMatcher) {
        return atomFactory.getMutableTripleHeadAtom(
                immutabilityTools.convertToMutableFunction(
                        uriTemplateMatcher.generateURIFunction(object.getIRIString())), klass);

    }

    private Function convertObjectPropertyAssertion(IRI s, IRI p, IRI o, UriTemplateMatcher uriTemplateMatcher) {
        return atomFactory.getMutableTripleHeadAtom(
                immutabilityTools.convertToMutableTerm(uriTemplateMatcher.generateURIFunction(s.getIRIString())),
                p,
                immutabilityTools.convertToMutableTerm(uriTemplateMatcher.generateURIFunction(o.getIRIString())));
    }

    private Function convertDataPropertyAssertion(IRI s, IRI p, ValueConstant o) {
        return o.getType().getLanguageTag()
                .map(lang -> atomFactory.getMutableTripleHeadAtom(termFactory.getUriTemplate(
                        termFactory.getConstantLiteral(s.getIRIString())),
                        p,
                        termFactory.getTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString())))
                .orElseGet(() -> atomFactory.getMutableTripleHeadAtom(termFactory.getUriTemplate(
                        termFactory.getConstantLiteral(s.getIRIString())),
                        p,
                        termFactory.getTypedTerm(o, o.getType())));
    }

    private Function convertAnnotationAssertion(IRI s, IRI p, Constant v) {
        if (v instanceof ValueConstant) {
            ValueConstant o = (ValueConstant) v;
            return o.getType().getLanguageTag()
                    .map(lang -> atomFactory.getMutableTripleHeadAtom(termFactory.getUriTemplate(
                            termFactory.getConstantLiteral(s.getIRIString())),
                            p,
                            termFactory.getTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString())))
                    .orElseGet(() -> atomFactory.getMutableTripleHeadAtom(termFactory.getUriTemplate(
                            termFactory.getConstantLiteral(s.getIRIString())),
                            p,
                            termFactory.getTypedTerm(o, o.getType())));
        }
        else {
            IRIConstant o = (IRIConstant) v;
            return atomFactory.getMutableTripleHeadAtom(
                    termFactory.getUriTemplate(termFactory.getConstantLiteral(s.getIRIString())),
                    p,
                    termFactory.getUriTemplate(termFactory.getConstantLiteral(o.getIRI().getIRIString())));

        }

    }

    /***
     * Adding ontology assertions (ABox) as rules (facts, head with no body).
     *
     * (no blank nodes are supported here)
     */
    private ImmutableList<CQIE> convertAssertions(Iterable<ClassAssertion> cas,
                                                  Iterable<ObjectPropertyAssertion> pas,
                                                  Iterable<DataPropertyAssertion> das,
                                                  Iterable<AnnotationAssertion> aas,
                                                  UriTemplateMatcher uriTemplateMatcher) {

        List<CQIE> mutableMapping = new ArrayList<>();

        int count = 0;
        for (ClassAssertion ca : cas) {
            CQIE rule = datalogFactory.getCQIE(convertClassAssertion(((IRIConstant) ca.getIndividual()).getIRI(), ca.getConcept().getIRI(), uriTemplateMatcher), Collections.emptyList());
            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", count);

        count = 0;
        for (ObjectPropertyAssertion pa : pas) {
            CQIE rule = datalogFactory.getCQIE(convertObjectPropertyAssertion(
                    ((IRIConstant) pa.getSubject()).getIRI(),
                    pa.getProperty().getIRI(),
                    ((IRIConstant) pa.getObject()).getIRI(), uriTemplateMatcher), Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} object property assertions as fact rules", count);


        count = 0;
        for (DataPropertyAssertion da : das) {
            CQIE rule = datalogFactory.getCQIE(convertDataPropertyAssertion(
                    ((IRIConstant) da.getSubject()).getIRI(),
                    da.getProperty().getIRI(),
                    da.getValue()), Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} data property assertions as fact rules", count);

        count = 0;
        for (AnnotationAssertion aa : aas) {
            CQIE rule = datalogFactory.getCQIE(convertAnnotationAssertion(
                    ((IRIConstant) aa.getSubject()).getIRI(),
                    aa.getProperty().getIRI(),
                    aa.getValue()), Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} annotation assertions as fact rules", count);
        return ImmutableList.copyOf(mutableMapping);
    }
}
