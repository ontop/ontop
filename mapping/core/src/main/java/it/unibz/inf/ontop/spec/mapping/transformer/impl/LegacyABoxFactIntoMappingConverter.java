package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DummyBasicDBMetadata;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
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
    private final DummyBasicDBMetadata defaultDummyDBMetadata;

    @Inject
    public LegacyABoxFactIntoMappingConverter(Datalog2QueryMappingConverter datalog2QueryMappingConverter,
                                              SpecificationFactory mappingFactory, AtomFactory atomFactory,
                                              TermFactory termFactory, DatalogFactory datalogFactory,
                                              DummyBasicDBMetadata defaultDummyDBMetadata) {
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
        this.mappingFactory = mappingFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
        this.defaultDummyDBMetadata = defaultDummyDBMetadata;
    }

    @Override
    public Mapping convert(OntologyABox ontology, ExecutorRegistry executorRegistry, boolean isOntologyAnnotationQueryingEnabled, UriTemplateMatcher uriTemplateMatcher) {

        List<AnnotationAssertion> annotationAssertions = isOntologyAnnotationQueryingEnabled ?
                ontology.getAnnotationAssertions() :
                Collections.emptyList();

        DBMetadata dummyDBMetadata = defaultDummyDBMetadata.clone();

        // Mutable !!
//        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(Stream.empty());

        ImmutableList<CQIE> rules = convertAssertions(
                ontology.getClassAssertions(),
                ontology.getObjectPropertyAssertions(),
                ontology.getDataPropertyAssertions(),
                annotationAssertions,
                uriTemplateMatcher
        );

        return datalog2QueryMappingConverter.convertMappingRules(
                rules,
                dummyDBMetadata,
                executorRegistry,
                mappingFactory.createMetadata(
                        //TODO: parse the ontology prefixes ??
                        mappingFactory.createPrefixManager(ImmutableMap.of()),
                        uriTemplateMatcher
                ));
    }

    /***
     * Adding ontology assertions (ABox) as rules (facts, head with no body).
     */
    private ImmutableList<CQIE> convertAssertions(Iterable<ClassAssertion> cas,
                                                  Iterable<ObjectPropertyAssertion> pas,
                                                  Iterable<DataPropertyAssertion> das,
                                                  Iterable<AnnotationAssertion> aas,
                                                  UriTemplateMatcher uriTemplateMatcher) {

        List<CQIE> mutableMapping = new ArrayList<>();

        int count = 0;
        for (ClassAssertion ca : cas) {
            // no blank nodes are supported here
            URIConstant c = (URIConstant) ca.getIndividual();
            Predicate p = atomFactory.getClassPredicate(ca.getConcept().getIRI());
            Function head = termFactory.getFunction(p,
                    uriTemplateMatcher.generateURIFunction(c.getURI()));
            CQIE rule = datalogFactory.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", count);

        count = 0;
        for (ObjectPropertyAssertion pa : pas) {
            // no blank nodes are supported here
            URIConstant s = (URIConstant) pa.getSubject();
            URIConstant o = (URIConstant) pa.getObject();
            AtomPredicate p = atomFactory.getObjectPropertyPredicate(pa.getProperty().getIRI());
            Function head = termFactory.getFunction(p,
                    uriTemplateMatcher.generateURIFunction(s.getURI()),
                    uriTemplateMatcher.generateURIFunction(o.getURI()));
            CQIE rule = datalogFactory.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} object property assertions as fact rules", count);


        count = 0;
        for (DataPropertyAssertion da : das) {
            // no blank nodes are supported here
            URIConstant s = (URIConstant) da.getSubject();
            ValueConstant o = da.getValue();
            Predicate p = atomFactory.getDataPropertyPredicate(da.getProperty().getIRI());


            Function head = o.getType().getLanguageTag()
                    .map(lang -> termFactory.getFunction(p, termFactory.getUriTemplate(
                            termFactory.getConstantLiteral(s.getURI())),
                            termFactory.getTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString())))
                    .orElseGet(() -> termFactory.getFunction(p, termFactory.getUriTemplate(
                            termFactory.getConstantLiteral(s.getURI())), termFactory.getTypedTerm(o, o.getType())));
            CQIE rule = datalogFactory.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} data property assertions as fact rules", count);

        count = 0;
        for (AnnotationAssertion aa : aas) {
            // no blank nodes are supported here

            URIConstant s = (URIConstant) aa.getSubject();
            Predicate p = atomFactory.getAnnotationPropertyPredicate(aa.getProperty().getIRI());

            Function head;
            if (aa.getValue() instanceof ValueConstant) {

                ValueConstant o = (ValueConstant) aa.getValue();

                head = o.getType().getLanguageTag()
                        .map(lang -> termFactory.getFunction(p, termFactory.getUriTemplate(
                                    termFactory.getConstantLiteral(s.getURI())),
                                    termFactory.getTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString())))
                        .orElseGet(() -> termFactory.getFunction(p, termFactory.getUriTemplate(
                                termFactory.getConstantLiteral(s.getURI())), termFactory.getTypedTerm(o, o.getType())));
            } else {

                URIConstant o = (URIConstant) aa.getValue();
                head = termFactory.getFunction(p,
                        termFactory.getUriTemplate(termFactory.getConstantLiteral(s.getURI())),
                        termFactory.getUriTemplate(termFactory.getConstantLiteral(o.getURI())));


            }
            CQIE rule = datalogFactory.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} annotation assertions as fact rules", count);
        return ImmutableList.copyOf(mutableMapping);
    }
}
