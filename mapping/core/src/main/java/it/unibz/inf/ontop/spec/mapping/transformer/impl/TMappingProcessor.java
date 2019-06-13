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
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.spec.ontology.ClassExpression;
import it.unibz.inf.ontop.spec.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.DataSomeValuesFrom;
import it.unibz.inf.ontop.spec.ontology.OClass;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.ObjectSomeValuesFrom;
import it.unibz.inf.ontop.spec.ontology.Equivalences;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
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

	public Mapping getTMappings(Mapping mapping, ClassifiedTBox reasoner, CQContainmentCheckUnderLIDs cqc, TMappingExclusionConfig excludeFromTMappings) {

		// Creates an index of all mappings based on the predicate of the head of
		// the mapping. The returned map can be used for fast access to the mapping list.
        ImmutableMultimap<IRI, TMappingRule> originalMappingIndex = mapping.getRDFAtomPredicates().stream()
                .flatMap(p -> mapping.getQueries(p).stream())
                .flatMap(q -> unionSplitter.splitUnion(unionNormalizer.optimize(q)))
                .map(q -> mappingCqcOptimizer.optimize(q))
                .flatMap(q -> iq2DatalogTranslator.translate(q).getRules().stream())
                .map(m -> cqc.removeRedundantAtoms(m))
                .collect(ImmutableCollectors.toMultimap(m -> extractRDFPredicate(m.getHead()),
                        m -> new TMappingRule(m.getHead(), m.getBody(), datalogFactory, termFactory)));

        ImmutableMap.Builder<IRI, ImmutableList<TMappingRule>> builder = ImmutableMap.builder();

        for (Equivalences<ObjectPropertyExpression> propertySet : reasoner.objectPropertiesDAG()) {
            ObjectPropertyExpression representative = propertySet.getRepresentative();
            if (representative.isInverse() || excludeFromTMappings.contains(representative))
                continue;

            ImmutableList<TMappingRule> currentNodeMappings = getTMappingIndexEntry(
                reasoner.objectPropertiesDAG().getSub(propertySet).stream()
                        .flatMap(descendants -> descendants.getMembers().stream())
                        .flatMap(child -> getRulesFromSubObjectProperties(representative.getIRI(), child, originalMappingIndex)), cqc);

            propertySet.getMembers().stream()
                    .filter(p -> !p.isInverse())
                    .forEach(p -> builder.put(p.getIRI(), getPropertyTMappingIndexEntry(currentNodeMappings, p.getIRI())));
        } // object properties loop ended

        for (Equivalences<DataPropertyExpression> propertySet : reasoner.dataPropertiesDAG()) {
            DataPropertyExpression representative = propertySet.getRepresentative();
            if (excludeFromTMappings.contains(representative))
                continue;

            ImmutableList<TMappingRule> currentNodeMappings = getTMappingIndexEntry(
                reasoner.dataPropertiesDAG().getSub(propertySet).stream()
                        .flatMap(descendants -> descendants.getMembers().stream())
                        .flatMap(child -> getRulesFromSubDataProperties(representative.getIRI(), child, originalMappingIndex)), cqc);

            propertySet.getMembers().stream()
                    .forEach(p -> builder.put(p.getIRI(), getPropertyTMappingIndexEntry(currentNodeMappings, p.getIRI())));
        } // data properties loop ended

		for (Equivalences<ClassExpression> classSet : reasoner.classesDAG()) {
			if (!(classSet.getRepresentative() instanceof OClass))
				continue;

			OClass representative = (OClass)classSet.getRepresentative();
			if (excludeFromTMappings.contains(representative))
				continue;

            ImmutableList<TMappingRule> currentNodeMappings = getTMappingIndexEntry(
                reasoner.classesDAG().getSub(classSet).stream()
                        .flatMap(descendants -> descendants.getMembers().stream())
                        .flatMap(child -> getRulesFromSubclasses(representative.getIRI(), child, originalMappingIndex)), cqc);

            classSet.getMembers().stream()
                    .filter(c -> c instanceof OClass)
                    .map(c -> (OClass)c)
                    .forEach(c -> builder.put(c.getIRI(), getClassTMappingIndexEntry(currentNodeMappings, c.getIRI())));
		} // class loop end

        ImmutableMap<IRI, ImmutableList<TMappingRule>> index = builder.build();

		ImmutableList<CQIE> tmappingsProgram = Stream.concat(
                index.values().stream(),
                originalMappingIndex.asMap().entrySet().stream()
                        // probably required for vocabulary terms that are not in the ontology
                        .filter(e -> !index.containsKey(e.getKey()))
                        .map(e -> getTMappingIndexEntry(e.getValue().stream(), cqc)))
                .flatMap(m -> m.stream())
                .map(m -> m.asCQIE())
				.collect(ImmutableCollectors.toSet()).stream() // REMOVE DUPLICATES
		        .collect(ImmutableCollectors.toList());

		return datalog2MappingConverter.convertMappingRules(tmappingsProgram, mapping.getMetadata());
	}

	private Stream<TMappingRule> getRulesFromSubclasses(IRI currentPredicate, ClassExpression child, ImmutableMultimap<IRI, TMappingRule> originalMappingIndex) {
        final int arg;
        final IRI childPredicate;
        if (child instanceof OClass) {
            childPredicate = ((OClass) child).getIRI();
            arg = 0;
        }
        else if (child instanceof ObjectSomeValuesFrom) {
            ObjectPropertyExpression some = ((ObjectSomeValuesFrom) child).getProperty();
            childPredicate = some.getIRI();
            arg = some.isInverse() ? 2 : 0;
        }
        else {
            DataPropertyExpression some = ((DataSomeValuesFrom) child).getProperty();
            childPredicate = some.getIRI();
            arg = 0; // can never be an inverse
        }

        return originalMappingIndex.get(childPredicate).stream()
                .map(childmapping -> {
                    Function newMappingHead = atomFactory.getMutableTripleHeadAtom(
                            childmapping.getHead().getTerm(arg), currentPredicate);
                    return new TMappingRule(newMappingHead, childmapping);
                });
    }

    private Stream<TMappingRule> getRulesFromSubObjectProperties(IRI currentPredicate, ObjectPropertyExpression child, ImmutableMultimap<IRI, TMappingRule> originalMappingIndex) {
        return originalMappingIndex.get(child.getIRI()).stream()
                .map(childmapping -> {
                    List<Term> terms = childmapping.getHead().getTerms();
                    Function newMappingHead = !child.isInverse()
                            ? atomFactory.getMutableTripleHeadAtom(terms.get(0), currentPredicate, terms.get(2))
                            : atomFactory.getMutableTripleHeadAtom(terms.get(2), currentPredicate, terms.get(0));

                    return new TMappingRule(newMappingHead, childmapping);
                });
    }

    private Stream<TMappingRule> getRulesFromSubDataProperties(IRI currentPredicate, DataPropertyExpression child, ImmutableMultimap<IRI, TMappingRule> originalMappingIndex) {
        return originalMappingIndex.get(child.getIRI()).stream()
                .map(childmapping -> {
                    List<Term> terms = childmapping.getHead().getTerms();
                    Function newMappingHead = atomFactory.getMutableTripleHeadAtom(terms.get(0), currentPredicate, terms.get(2));
                    return new TMappingRule(newMappingHead, childmapping);
                });
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


    private ImmutableList<TMappingRule> getPropertyTMappingIndexEntry(ImmutableList<TMappingRule> original, IRI newPredicate) {
        return original.stream()
                .map(rule -> new TMappingRule(
                        atomFactory.getMutableTripleHeadAtom(rule.getHead().getTerm(0), newPredicate, rule.getHead().getTerm(2)), rule))
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableList<TMappingRule> getClassTMappingIndexEntry(ImmutableList<TMappingRule> original, IRI newPredicate) {
        return original.stream()
                .map(rule -> new TMappingRule(
                        atomFactory.getMutableTripleHeadAtom(rule.getHead().getTerm(0), newPredicate), rule))
                .collect(ImmutableCollectors.toList());
    }


    private ImmutableList<TMappingRule> getTMappingIndexEntry(Stream<TMappingRule> stream, CQContainmentCheckUnderLIDs cqc) {
        ImmutableList<TMappingRule> rs = stream.collect(ImmutableCollectors.toList());
        List<TMappingRule> rules = new ArrayList<>(rs.size());
        for (TMappingRule newRule : rs)
            mergeMappingsWithCQC(rules, newRule, cqc);
        return ImmutableList.copyOf(rules);
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

        // Facts are just added
        if (newRule.isFact()) {
            rules.add(newRule);
            return;
        }

        Iterator<TMappingRule> mappingIterator = rules.iterator();
        while (mappingIterator.hasNext()) {

            TMappingRule currentRule = mappingIterator.next();
            // ROMAN (14 Oct 2015): quick fix, but one has to be more careful with variables in filters
            if (currentRule.equals(newRule))
                return;

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
