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
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;


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

        // ROMAN (22 Sep 2018): no streams - uriTemplateMatcher is mutable

        // blank nodes are NOT supported here

        ImmutableList.Builder<Function> heads = ImmutableList.builder();

        for (ClassAssertion ca : ontology.getClassAssertions()) {
            heads.add(convertClassAssertion(
                    ((IRIConstant) ca.getIndividual()).getIRI(),
                    ca.getConcept().getIRI(), uriTemplateMatcher));
        }
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", ontology.getClassAssertions().size());

        for (ObjectPropertyAssertion pa : ontology.getObjectPropertyAssertions()) {
            heads.add(convertObjectPropertyAssertion(
                    ((IRIConstant) pa.getSubject()).getIRI(),
                    pa.getProperty().getIRI(),
                    ((IRIConstant) pa.getObject()).getIRI(), uriTemplateMatcher));
        }
        LOGGER.debug("Appended {} object property assertions as fact rules", ontology.getObjectPropertyAssertions().size());

        for (DataPropertyAssertion da : ontology.getDataPropertyAssertions()) {
            heads.add(convertDataPropertyAssertion(
                    ((IRIConstant) da.getSubject()).getIRI(),
                    da.getProperty().getIRI(),
                    da.getValue()));
        }
        LOGGER.debug("Appended {} data property assertions as fact rules", ontology.getDataPropertyAssertions().size());

        if (isOntologyAnnotationQueryingEnabled) {
            for (AnnotationAssertion aa : ontology.getAnnotationAssertions()) {
                heads.add(convertAnnotationAssertion(
                        ((IRIConstant) aa.getSubject()).getIRI(),
                        aa.getProperty().getIRI(),
                        aa.getValue()));
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
                        mappingFactory.createPrefixManager(ImmutableMap.of()),
                        uriTemplateMatcher
                ));
    }

    private Function convertClassAssertion(IRI object, IRI klass, UriTemplateMatcher uriTemplateMatcher) {
        return atomFactory.getMutableTripleHeadAtom(
                immutabilityTools.convertToMutableFunction(uriTemplateMatcher.generateURIFunction(object.getIRIString())),
                klass);

    }

    private Function convertObjectPropertyAssertion(IRI s, IRI p, IRI o, UriTemplateMatcher uriTemplateMatcher) {
        return atomFactory.getMutableTripleHeadAtom(
                immutabilityTools.convertToMutableTerm(uriTemplateMatcher.generateURIFunction(s.getIRIString())),
                p,
                immutabilityTools.convertToMutableTerm(uriTemplateMatcher.generateURIFunction(o.getIRIString())));
    }

    private Function convertDataPropertyAssertion(IRI s, IRI p, ValueConstant o) {
        Term v = o.getType().getLanguageTag()
                .map(lang ->
                        termFactory.getTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString()))
                .orElseGet(() ->
                        termFactory.getTypedTerm(o, o.getType()));

        return atomFactory.getMutableTripleHeadAtom(
                // ROMAN (22 Sep 2018) - why is there no convertToMutableTerm?
                termFactory.getUriTemplate(termFactory.getConstantLiteral(s.getIRIString())), p, v);
    }

    private Function convertAnnotationAssertion(IRI s, IRI p, Constant v) {
        Term vv;
        if (v instanceof ValueConstant) {
            ValueConstant o = (ValueConstant) v;
            vv = o.getType().getLanguageTag()
                    .map(lang ->
                            termFactory.getTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString()))
                    .orElseGet(() ->
                            termFactory.getTypedTerm(o, o.getType()));
        }
        else {
            IRIConstant o = (IRIConstant) v;
            vv = termFactory.getUriTemplate(termFactory.getConstantLiteral(o.getIRI().getIRIString()));

        }
        return atomFactory.getMutableTripleHeadAtom(
                // ROMAN (22 Sep 2018) - why is there no convertToMutableTerm?
                termFactory.getUriTemplate(termFactory.getConstantLiteral(s.getIRIString())), p, vv);
    }
}
