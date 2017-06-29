package it.unibz.inf.ontop.mapping.transf.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.mapping.transf.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.sql.DBMetadataTestingTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {

    private final Mapping2DatalogConverter mapping2DatalogConverter;
    private final Datalog2QueryMappingConverter datalog2QueryMappingConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);

    @Inject
    public LegacyABoxFactIntoMappingConverter(Mapping2DatalogConverter mapping2DatalogConverter,
                                              Datalog2QueryMappingConverter datalog2QueryMappingConverter) {
        this.mapping2DatalogConverter = mapping2DatalogConverter;
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
    }

    @Override
    public Mapping convert(Ontology ontology, ExecutorRegistry executorRegistry, MappingMetadata mappingMetadata,
                           boolean isOntologyAnnotationQueryingEnabled) {

        List<AnnotationAssertion> annotationAssertions = isOntologyAnnotationQueryingEnabled ?
                ontology.getAnnotationAssertions() :
                Collections.emptyList();

//        ImmutableList<CQIE> rules = mapping2DatalogConverter.convert(inputMapping)
//                .collect(ImmutableCollectors.toList());
        // Adding ontology assertions (ABox) as rules (facts, head with no body).
        DBMetadata dummyDBMetadata = DBMetadataTestingTools.createDummyMetadata();
        ImmutableList<CQIE> rules = convertAssertions(
                ontology.getClassAssertions(),
                ontology.getObjectPropertyAssertions(),
                ontology.getDataPropertyAssertions(),
                annotationAssertions,
                mappingMetadata.getUriTemplateMatcher()
        );

        return datalog2QueryMappingConverter.convertMappingRules(rules, dummyDBMetadata, executorRegistry, mappingMetadata);
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
            Function head = DATA_FACTORY.getFunction(p,
                    uriTemplateMatcher.generateURIFunction(c.getURI()));
            CQIE rule = DATA_FACTORY.getCQIE(head, Collections.emptyList());

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
            Function head = DATA_FACTORY.getFunction(p,
                    uriTemplateMatcher.generateURIFunction(s.getURI()),
                    uriTemplateMatcher.generateURIFunction(o.getURI()));
            CQIE rule = DATA_FACTORY.getCQIE(head, Collections.emptyList());

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
                head = DATA_FACTORY.getFunction(p, DATA_FACTORY.getUriTemplate(
                        DATA_FACTORY.getConstantLiteral(s.getURI())),
                        DATA_FACTORY.getTypedTerm(DATA_FACTORY.getConstantLiteral(o.getValue()), o.getLanguage()));
            } else {

                head = DATA_FACTORY.getFunction(p, DATA_FACTORY.getUriTemplate(
                        DATA_FACTORY.getConstantLiteral(s.getURI())), DATA_FACTORY.getTypedTerm(o, o.getType()));
            }
            CQIE rule = DATA_FACTORY.getCQIE(head, Collections.emptyList());

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
                    head = DATA_FACTORY.getFunction(p, DATA_FACTORY.getUriTemplate(
                            DATA_FACTORY.getConstantLiteral(s.getURI())),
                            DATA_FACTORY.getTypedTerm(DATA_FACTORY.getConstantLiteral(o.getValue()), o.getLanguage()));
                } else {

                    head = DATA_FACTORY.getFunction(p, DATA_FACTORY.getUriTemplate(
                            DATA_FACTORY.getConstantLiteral(s.getURI())), DATA_FACTORY.getTypedTerm(o, o.getType()));
                }
            } else {

                URIConstant o = (URIConstant) v;
                head = DATA_FACTORY.getFunction(p,
                        DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(s.getURI())),
                        DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral(o.getURI())));


            }
            CQIE rule = DATA_FACTORY.getCQIE(head, Collections.emptyList());

            mutableMapping.add(rule);
            count++;
        }

        LOGGER.debug("Appended {} annotation assertions as fact rules", count);
        return ImmutableList.copyOf(mutableMapping);
    }
}
