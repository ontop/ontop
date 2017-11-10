package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DBMetadataTestingTools;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.URIConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {

    private final Datalog2QueryMappingConverter datalog2QueryMappingConverter;
    private final SpecificationFactory mappingFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);

    @Inject
    public LegacyABoxFactIntoMappingConverter(Datalog2QueryMappingConverter datalog2QueryMappingConverter,
                                              SpecificationFactory mappingFactory) {
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
        this.mappingFactory = mappingFactory;
    }

    @Override
    public Mapping convert(Ontology ontology, ExecutorRegistry executorRegistry, boolean isOntologyAnnotationQueryingEnabled, UriTemplateMatcher uriTemplateMatcher) {

        List<AnnotationAssertion> annotationAssertions = isOntologyAnnotationQueryingEnabled ?
                ontology.getAnnotationAssertions() :
                Collections.emptyList();

        DBMetadata dummyDBMetadata = DBMetadataTestingTools.createDummyMetadata();

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
                                                  List<AnnotationAssertion> aas,
                                                  UriTemplateMatcher uriTemplateMatcher) {

        List<CQIE> mutableMapping = new ArrayList<>();

        int count = 0;
        for (ClassAssertion ca : cas) {
            // no blank nodes are supported here
            URIConstant c = (URIConstant) ca.getIndividual();
            Predicate p = ca.getConcept().getPredicate();
            Function head = TERM_FACTORY.getFunction(p,
                    uriTemplateMatcher.generateURIFunction(c.getURI()));
            CQIE rule = DATALOG_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", count);

        count = 0;
        for (ObjectPropertyAssertion pa : pas) {
            // no blank nodes are supported here
            URIConstant s = (URIConstant) pa.getSubject();
            URIConstant o = (URIConstant) pa.getObject();
            Predicate p = pa.getProperty().getPredicate();
            Function head = TERM_FACTORY.getFunction(p,
                    uriTemplateMatcher.generateURIFunction(s.getURI()),
                    uriTemplateMatcher.generateURIFunction(o.getURI()));
            CQIE rule = DATALOG_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }
        LOGGER.debug("Appended {} object property assertions as fact rules", count);


        count = 0;
        for (DataPropertyAssertion da : das) {
            // no blank nodes are supported here
            URIConstant s = (URIConstant) da.getSubject();
            ValueConstant o = da.getValue();
            Predicate p = da.getProperty().getPredicate();

            Function head;
            if (o.getLanguage() != null) {
                head = TERM_FACTORY.getFunction(p, TERM_FACTORY.getUriTemplate(
                        TERM_FACTORY.getConstantLiteral(s.getURI())),
                        TERM_FACTORY.getTypedTerm(TERM_FACTORY.getConstantLiteral(o.getValue()), o.getLanguage()));
            } else {

                head = TERM_FACTORY.getFunction(p, TERM_FACTORY.getUriTemplate(
                        TERM_FACTORY.getConstantLiteral(s.getURI())), TERM_FACTORY.getTypedTerm(o, o.getType()));
            }
            CQIE rule = DATALOG_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} data property assertions as fact rules", count);

        count = 0;
        for (AnnotationAssertion aa : aas) {
            // no blank nodes are supported here

            URIConstant s = (URIConstant) aa.getSubject();
            Constant v = aa.getValue();
            Predicate p = aa.getProperty().getPredicate();

            Function head;
            if (v instanceof ValueConstant) {

                ValueConstant o = (ValueConstant) v;

                if (o.getLanguage() != null) {
                    head = TERM_FACTORY.getFunction(p, TERM_FACTORY.getUriTemplate(
                            TERM_FACTORY.getConstantLiteral(s.getURI())),
                            TERM_FACTORY.getTypedTerm(TERM_FACTORY.getConstantLiteral(o.getValue()), o.getLanguage()));
                } else {

                    head = TERM_FACTORY.getFunction(p, TERM_FACTORY.getUriTemplate(
                            TERM_FACTORY.getConstantLiteral(s.getURI())), TERM_FACTORY.getTypedTerm(o, o.getType()));
                }
            } else {

                URIConstant o = (URIConstant) v;
                head = TERM_FACTORY.getFunction(p,
                        TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(s.getURI())),
                        TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(o.getURI())));


            }
            CQIE rule = DATALOG_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} annotation assertions as fact rules", count);
        return ImmutableList.copyOf(mutableMapping);
    }
}
