package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphism;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphismIterator;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
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
    private final ImmutableSubstitutionTools immutableSubstitutionTools;
    private final SubstitutionFactory substitutionFactory;


    public TMappingEntry(ImmutableList<TMappingRule> rules, NoNullValueEnforcer noNullValueEnforcer, UnionBasedQueryMerger queryMerger, SubstitutionUtilities substitutionUtilities, ImmutableSubstitutionTools immutableSubstitutionTools, SubstitutionFactory substitutionFactory) {
        this.rules = rules;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.queryMerger = queryMerger;
        this.substitutionUtilities = substitutionUtilities;
        this.immutableSubstitutionTools = immutableSubstitutionTools;
        this.substitutionFactory = substitutionFactory;
    }

    public TMappingEntry createCopy(java.util.function.Function<TMappingRule, TMappingRule> headReplacer) {
        return new TMappingEntry(
                rules.stream().map(headReplacer).collect(ImmutableCollectors.toList()),
                noNullValueEnforcer,
                queryMerger,
                substitutionUtilities, immutableSubstitutionTools, substitutionFactory);
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

    @Override
    public String toString() { return "TME: " + getPredicateInfo() + ": " + rules.toString(); }

    public static Collector<TMappingRule, BuilderWithCQC, TMappingEntry> toTMappingEntry(ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc, NoNullValueEnforcer noNullValueEnforcer, UnionBasedQueryMerger queryMerger, SubstitutionUtilities substitutionUtilities, ImmutableSubstitutionTools immutableSubstitutionTools, SubstitutionFactory substitutionFactory) {
        return Collector.of(
                () -> new BuilderWithCQC(cqc, noNullValueEnforcer, queryMerger, substitutionUtilities, immutableSubstitutionTools, substitutionFactory), // Supplier
                BuilderWithCQC::add, // Accumulator
                (b1, b2) -> b1.addAll(b2.build().rules.iterator()), // Merger
                BuilderWithCQC::build, // Finisher
                Collector.Characteristics.UNORDERED);
    }

    private static final class BuilderWithCQC {
        private final List<TMappingRule> rules = new ArrayList<>();
        private final ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc;
        private final NoNullValueEnforcer noNullValueEnforcer;
        private final UnionBasedQueryMerger queryMerger;
        private final SubstitutionUtilities substitutionUtilities;
        private final ImmutableSubstitutionTools immutableSubstitutionTools;
        private final SubstitutionFactory substitutionFactory;

        BuilderWithCQC(ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc, NoNullValueEnforcer noNullValueEnforcer, UnionBasedQueryMerger queryMerger, SubstitutionUtilities substitutionUtilities, ImmutableSubstitutionTools immutableSubstitutionTools, SubstitutionFactory substitutionFactory) {
            this.cqc = cqc;
            this.noNullValueEnforcer = noNullValueEnforcer;
            this.queryMerger = queryMerger;
            this.substitutionUtilities = substitutionUtilities;
            this.immutableSubstitutionTools = immutableSubstitutionTools;
            this.substitutionFactory = substitutionFactory;
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
            return new TMappingEntry(ImmutableList.copyOf(rules), noNullValueEnforcer, queryMerger, substitutionUtilities, immutableSubstitutionTools, substitutionFactory);
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

                ImmutableSubstitution<VariableOrGroundTerm> toNewRule = computeHomomorphsim(newRule, currentRule, cqc);
                if ((toNewRule != null) && checkConditions(newRule, currentRule, toNewRule)) {
                    if (newRule.getDatabaseAtoms().size() < currentRule.getDatabaseAtoms().size()) {
                        couldIgnore = true;
                    }
                    else {
                        // if the new mapping is redundant and there are no conditions then do not add anything
                        return;
                    }
                }

                ImmutableSubstitution<VariableOrGroundTerm> fromNewRule = computeHomomorphsim(currentRule, newRule, cqc);
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
                    ImmutableList<ImmutableExpression> newf = newRule.getConditions().get(0).stream()
                            .map(atom -> fromNewRule.applyToBooleanExpression(atom))
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

        private ImmutableSubstitution<VariableOrGroundTerm> computeHomomorphsim(TMappingRule to, TMappingRule from, ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc) {
            ImmutableHomomorphism.Builder builder = ImmutableHomomorphism.builder();
            boolean headMatch = extendHomomorphism(builder, from.getHeadTerms(), to.getHeadTerms());
            if (!headMatch)
                return null;

            ImmutableHomomorphismIterator h = cqc.homomorphismIterator(builder.build(), from.getDatabaseAtoms(), to.getDatabaseAtoms());
            if (!h.hasNext())
                return null;

            ImmutableHomomorphism hom = h.next();
            return substitutionFactory.getSubstitution(
                    hom.asMap().entrySet().stream()
                            .filter(e -> !e.getKey().equals(e.getValue()))
                            .collect(ImmutableCollectors.toMap()));
        }

        private static boolean extendHomomorphism(ImmutableHomomorphism.Builder builder, ImmutableList<? extends ImmutableTerm> from, ImmutableList<? extends ImmutableTerm> to) {

            int arity = from.size();
            for (int i = 0; i < arity; i++) {
                ImmutableTerm fromTerm = from.get(i);
                ImmutableTerm toTerm = to.get(i);
                if (fromTerm instanceof Variable) {
                    if (!(toTerm instanceof VariableOrGroundTerm))
                        return false;

                    builder.extend((Variable)fromTerm, (VariableOrGroundTerm)toTerm);
                    // if we cannot find a match, terminate the process and return false
                    if (!builder.isValid())
                        return false;
                }
                else if (fromTerm instanceof Constant) {
                    // constants must match
                    if (!fromTerm.equals(toTerm))
                        return false;
                }
                else /*if (fromTerm instanceof ImmutableFunctionalTerm)*/ {
                    // the to term must also be a function
                    if (!(toTerm instanceof ImmutableFunctionalTerm))
                        return false;

                    ImmutableFunctionalTerm fromIFT = (ImmutableFunctionalTerm)fromTerm;
                    ImmutableFunctionalTerm toIFT = (ImmutableFunctionalTerm)toTerm;
                    if (fromIFT.getFunctionSymbol() != toIFT.getFunctionSymbol())
                        return false;

                    boolean result = extendHomomorphism(builder, fromIFT.getTerms(), toIFT.getTerms());
                    // if we cannot find a match, terminate the process and return false
                    if (!result)
                        return false;
                }
            }
            return true;
        }



        private boolean checkConditions(TMappingRule rule1, TMappingRule rule2, ImmutableSubstitution<VariableOrGroundTerm> toRule1) {
            if (rule2.getConditions().size() == 0)
                return true;
            if (rule2.getConditions().size() > 1 || rule1.getConditions().size() != 1)
                return false;

            return rule2.getConditions().get(0).stream()
                    .map(atom -> toRule1.applyToBooleanExpression(atom))
                    .allMatch(atom -> rule1.getConditions().get(0).contains(atom));
        }

    }
}
