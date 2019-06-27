package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;


public class LegacyABoxFactIntoMappingConverter implements ABoxFactIntoMappingConverter {

    private final SpecificationFactory mappingFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyABoxFactIntoMappingConverter.class);
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;
    private final ImmutabilityTools immutabilityTools;
    private final IntermediateQueryFactory iqFactory;
    private final DatalogRule2QueryConverter datalogRuleConverter;
    private final UnionBasedQueryMerger queryMerger;


    @Inject
    public LegacyABoxFactIntoMappingConverter(SpecificationFactory mappingFactory, AtomFactory atomFactory,
                                              TermFactory termFactory, DatalogFactory datalogFactory,
                                              ImmutabilityTools immutabilityTools, IntermediateQueryFactory iqFactory, DatalogRule2QueryConverter datalogRuleConverter, UnionBasedQueryMerger queryMerger) {
        this.mappingFactory = mappingFactory;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
        this.immutabilityTools = immutabilityTools;
        this.iqFactory = iqFactory;
        this.datalogRuleConverter = datalogRuleConverter;
        this.queryMerger = queryMerger;
    }

    @Override
    public Mapping convert(OntologyABox ontology, boolean isOntologyAnnotationQueryingEnabled,
                           UriTemplateMatcher uriTemplateMatcher) {

        ImmutableMultimap<Term, Function> classes = ontology.getClassAssertions().stream()
                .map(ca -> atomFactory.getMutableTripleHeadAtom(
                        getTerm(ca.getIndividual(), uriTemplateMatcher),
                        ca.getConcept().getIRI()))
                .collect(ImmutableCollectors.toMultimap(a -> a.getTerm(2), a -> a));

        ImmutableMultimap<Term, Function> properties = Stream.concat(Stream.concat(
                ontology.getObjectPropertyAssertions().stream()
                    .map(pa -> atomFactory.getMutableTripleHeadAtom(
                            getTerm(pa.getSubject(), uriTemplateMatcher),
                            pa.getProperty().getIRI(),
                            getTerm(pa.getObject(), uriTemplateMatcher))),

                ontology.getDataPropertyAssertions().stream()
                    .map(da -> atomFactory.getMutableTripleHeadAtom(
                            getTerm(da.getSubject(), uriTemplateMatcher),
                            da.getProperty().getIRI(),
                            getValueConstant(da.getValue())))),

                isOntologyAnnotationQueryingEnabled
                    ? ontology.getAnnotationAssertions().stream()
                        .map(aa -> atomFactory.getMutableTripleHeadAtom(
                            getTerm(aa.getSubject(), uriTemplateMatcher),
                            aa.getProperty().getIRI(),
                            (aa.getValue() instanceof ValueConstant)
                                    ? getValueConstant((ValueConstant) aa.getValue())
                                    : getTerm((ObjectConstant) aa.getValue(), uriTemplateMatcher)))
                    : Stream.of())
                .collect(ImmutableCollectors.toMultimap(a -> a.getTerm(1), a -> a));

        LOGGER.debug("Appended {} object property assertions as fact rules", ontology.getObjectPropertyAssertions().size());
        LOGGER.debug("Appended {} data property assertions as fact rules", ontology.getDataPropertyAssertions().size());
        LOGGER.debug("Appended {} annotation assertions as fact rules", ontology.getAnnotationAssertions().size());
        LOGGER.debug("Appended {} class assertions from ontology as fact rules", ontology.getClassAssertions().size());

        ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable = table(classes);
        ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable = table(properties);

        if (!classTable.isEmpty())
            System.out.println("CLASS TABLE " + classTable);

        Mapping a = mappingFactory.createMapping(
                mappingFactory.createMetadata(
                        //TODO: parse the ontology prefixes ??
                        mappingFactory.createPrefixManager(ImmutableMap.of()),
                        uriTemplateMatcher),
                propertyTable,
                classTable);

        return a;
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> table(ImmutableMultimap<Term, Function> heads) {

        return heads.keySet().stream()
                .map(iri -> queryMerger.mergeDefinitions(
                        heads.get(iri).stream()
                                .map(h -> datalogRuleConverter.convertDatalogRule(datalogFactory.getCQIE(h, Collections.emptyList()), ImmutableSet.of(), Optional.empty(), iqFactory))
                                .collect(ImmutableCollectors.toList())).get())
                .map(IQ::liftBinding)
                .map(iq -> Tables.immutableCell(
                        (RDFAtomPredicate) iq.getProjectionAtom().getPredicate(),
                        MappingTools.extractRDFPredicate(iq).getIri(),
                        iq))
                .collect(ImmutableCollectors.toTable());
    }



    // BNODES are not supported here
    private Term getTerm(ObjectConstant o, UriTemplateMatcher uriTemplateMatcher) {
        IRIConstant iri = (IRIConstant)o;
        return immutabilityTools.convertToMutableFunction(uriTemplateMatcher.generateURIFunction(iri.getIRI().getIRIString()));
    }

    private Term getValueConstant(ValueConstant o) {
        return o.getType().getLanguageTag()
                .map(lang ->
                        termFactory.getTypedTerm(termFactory.getConstantLiteral(o.getValue()), lang.getFullString()))
                .orElseGet(() ->
                        termFactory.getTypedTerm(o, o.getType()));
    }


}
