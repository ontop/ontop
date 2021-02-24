package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphism;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphismIterator;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.tools.impl.IQ2CQ;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class TMappingEntry {

    public static Collector<TMappingRule, TMappingEntry, ImmutableList<TMappingRule>> toTMappingEntry(ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> cqc, CoreSingletons coreSingletons) {
        return Collector.of(
                () -> new TMappingEntry(cqc, coreSingletons), // Supplier
                TMappingEntry::add, // Accumulator
                (b1, b2) -> b1.addAll(b2.build().iterator()), // Merger
                TMappingEntry::build, // Finisher
                Collector.Characteristics.UNORDERED);
    }

    private final List<TMappingRule> rules = new ArrayList<>();
    private final ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> cqc;
    private final TermFactory termFactory;
    private final CoreSingletons coreSingletons;

    public TMappingEntry(ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> cqc, CoreSingletons coreSingletons) {
        this.cqc = cqc;
        this.termFactory = coreSingletons.getTermFactory();
        this.coreSingletons = coreSingletons;
    }

    public TMappingEntry add(TMappingRule rule) {
        mergeMappingsWithCQC(rule);
        return this;
    }

    public TMappingEntry addAll(Iterator<TMappingRule> rs) {
        while (rs.hasNext())
            mergeMappingsWithCQC(rs.next());
        return this;
    }

    public ImmutableList<TMappingRule> build() {
        return ImmutableList.copyOf(rules);
    }


    /***
     *
     * This is an optimization mechanism that allows T-mappings to reduce
     * the number of mapping assertions. The unfolding will then produce fewer queries.
     *
     * The method
     *    (1) removes a mapping assertion from rules if it is subsumed by the given assertion
     *
     *    (2) does not add the assertion if it is subsumed by one of the rules
     *
     *    (3) merges the given assertion into an existing assertion if their database atoms
     *        are homomorphically equivalent
     *
     * For example, if we are given
     *     S(x,z) :- R(x,y,z), y = 2
     * and rules contains
     *     S(x,z) :- R(x,y,z), y > 7
     * then this method will modify the existing assertion into
     *     S(x,z) :- R(x,y,z), OR(y > 7, y = 2)
     */

    private void mergeMappingsWithCQC(TMappingRule assertion) {

        if (rules.contains(assertion))
            return;

        if (assertion.getDatabaseAtoms().isEmpty() && assertion.getConditions().isEmpty()) {
            rules.add(assertion); // facts are just added
            return;
        }

        Iterator<TMappingRule> mappingIterator = rules.iterator();
        while (mappingIterator.hasNext()) {

            TMappingRule current = mappingIterator.next();
            // to and from refer to "to assertion" and "from assertion"

            boolean couldIgnore = false;

            Optional<ImmutableHomomorphism> to = getHomomorphismIterator(current, assertion)
                            .filter(ImmutableHomomorphismIterator::hasNext)
                            .map(ImmutableHomomorphismIterator::next);

            if (to.isPresent()) {
                if (current.getConditions().isEmpty() ||
                        (current.getConditions().size() == 1 &&
                                assertion.getConditions().size() == 1 &&
                                // rule1.getConditions().get(0) contains all images of rule2.getConditions.get(0)
                                current.getConditions().get(0).stream()
                                        .map(atom -> to.get().applyToBooleanExpression(atom, termFactory))
                                        .allMatch(atom -> assertion.getConditions().get(0).contains(atom)))) {

                    if (assertion.getDatabaseAtoms().size() < current.getDatabaseAtoms().size()) {
                        couldIgnore = true;
                    }
                    else {
                        // if the new mapping is redundant and there are no conditions then do not add anything
                        return;
                    }
                }
            }

            Optional<ImmutableHomomorphism> from = getHomomorphismIterator(assertion, current)
                            .filter(ImmutableHomomorphismIterator::hasNext)
                            .map(ImmutableHomomorphismIterator::next);

            if (from.isPresent()) {
                if (assertion.getConditions().isEmpty() ||
                        (assertion.getConditions().size() == 1 &&
                                current.getConditions().size() == 1 &&
                                // rule1.getConditions().get(0) contains all images of rule2.getConditions.get(0)
                                assertion.getConditions().get(0).stream()
                                        .map(atom -> from.get().applyToBooleanExpression(atom, termFactory))
                                        .allMatch(atom -> current.getConditions().get(0).contains(atom)))) {

                    // The existing query is more specific than the new query, so we
                    // need to add the new query and remove the old
                    mappingIterator.remove();
                    continue;
                }
            }

            if (couldIgnore) {
                // if the new mapping is redundant and there are no conditions then do not add anything
                return;
            }

            if (to.isPresent() && from.isPresent()) {
                // We found an equivalence, we will try to merge the conditions of
                // newRule into the current
                // Here we can merge conditions of the new query with the one we have just found
                // new map always has just one set of filters  !!
                ImmutableList<ImmutableExpression> newf = assertion.getConditions().get(0).stream()
                        .map(atom -> from.get().applyToBooleanExpression(atom, termFactory))
                        .collect(ImmutableCollectors.toList());

                ImmutableSet<Variable> newfVars = newf.stream()
                        .flatMap(ImmutableTerm::getVariableStream)
                        .collect(ImmutableCollectors.toSet());
                ImmutableSet<Variable> ccVars = current.getDatabaseAtoms().stream()
                        .flatMap(a -> a.getVariables().stream())
                        .collect(ImmutableCollectors.toSet());
                if (ccVars.containsAll(newfVars)) {
                    // if each of the existing conditions in one of the filter groups
                    // is found in the new filter then the new filter is redundant
                    if (current.getConditions().stream().anyMatch(newf::containsAll))
                        return;

                    // REPLACE THE CURRENT RULE
                    mappingIterator.remove();
                    rules.add(new TMappingRule(current, Stream.concat(
                            current.getConditions().stream()
                                    // if each of the new conditions is found among econd then the old condition is redundant
                                    .filter(f -> !f.containsAll(newf)),
                            Stream.of(newf))
                            .collect(ImmutableCollectors.toList())));
                    return;
                }
            }
        }
        rules.add(assertion);
    }

    private Optional<ImmutableHomomorphismIterator<RelationPredicate>> getHomomorphismIterator(TMappingRule from, TMappingRule to) {
        ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder();
        for (int i = 0; i < from.getHeadTerms().size(); i++)
            if (!builder.extend(from.getHeadTerms().get(i), to.getHeadTerms().get(i)).isValid())
                return Optional.empty();

        ImmutableHomomorphism h = builder.build();
        return Optional.of(cqc.homomorphismIterator(h, IQ2CQ.toDataAtoms(from.getDatabaseAtoms(), coreSingletons),
                IQ2CQ.toDataAtoms(to.getDatabaseAtoms(), coreSingletons)));
    }
}
