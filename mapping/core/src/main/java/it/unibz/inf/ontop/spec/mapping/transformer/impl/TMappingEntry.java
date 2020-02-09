package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphism;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphismIterator;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class TMappingEntry {
    private ImmutableList<TMappingRule> rules;

    private final TermFactory termFactory;


    public TMappingEntry(ImmutableList<TMappingRule> rules, TermFactory termFactory) {
        this.rules = rules;
        this.termFactory = termFactory;
    }

    public TMappingEntry createCopy(java.util.function.Function<TMappingRule, TMappingRule> headReplacer) {
        return new TMappingEntry(
                rules.stream().map(headReplacer).collect(ImmutableCollectors.toList()),
                termFactory);
    }

    public IQ asIQ(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory, UnionBasedQueryMerger queryMerger) {
        return queryMerger.mergeDefinitions(rules.stream()
                        .map(r -> r.asIQ(iqFactory, substitutionFactory))
                        .collect(ImmutableCollectors.toList())).get();
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }

    // ASSUMES NON-EMPTINESS
    public MappingAssertionIndex getPredicateInfo() { return rules.iterator().next().getPredicateInfo(); }

    public RDFAtomPredicate getRDFAtomPredicate() { return rules.iterator().next().getRDFAtomPredicate(); }

    @Override
    public String toString() { return "TME: " + getPredicateInfo() + ": " + rules.toString(); }

    public static Collector<TMappingRule, BuilderWithCQC, TMappingEntry> toTMappingEntry(ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> cqc, TermFactory termFactory) {
        return Collector.of(
                () -> new BuilderWithCQC(cqc, termFactory), // Supplier
                BuilderWithCQC::add, // Accumulator
                (b1, b2) -> b1.addAll(b2.build().rules.iterator()), // Merger
                BuilderWithCQC::build, // Finisher
                Collector.Characteristics.UNORDERED);
    }

    private static final class BuilderWithCQC {
        private final List<TMappingRule> rules = new ArrayList<>();
        private final ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> cqc;

        private final TermFactory termFactory;

        BuilderWithCQC(ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> cqc, TermFactory termFactory) {
            this.cqc = cqc;
            this.termFactory = termFactory;
        }

        public BuilderWithCQC add(TMappingRule rule) {
            mergeMappingsWithCQC(rule);
            return this;
        }

        public BuilderWithCQC addAll(Iterator<TMappingRule> rs) {
            while (rs.hasNext())
                mergeMappingsWithCQC(rs.next());
            return this;
        }

        public TMappingEntry build() {
            return new TMappingEntry(ImmutableList.copyOf(rules), termFactory);
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

                Optional<ImmutableHomomorphism> to =
                        fixHeadTermMapping(current.getHeadTerms(), assertion.getHeadTerms())
                        .map(h -> cqc.homomorphismIterator(h, IQ2CQ.toDataAtoms(current.getDatabaseAtoms()), IQ2CQ.toDataAtoms(assertion.getDatabaseAtoms())))
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

                Optional<ImmutableHomomorphism> from =
                        fixHeadTermMapping(assertion.getHeadTerms(), current.getHeadTerms())
                        .map(h -> cqc.homomorphismIterator(h, IQ2CQ.toDataAtoms(assertion.getDatabaseAtoms()), IQ2CQ.toDataAtoms(current.getDatabaseAtoms())))
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

        private Optional<ImmutableHomomorphism> fixHeadTermMapping(ImmutableList<ImmutableTerm> fromHead, ImmutableList<ImmutableTerm> toHead) {
            ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder();
            for (int i = 0; i < fromHead.size(); i++)
                if (!builder.extend(fromHead.get(i), toHead.get(i)).isValid())
                    return Optional.empty();
            return Optional.of(builder.build());
        }
    }
}
