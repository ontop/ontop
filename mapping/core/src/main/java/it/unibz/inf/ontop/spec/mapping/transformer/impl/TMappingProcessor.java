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
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.mapdb.Fun;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class TMappingProcessor {

	// TODO: the implementation of EXCLUDE ignores equivalent classes / properties


	private final AtomFactory atomFactory;
	private final TermFactory termFactory;
	private final DatalogFactory datalogFactory;
	private final SubstitutionUtilities substitutionUtilities;
	private final ImmutabilityTools immutabilityTools;
    private final Datalog2QueryMappingConverter datalog2MappingConverter;
    private final QueryUnionSplitter unionSplitter;
    private final IQ2DatalogTranslator iq2DatalogTranslator;
    private final UnionFlattener unionNormalizer;
    private final MappingCQCOptimizer mappingCqcOptimizer;

    @Inject
	private TMappingProcessor(AtomFactory atomFactory, TermFactory termFactory, DatalogFactory datalogFactory,
                              SubstitutionUtilities substitutionUtilities,
                              ImmutabilityTools immutabilityTools, Datalog2QueryMappingConverter datalog2MappingConverter, QueryUnionSplitter unionSplitter, IQ2DatalogTranslator iq2DatalogTranslator, UnionFlattener unionNormalizer, MappingCQCOptimizer mappingCqcOptimizer) {
		this.atomFactory = atomFactory;
		this.termFactory = termFactory;
		this.datalogFactory = datalogFactory;
		this.substitutionUtilities = substitutionUtilities;
		this.immutabilityTools = immutabilityTools;
        this.datalog2MappingConverter = datalog2MappingConverter;
        this.unionSplitter = unionSplitter;
        this.iq2DatalogTranslator = iq2DatalogTranslator;
        this.unionNormalizer = unionNormalizer;
        this.mappingCqcOptimizer = mappingCqcOptimizer;
    }


	/**
	 * constructs the TMappings using DAG
	 * @param mapping
	 * @param reasoner
	 * @return
	 */

	public Mapping getTMappings(Mapping mapping, ClassifiedTBox reasoner, CQContainmentCheckUnderLIDs cqc, TMappingExclusionConfig excludeFromTMappings, ImmutableCQContainmentCheck cqContainmentCheck) {

		// Creates an index of all mappings based on the predicate of the head of
		// the mapping. The returned map can be used for fast access to the mapping list.
        ImmutableMultimap<IRI, TMappingRule> originalMappingIndex = mapping.getRDFAtomPredicates().stream()
                .flatMap(p -> mapping.getQueries(p).stream())
                .flatMap(q -> unionSplitter.splitUnion(unionNormalizer.optimize(q)))
                .map(q -> mappingCqcOptimizer.optimize(cqContainmentCheck, q))
                .flatMap(q -> iq2DatalogTranslator.translate(q).getRules().stream())
                .collect(ImmutableCollectors.toMultimap(m -> extractRDFPredicate(m.getHead()),
                        m -> new TMappingRule(m.getHead(), m.getBody(), datalogFactory, termFactory, atomFactory, immutabilityTools)));

        ImmutableMap<IRI, ImmutableList<TMappingRule>> index = Stream.concat(Stream.concat(
                saturate(reasoner.objectPropertiesDAG(),
                        p -> !p.isInverse() && !excludeFromTMappings.contains(p), originalMappingIndex,
                        ObjectPropertyExpression::getIRI, p -> getNewHeadP(p.isInverse()), cqc, p -> !p.isInverse()),

                saturate(reasoner.dataPropertiesDAG(),
                        p -> !excludeFromTMappings.contains(p), originalMappingIndex,
                        DataPropertyExpression::getIRI, p -> getNewHeadP(false), cqc, p -> true)),

                saturate(reasoner.classesDAG(),
                        s -> (s instanceof OClass) && !excludeFromTMappings.contains((OClass)s), originalMappingIndex,
                        this::getIRI, this::getNewHeadC, cqc, c -> c instanceof OClass))

                .collect(ImmutableCollectors.toMap());

		ImmutableList<CQIE> tmappingsProgram = Stream.concat(
                index.values().stream().flatMap(l -> l.stream()),
                originalMappingIndex.asMap().entrySet().stream()
                        // probably required for vocabulary terms that are not in the ontology
                        // also, for all "excluded" mappings
                        .filter(e -> !index.containsKey(e.getKey()))
                        .flatMap(e -> e.getValue().stream().collect(toListWithCQC(cqc)).stream()))
                .map(m -> m.asCQIE())
		        .collect(ImmutableCollectors.toList());

        return datalog2MappingConverter.convertMappingRules(tmappingsProgram, mapping.getMetadata());
	}

    private <T> Stream<Map.Entry<IRI, ImmutableList<TMappingRule>>> saturate(EquivalencesDAG<T> dag,
                                                              Predicate<T> repFilter,
                                                              ImmutableMultimap<IRI, TMappingRule> originalMappingIndex,
                                                              java.util.function.Function<T, IRI> getIRI,
                                                              java.util.function.Function<T, BiFunction<Function, IRI, Function>> getNewHeadGen,
                                                              CQContainmentCheckUnderLIDs cqc,
                                                              Predicate<T> populationFilter) {

	    java.util.function.BiFunction<T, T, java.util.function.Function<TMappingRule, TMappingRule>> headReplacer =
                (s, d) -> (m -> new TMappingRule(getNewHeadGen.apply(s).apply(m.getHead(), getIRI.apply(d)), m));

	    ImmutableMap<IRI, ImmutableList<TMappingRule>> representatives = dag.stream()
                .filter(s -> repFilter.test(s.getRepresentative()))
                .collect(ImmutableCollectors.toMap(
                        s -> getIRI.apply(s.getRepresentative()),
                        s -> dag.getSub(s).stream()
                                .flatMap(ss -> ss.getMembers().stream())
                                .flatMap(d -> originalMappingIndex.get(getIRI.apply(d)).stream()
                                        .map(headReplacer.apply(d, s.getRepresentative())))
                                .collect(toListWithCQC(cqc))));

	    return dag.stream()
                .filter(s -> repFilter.test(s.getRepresentative()))
                .flatMap(s -> s.getMembers().stream()
                    .filter(populationFilter)
                    .collect(ImmutableCollectors.toMap(
                            d -> getIRI.apply(d),
                            d -> representatives.get(getIRI.apply(s.getRepresentative())).stream()
                                    .map(headReplacer.apply(s.getRepresentative(), d))
                                    .collect(ImmutableCollectors.toList())))
                    .entrySet().stream());
    }


	private IRI getIRI(ClassExpression child) {
        if (child instanceof OClass) {
            return ((OClass) child).getIRI();
        }
        else if (child instanceof ObjectSomeValuesFrom) {
            ObjectPropertyExpression some = ((ObjectSomeValuesFrom) child).getProperty();
            return some.getIRI();
        }
        else {
            DataPropertyExpression some = ((DataSomeValuesFrom) child).getProperty();
            return some.getIRI();
        }
    }

	private java.util.function.BiFunction<Function, IRI, Function> getNewHeadC(ClassExpression child) {
        if (child instanceof OClass) {
            return (head, newIri) -> atomFactory.getMutableTripleHeadAtom(head.getTerm(0), newIri);
        }
        else if (child instanceof ObjectSomeValuesFrom) {
            ObjectPropertyExpression some = ((ObjectSomeValuesFrom) child).getProperty();
            return some.isInverse()
                ? (head, newIri) -> atomFactory.getMutableTripleHeadAtom(head.getTerm(2), newIri)
                : (head, newIri) -> atomFactory.getMutableTripleHeadAtom(head.getTerm(0), newIri);
        }
        else {
            DataPropertyExpression some = ((DataSomeValuesFrom) child).getProperty();
            // can never be an inverse
            return (head, newIri) -> atomFactory.getMutableTripleHeadAtom(head.getTerm(0), newIri);
        }
    }

    private java.util.function.BiFunction<Function, IRI, Function> getNewHeadP(boolean isInverse) {
        return isInverse
                ? (head, newIri) -> atomFactory.getMutableTripleHeadAtom(head.getTerm(2), newIri, head.getTerm(0))
                : (head, newIri) -> atomFactory.getMutableTripleHeadAtom(head.getTerm(0), newIri, head.getTerm(2));
    }

    private IRI extractRDFPredicate(Function headAtom) {
		if (!(headAtom.getFunctionSymbol() instanceof RDFAtomPredicate))
			throw new MinorOntopInternalBugException("Mapping assertion without an RDFAtomPredicate found");

		RDFAtomPredicate predicate = (RDFAtomPredicate) headAtom.getFunctionSymbol();

		ImmutableList<ImmutableTerm> arguments = headAtom.getTerms().stream()
				.map(immutabilityTools::convertIntoImmutableTerm)
				.collect(ImmutableCollectors.toList());

		return predicate.getClassIRI(arguments)
				.orElseGet(() -> predicate.getPropertyIRI(arguments)
						.orElseThrow(() -> new MinorOntopInternalBugException("Could not extract a predicate IRI from " + headAtom)));
	}




    private Collector<TMappingRule, BuilderWithCQC, ImmutableList<TMappingRule>> toListWithCQC(CQContainmentCheckUnderLIDs cqc) {
        return Collector.of(
                () -> new BuilderWithCQC(cqc), // Supplier
                BuilderWithCQC::add, // Accumulator
                (b1, b2) -> b1.addAll(b2.build().iterator()), // Merger
                BuilderWithCQC::build, // Finisher
                Collector.Characteristics.UNORDERED);
    }

    private final class BuilderWithCQC {
        private final List<TMappingRule> rules = new ArrayList<>();
        private final CQContainmentCheckUnderLIDs cqc;

        BuilderWithCQC(CQContainmentCheckUnderLIDs cqc) {
            this.cqc = cqc;
        }

        public BuilderWithCQC add(TMappingRule rule) {
            mergeMappingsWithCQC(rules, rule, cqc);
            return this;
        }

        public BuilderWithCQC addAll(Iterator<TMappingRule> rs) {
            while (rs.hasNext())
                mergeMappingsWithCQC(rules, rs.next(), cqc);
            return this;
        }

        public ImmutableList<TMappingRule> build() {
            return ImmutableList.copyOf(rules);
        }
    }


    /***
     *
     * This is an optimization mechanism that allows T-mappings to produce a
     * smaller number of mappings, and hence, the unfolding will be able to
     * produce fewer queries.
     *
     * Given a set of mappings for a class/property A in currentMappings
     * , this method tries to add a the data coming from a new mapping for A in
     * an optimal way, that is, this method will attempt to include the content
     * of coming from newmapping by modifying an existing mapping
     * instead of adding a new mapping.
     *
     * <p/>
     *
     * To do this, this method will strip newmapping from any
     * (in)equality conditions that hold over the variables of the query,
     * leaving only the raw body. Then it will look for another "stripped"
     * mapping <bold>m</bold> in currentMappings such that m is
     * equivalent to stripped(newmapping). If such a m is found, this method
     * will add the extra semantics of newmapping to "m" by appending
     * newmapping's conditions into an OR atom, together with the existing
     * conditions of m.
     *
     * </p>
     * If no such m is found, then this method simply adds newmapping to
     * currentMappings.
     *
     *
     * <p/>
     * For example. If new mapping is equal to
     * <p/>
     *
     * S(x,z) :- R(x,y,z), y = 2
     *
     * <p/>
     * and there exists a mapping m
     * <p/>
     * S(x,z) :- R(x,y,z), y > 7
     *
     * This method would modify 'm' as follows:
     *
     * <p/>
     * S(x,z) :- R(x,y,z), OR(y > 7, y = 2)
     *
     * <p/>
     *
     */

    private void mergeMappingsWithCQC(List<TMappingRule> rules, TMappingRule newRule, CQContainmentCheckUnderLIDs cqc) {

        if (rules.contains(newRule))
            return;

        // Facts are just added
        if (newRule.isFact()) {
            rules.add(newRule);
            return;
        }

        Iterator<TMappingRule> mappingIterator = rules.iterator();
        while (mappingIterator.hasNext()) {

            TMappingRule currentRule = mappingIterator.next();

            boolean couldIgnore = false;

            Substitution toNewRule = newRule.computeHomomorphsim(currentRule, cqc);
            if ((toNewRule != null) && checkConditions(newRule, currentRule, toNewRule)) {
                if (newRule.getDatabaseAtoms().size() < currentRule.getDatabaseAtoms().size()) {
                    couldIgnore = true;
                }
                else {
                    // if the new mapping is redundant and there are no conditions then do not add anything
                    return;
                }
            }

            Substitution fromNewRule = currentRule.computeHomomorphsim(newRule, cqc);
            if ((fromNewRule != null) && checkConditions(currentRule, newRule, fromNewRule)) {
                // The existing query is more specific than the new query, so we
                // need to add the new query and remove the old
                mappingIterator.remove();
                continue;
            }

            if (couldIgnore) {
                // if the new mapping is redundant and there are no conditions then do not add anything
                return;
            }

            if ((toNewRule != null) && (fromNewRule != null)) {
                // We found an equivalence, we will try to merge the conditions of
                // newRule into the currentRule
                // Here we can merge conditions of the new query with the one we have just found
                // new map always has just one set of filters  !!
                ImmutableList<Function> newf = newRule.getConditions().get(0).stream()
                        .map(atom -> applySubstitution(atom, fromNewRule))
                        .collect(ImmutableCollectors.toList());

                // if each of the existing conditions in one of the filter groups
                // is found in the new filter then the new filter is redundant
                if (currentRule.getConditions().stream().anyMatch(f -> newf.containsAll(f)))
                    return;

                // REPLACE THE CURRENT RULE
                mappingIterator.remove();
                rules.add(new TMappingRule(currentRule,
                        Stream.concat(currentRule.getConditions().stream()
                                // if each of the new conditions is found among econd then the old condition is redundant
                                .filter(f -> !f.containsAll(newf)), // no need to clone
                        Stream.of(newf))
                        .collect(ImmutableCollectors.toList())));
                return;
            }
        }
        rules.add(newRule);
    }

    private boolean checkConditions(TMappingRule rule1, TMappingRule rule2, Substitution toRule1) {
        if (rule2.getConditions().size() == 0)
            return true;
        if (rule2.getConditions().size() > 1 || rule1.getConditions().size() != 1)
            return false;

        return rule2.getConditions().get(0).stream()
                .map(atom -> applySubstitution(atom, toRule1))
                .allMatch(atom -> rule1.getConditions().get(0).contains(atom));
    }

    private Function applySubstitution(Function atom, Substitution sub) {
        Function clone = (Function)atom.clone();
        substitutionUtilities.applySubstitution(clone, sub);
        return clone;
    }
}
