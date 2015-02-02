package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.HashMap;
import fj.data.List;
import fj.data.Option;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.TypeProposal;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions.union;
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLift.computeTypePropagatingSubstitution;

/**
 * TODO: improve its name.
 *
 * See if turns into a substitution.
 *
 */
public class RuleLevelSubstitution {

    private final List<Function> unifiableDataAtoms;
    private final List<Function> filterAtoms;
    private final Unifier typingSubstitution;

    public RuleLevelSubstitution(CQIE rule, HashMap<Predicate, TypeProposal> childProposalIndex) throws TypeLift.MultiTypeException {

        List<Function> bodyAtoms = List.iterableList(rule.getBody());

        unifiableDataAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isDataFunction();
            }
        });
        filterAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isBooleanFunction();
            }
        });

        /**
         * TODO: explain
         */
        typingSubstitution = aggregateRuleAndProposals(Option.<Unifier>none(), unifiableDataAtoms, childProposalIndex);
    }

    /**
     * TODO: implement
     *
     */
    public List<Function> getUntypedDataAtoms() {
        return null;
    }

    /**
     * TODO: implement
     */
    public List<Function> getTypedFilterAtoms() {
        return null;
    }

    public Unifier getSubstitution() {
        return typingSubstitution;
    }


    /**
     * Tail-recursive method that "iterates" over the body atoms of a given rule defining the parent predicate.
     *
     * For a given body atom, tries to make the *union* (NOT composition) of the current substitution function with
     * the one deduced from the child proposal corresponding to the current atom.
     *
     * If some problems with a substitution function occur, throws a MultiTypeException.
     *
     */
    private static Unifier aggregateRuleAndProposals(final Option<Unifier> optionalSubstitutionFunction,
                                                     final List<Function> remainingBodyAtoms,
                                                     final HashMap<Predicate, TypeProposal> childProposalIndex) throws TypeLift.MultiTypeException {
        /**
         * Stop condition (no further body atom).
         */
        if (remainingBodyAtoms.isEmpty()) {
            if (optionalSubstitutionFunction.isNone()) {
                throw new IllegalArgumentException("Do not give a None substitution function with an empty list of rules");
            }
            return optionalSubstitutionFunction.some();
        }

        Function bodyAtom = remainingBodyAtoms.head();
        Option<TypeProposal> optionalChildProposal = childProposalIndex.get(bodyAtom.getFunctionSymbol());

        Option<Unifier> newOptionalSubstitutionFct;

        /**
         * If there is a child proposal corresponding to the current body atom,
         * computes a substitution function that propagates types.
         *
         * Then, makes the union of this substitution function with the previous one.
         *
         */
        if (optionalChildProposal.isSome()) {
            try {
                Unifier proposedSubstitutionFunction = computeTypePropagatingSubstitution(
                        bodyAtom, optionalChildProposal.some());

                if (optionalSubstitutionFunction.isNone()) {
                    newOptionalSubstitutionFct = Option.some(proposedSubstitutionFunction);
                }
                /**
                 * We do NOT consider the composition of the substitution functions (like during unifier)
                 * BUT THEIR UNION.
                 *
                 * Why? Because we want to apply a type only once, not multiple times.
                 *
                 * By composition "{ x/int(x) } o { x/int(x) } = { x/int(int(x) }" which is not what we want.
                 * With unions, "{ x/int(x) } U { x/int(x) } = { x/int(x) }".
                 *
                 * Throw a type propagation exception if the substitutions are conflicting.
                 * For example, this "union" does not a produce a function.
                 *
                 * " {x/int(x) } U { x/str(x) } "
                 *
                 */
                else {
                    newOptionalSubstitutionFct = Option.some(union(optionalSubstitutionFunction.some(),
                            proposedSubstitutionFunction));
                }
            }
            /**
             * Impossible to propagate type.
             * This happens when multiple types are proposed for this predicate.
             */
            catch(Substitutions.SubstitutionException e) {
                throw new TypeLift.MultiTypeException();
            }
        }
        /**
         * Otherwise, keeps the same proposed head.
         */
        else {
            newOptionalSubstitutionFct = optionalSubstitutionFunction;
        }

        /**
         * Tail recursion
         */
        return aggregateRuleAndProposals(newOptionalSubstitutionFct, remainingBodyAtoms.tail(), childProposalIndex);
    }

}
