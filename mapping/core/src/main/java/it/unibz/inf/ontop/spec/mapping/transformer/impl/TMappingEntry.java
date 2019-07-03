package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class TMappingEntry {
    private ImmutableList<TMappingRule> rules;

    private final NoNullValueEnforcer noNullValueEnforcer;
    private final UnionBasedQueryMerger queryMerger;
    private final SubstitutionUtilities substitutionUtilities;


    public TMappingEntry(ImmutableList<TMappingRule> rules, NoNullValueEnforcer noNullValueEnforcer, UnionBasedQueryMerger queryMerger, SubstitutionUtilities substitutionUtilities) {
        this.rules = rules;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.queryMerger = queryMerger;
        this.substitutionUtilities = substitutionUtilities;
    }

    public TMappingEntry createCopy(java.util.function.Function<TMappingRule, TMappingRule> headReplacer) {
        return new TMappingEntry(
                rules.stream().map(headReplacer).collect(ImmutableCollectors.toList()),
                noNullValueEnforcer,
                queryMerger,
                substitutionUtilities);
    }

    public IQ asIQ() {
        // In case some legacy implementations do not preserve IS_NOT_NULL conditions
        return noNullValueEnforcer.transform(
                    queryMerger.mergeDefinitions(rules.stream()
                        .map(r -> r.asIQ())
                        .collect(ImmutableCollectors.toList())).get())
               .liftBinding();
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }

    // ASSUMES NON-EMPTINESS
    public MappingTools.RDFPredicateInfo getPredicateInfo() { return rules.iterator().next().getPredicateInfo(); }

    public static Collector<TMappingRule, BuilderWithCQC, TMappingEntry> toTMappingEntry(CQContainmentCheckUnderLIDs cqc, NoNullValueEnforcer noNullValueEnforcer, UnionBasedQueryMerger queryMerger, SubstitutionUtilities substitutionUtilities) {
        return Collector.of(
                () -> new BuilderWithCQC(cqc, noNullValueEnforcer, queryMerger, substitutionUtilities), // Supplier
                BuilderWithCQC::add, // Accumulator
                (b1, b2) -> b1.addAll(b2.build().rules.iterator()), // Merger
                BuilderWithCQC::build, // Finisher
                Collector.Characteristics.UNORDERED);
    }

    private static final class BuilderWithCQC {
        private final List<TMappingRule> rules = new ArrayList<>();
        private final CQContainmentCheckUnderLIDs cqc;
        private final NoNullValueEnforcer noNullValueEnforcer;
        private final UnionBasedQueryMerger queryMerger;
        private final SubstitutionUtilities substitutionUtilities;

        BuilderWithCQC(CQContainmentCheckUnderLIDs cqc, NoNullValueEnforcer noNullValueEnforcer, UnionBasedQueryMerger queryMerger, SubstitutionUtilities substitutionUtilities) {
            this.cqc = cqc;
            this.noNullValueEnforcer = noNullValueEnforcer;
            this.queryMerger = queryMerger;
            this.substitutionUtilities = substitutionUtilities;
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
            return new TMappingEntry(ImmutableList.copyOf(rules), noNullValueEnforcer, queryMerger, substitutionUtilities);
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

        private void mergeMappingsWithCQC(TMappingRule newRule) {

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
}
