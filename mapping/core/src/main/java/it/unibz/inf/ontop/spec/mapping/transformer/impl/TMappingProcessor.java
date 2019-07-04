package it.unibz.inf.ontop.spec.mapping.transformer.impl;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TMappingProcessor {

	// TODO: the implementation of EXCLUDE ignores equivalent classes / properties


	private final AtomFactory atomFactory;
	private final TermFactory termFactory;
	private final DatalogFactory datalogFactory;
	private final SubstitutionUtilities substitutionUtilities;
	private final ImmutabilityTools immutabilityTools;
    private final QueryUnionSplitter unionSplitter;
    private final IQ2DatalogTranslator iq2DatalogTranslator;
    private final UnionFlattener unionNormalizer;
    private final MappingCQCOptimizer mappingCqcOptimizer;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final SpecificationFactory specificationFactory;
    private final IntermediateQueryFactory iqFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final DatalogRule2QueryConverter datalogRuleConverter;

    @Inject
	private TMappingProcessor(AtomFactory atomFactory, TermFactory termFactory, DatalogFactory datalogFactory,
                              SubstitutionUtilities substitutionUtilities,
                              ImmutabilityTools immutabilityTools,
                              QueryUnionSplitter unionSplitter, IQ2DatalogTranslator iq2DatalogTranslator,
                              UnionFlattener unionNormalizer, MappingCQCOptimizer mappingCqcOptimizer,
                              NoNullValueEnforcer noNullValueEnforcer,
                              SpecificationFactory specificationFactory, IntermediateQueryFactory iqFactory,
                              UnionBasedQueryMerger queryMerger, DatalogRule2QueryConverter datalogRuleConverter) {
		this.atomFactory = atomFactory;
		this.termFactory = termFactory;
		this.datalogFactory = datalogFactory;
		this.substitutionUtilities = substitutionUtilities;
		this.immutabilityTools = immutabilityTools;
        this.unionSplitter = unionSplitter;
        this.iq2DatalogTranslator = iq2DatalogTranslator;
        this.unionNormalizer = unionNormalizer;
        this.mappingCqcOptimizer = mappingCqcOptimizer;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.specificationFactory = specificationFactory;
        this.iqFactory = iqFactory;
        this.queryMerger = queryMerger;
        this.datalogRuleConverter = datalogRuleConverter;
    }


	/**
	 * constructs the TMappings using DAG
	 * @param mapping
	 * @param reasoner
	 * @return
	 */

	public Mapping getTMappings(Mapping mapping, ClassifiedTBox reasoner, CQContainmentCheckUnderLIDs cqc, TMappingExclusionConfig excludeFromTMappings, ImmutableCQContainmentCheck cqContainmentCheck) {

	    // index mapping assertions by the predicate type
        //     same IRI can be a class name and a property name
        //     but the same IRI cannot be an object and a data or annotation property name at the same time
        // see https://www.w3.org/TR/owl2-new-features/#F12:_Punning

        ImmutableMultimap<MappingTools.RDFPredicateInfo, TMappingRule> originalMappingIndex = mapping.getRDFAtomPredicates().stream()
                .flatMap(p -> mapping.getQueries(p).stream())
                .flatMap(q -> unionSplitter.splitUnion(unionNormalizer.optimize(q)))
                .map(q -> mappingCqcOptimizer.optimize(cqContainmentCheck, q))
                .map(q -> new TMappingRule(q, datalogFactory, termFactory, atomFactory, immutabilityTools, iq2DatalogTranslator, iqFactory, datalogRuleConverter))
                .collect(ImmutableCollectors.toMultimap(q -> q.getPredicateInfo(), q -> q));

        ImmutableMap<MappingTools.RDFPredicateInfo, TMappingEntry> index = Stream.concat(Stream.concat(
                saturate(reasoner.objectPropertiesDAG(),
                        p -> !p.isInverse() && !excludeFromTMappings.contains(p), originalMappingIndex,
                        this::getRDFPredicateInfo, p -> getNewHeadP(p.isInverse()), cqc, p -> !p.isInverse()),

                saturate(reasoner.dataPropertiesDAG(),
                        p -> !excludeFromTMappings.contains(p), originalMappingIndex,
                        this::getRDFPredicateInfo, p -> getNewHeadP(false), cqc, p -> true)),

                saturate(reasoner.classesDAG(),
                        s -> (s instanceof OClass) && !excludeFromTMappings.contains((OClass)s), originalMappingIndex,
                        this::getRDFPredicateInfo, this::getNewHeadC, cqc, c -> c instanceof OClass))

                .collect(ImmutableCollectors.toMap());

        ImmutableList<TMappingEntry> entries = Stream.concat(
                index.values().stream(),
                originalMappingIndex.asMap().entrySet().stream()
                        // probably required for vocabulary terms that are not in the ontology
                        // also, for all "excluded" mappings
                        .filter(e -> !index.containsKey(e.getKey()))
                        .map(e -> e.getValue().stream()
                                .collect(TMappingEntry.toTMappingEntry(cqc, noNullValueEnforcer, queryMerger, substitutionUtilities))))
                .collect(ImmutableCollectors.toList());

        return specificationFactory.createMapping(mapping.getMetadata(),
                        extractTable(entries.stream().filter(e -> !e.getPredicateInfo().isClass())),
                        extractTable(entries.stream().filter(e -> e.getPredicateInfo().isClass())));
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> extractTable(Stream<TMappingEntry> stream) {

        return stream
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getPredicateInfo(), e.asIQ()))
                .map(e -> Tables.immutableCell(
                        (RDFAtomPredicate) e.getValue().getProjectionAtom().getPredicate(),
                        e.getKey().getIri(),
                        e.getValue()))
                .collect(ImmutableCollectors.toTable());
    }

    private <T> Stream<Map.Entry<MappingTools.RDFPredicateInfo, TMappingEntry>> saturate(EquivalencesDAG<T> dag,
                                                                                         Predicate<T> repFilter,
                                                                                         ImmutableMultimap<MappingTools.RDFPredicateInfo, TMappingRule> originalMappingIndex,
                                                                                         java.util.function.Function<T, MappingTools.RDFPredicateInfo> getIRI,
                                                                                         java.util.function.Function<T, Function<ImmutableList<Term>, ImmutableList<Term>>> getNewHeadGen,
                                                                                         CQContainmentCheckUnderLIDs cqc,
                                                                                         Predicate<T> populationFilter) {

	    java.util.function.BiFunction<T, T, java.util.function.Function<TMappingRule, TMappingRule>> headReplacer =
                (s, d) -> (m -> new TMappingRule(getNewHeadGen.apply(s).apply(m.getHeadTerms()), getIRI.apply(d), m));

	    ImmutableMap<MappingTools.RDFPredicateInfo, TMappingEntry> representatives = dag.stream()
                .filter(s -> repFilter.test(s.getRepresentative()))
                .collect(ImmutableCollectors.toMap(
                        s -> getIRI.apply(s.getRepresentative()),
                        s -> dag.getSub(s).stream()
                                .flatMap(ss -> ss.getMembers().stream())
                                .flatMap(d -> originalMappingIndex.get(getIRI.apply(d)).stream()
                                        .map(headReplacer.apply(d, s.getRepresentative())))
                                .collect(TMappingEntry.toTMappingEntry(cqc, noNullValueEnforcer, queryMerger, substitutionUtilities))));

	    return dag.stream()
                .filter(s -> repFilter.test(s.getRepresentative()))
                .flatMap(s -> s.getMembers().stream()
                    .filter(populationFilter)
                    .collect(ImmutableCollectors.toMap(
                            d -> getIRI.apply(d),
                            d -> representatives.get(getIRI.apply(s.getRepresentative()))
                                    .createCopy(headReplacer.apply(s.getRepresentative(), d))))
                    .entrySet().stream())
                .filter(e -> !e.getValue().isEmpty());
    }


	private MappingTools.RDFPredicateInfo getRDFPredicateInfo(ClassExpression child) {
        if (child instanceof OClass)
            return new MappingTools.RDFPredicateInfo(true, ((OClass) child).getIRI());
        else if (child instanceof ObjectSomeValuesFrom)
            return getRDFPredicateInfo(((ObjectSomeValuesFrom) child).getProperty());
        else
            return getRDFPredicateInfo(((DataSomeValuesFrom) child).getProperty());
    }

    private MappingTools.RDFPredicateInfo getRDFPredicateInfo(ObjectPropertyExpression child) {
        return new MappingTools.RDFPredicateInfo(false, child.getIRI());
    }

    private MappingTools.RDFPredicateInfo getRDFPredicateInfo(DataPropertyExpression child) {
        return new MappingTools.RDFPredicateInfo(false, child.getIRI());
    }

    private java.util.function.Function<ImmutableList<Term>, ImmutableList<Term>> getNewHeadC(ClassExpression child) {
        if (child instanceof OClass) {
            return Function.identity();
        }
        else if (child instanceof ObjectSomeValuesFrom) {
            ObjectPropertyExpression some = ((ObjectSomeValuesFrom) child).getProperty();
            return some.isInverse()
                ? head -> ImmutableList.of(head.get(1))
                : head -> ImmutableList.of(head.get(0));
        }
        else {
            DataPropertyExpression some = ((DataSomeValuesFrom) child).getProperty();
            // can never be an inverse
            return head -> ImmutableList.of(head.get(0));
        }
    }

    private java.util.function.Function<ImmutableList<Term>, ImmutableList<Term>> getNewHeadP(boolean isInverse) {
        return isInverse
                ? head -> ImmutableList.of(head.get(1), head.get(0))
                : Function.identity();
    }
}
