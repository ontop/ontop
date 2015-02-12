package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ImmutableSet;
import fj.F;
import fj.P2;
import fj.data.*;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.CQIEImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;

import java.util.ArrayList;

import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitutions.union;
import static org.semanticweb.ontop.owlrefplatform.core.unfolding.TypeLiftTools.*;

/**
 * Implementation making the following assumption:
 *   - Rules corresponds to conjunctive queries (no left-join)
 *
 */
public class RuleLevelProposalImpl implements RuleLevelProposal {

    private final Unifier typingSubstitution;
    private final CQIE typedRule;
    private final TypeProposal typeProposal;

    /**
     * Computes the substitution and the typed rule.
     *
     * May throw a MultiTypeException
     */
    public RuleLevelProposalImpl(CQIE initialRule, HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {

        /**
         * Only direct atoms (UCQ assumption: left-joins are not supported)
         */
        List<Function> bodyAtoms = List.iterableList(initialRule.getBody());

        List<Function> bodyDataAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isDataFunction();
            }
        });
        List<Function> filterAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return atom.isBooleanFunction();
            }
        });

        /**
         * Excludes joins and left joins but consider group.
         * TODO: this filtering test is weak. Improve it.
         */
        List<Function> nonCompositeAlgebraAtoms = bodyAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                if (!atom.isAlgebraFunction())
                    return false;
                Predicate predicate = atom.getFunctionSymbol();
                if (predicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN) || predicate.equals(OBDAVocabulary.SPARQL_JOIN))
                    return false;
                return true;
            }
        });

        /**
         * Extends the body data atoms so that are compatible with the typed child head for computing
         * a type propagation substitution.
         */
        List<Function> extendedBodyDataAtoms = computeExtendedBodyDataAtoms(bodyDataAtoms, childProposalIndex);

        /**
         * Computes the type propagating substitution.
         */
        typingSubstitution = aggregateRuleAndProposals(extendedBodyDataAtoms, childProposalIndex);

        /**
         * TODO: Only works for UCQs (Left-join hacky notation is not supported)
         */
        typedRule = constructTypedRule(initialRule, typingSubstitution, extendedBodyDataAtoms, filterAtoms, nonCompositeAlgebraAtoms);

        /**
         * Derives the type proposal
         */
        typeProposal = makeTypeProposal(typedRule, typingSubstitution);

    }

    /**
     * Converts each data atom into an unifiable atom thanks to its corresponding type proposal.
     * If no type proposal corresponds to a data atom, it means it is already unifiable.
     *
     * Some of these conversions may introduce new non-conflicting variables.
     *
     */
    private static List<Function> computeExtendedBodyDataAtoms(final List<Function> dataAtoms,
                                                               final HashMap<Predicate, PredicateLevelProposal> childProposalIndex) {

        /**
         * All the variables are supposed to be present in the data atoms (safe Datalog rules).
         *
         * Append-only Set.
         */
        final java.util.Set<Variable> alreadyKnownRuleVariables = extractVariables(dataAtoms);

        return dataAtoms.map(new F<Function, Function>() {
            @Override
            public Function f(Function atom) {
                Option<PredicateLevelProposal> optionalChildPredProposal = childProposalIndex.get(atom.getFunctionSymbol());
                /**
                 * No child proposal --> return the original atom.
                 */
                if (optionalChildPredProposal.isNone())
                    return atom;

                /**
                 * Converts into an unifiable atom thanks to the type proposal.
                 *
                 * If new variables are created, they are added to the tracking set.
                 *
                 */
                TypeProposal childTypeProposal = optionalChildPredProposal.some().getTypeProposal();
                P2<Function, java.util.Set<Variable>> newAtomAndVariables = childTypeProposal.convertIntoExtendedAtom(atom,
                        ImmutableSet.copyOf(alreadyKnownRuleVariables));

                // Appends new variables
                alreadyKnownRuleVariables.addAll(newAtomAndVariables._2());

                Function unifiableAtom = newAtomAndVariables._1();
                return unifiableAtom;
            }
        });
    }

    @Override
    public Unifier getTypingSubstitution() {
        return typingSubstitution;
    }

    @Override
    public CQIE getTypedRule() {
        return typedRule;
    }

    @Override
    public CQIE getDetypedRule() {
        Function extendedTypedHead =  typeProposal.getExtendedTypedAtom();
        Function detypedHead = removeTypeFromAtom(extendedTypedHead);

        CQIE detypedRule = typedRule.clone();
        detypedRule.updateHead(detypedHead);
        return detypedRule;
    }


    /**
     * Entry point for the homonym tail-recursive function.
     */
    private static Unifier aggregateRuleAndProposals(final List<Function> extendedBodyDataAtoms,
                                                     final HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        return aggregateRuleAndProposals(Option.<Unifier>none(), extendedBodyDataAtoms, childProposalIndex);
    }


    /**
     * Tail-recursive function that "iterates" over the body atoms of a given rule defining the parent predicate.
     *
     * For a given body atom, tries to make the *union* (NOT composition) of the current substitution function with
     * the one deduced from the child proposal corresponding to the current atom.
     *
     * If some problems with a substitution function occur, throws a MultiTypeException.
     *
     */
    private static Unifier aggregateRuleAndProposals(final Option<Unifier> optionalSubstitution,
                                                     final List<Function> remainingBodyDataAtoms,
                                                     final HashMap<Predicate, PredicateLevelProposal> childProposalIndex)
            throws TypeLiftTools.MultiTypeException {
        /**
         * Stop condition (no further body data atom).
         */
        if (remainingBodyDataAtoms.isEmpty()) {
            /**
             * If no child proposal corresponds to the body atoms, no substitution is created.
             * --> Returns an empty substitution.
             */
            if (optionalSubstitution.isNone()) {
                // Empty substitution
                return new Unifier();
            }
            return optionalSubstitution.some();
        }

        Function bodyAtom = remainingBodyDataAtoms.head();
        Option<PredicateLevelProposal> optionalChildProposal = childProposalIndex.get(bodyAtom.getFunctionSymbol());

        Option<Unifier> newOptionalSubstitution;

        /**
         * If there is a child proposal corresponding to the current body atom,
         * computes a substitution function that propagates types.
         *
         * Then, makes the union of this substitution function with the previous one.
         *
         */
        if (optionalChildProposal.isSome()) {
            try {
                Unifier proposedSubstitution = computeTypePropagatingSubstitution(bodyAtom,
                        optionalChildProposal.some().getTypeProposal());

                if (optionalSubstitution.isNone()) {
                    newOptionalSubstitution = Option.some(proposedSubstitution);
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
                    newOptionalSubstitution = Option.some(union(optionalSubstitution.some(),
                            proposedSubstitution));
                }
            }
            /**
             * Impossible to propagate type.
             * This happens when multiple types are proposed for this predicate.
             */
            catch(Substitutions.SubstitutionException e) {
                throw new TypeLiftTools.MultiTypeException();
            }
        }
        /**
         * Otherwise, keeps the same proposed head.
         */
        else {
            newOptionalSubstitution = optionalSubstitution;
        }

        /**
         * Tail recursion
         */
        return aggregateRuleAndProposals(newOptionalSubstitution, remainingBodyDataAtoms.tail(), childProposalIndex);
    }

    /**
     * Rebuilds a Datalog rule from the data and filter atoms and the typing substitution.
     *
     * Note that it only constructs Conjunctive Queries!
     *
     */
    private static CQIE constructTypedRule(CQIE initialRule, Unifier typingSubstitution, List<Function> extendedDataAtoms, List<Function> untypedFilterAtoms,
                                           List<Function> untypedNonCompositeAlgebraAtoms) {

        /**
         * Derives a typed head by applying the substitution
         */
        Function typedHead = (Function) initialRule.getHead().clone();
        //SIDE-EFFECT!!!
        UnifierUtilities.applyUnifier(typedHead, typingSubstitution);

        /**
         * Types filter and non composite algebra atoms
         */
        List<Function> typedBodyAtoms = typeAtoms(typingSubstitution, untypedFilterAtoms.append(untypedNonCompositeAlgebraAtoms));

        /**
         * Removes the variables that correspond to URI templates
         */
        List<Function> newUntypedDataAtoms = removeURITemplates(extendedDataAtoms, typingSubstitution);

        /**
         * Concats the three types of body atoms
         */
        List<Function> allBodyAtoms = newUntypedDataAtoms.append(typedBodyAtoms);
        java.util.List<Function> typedRuleBody = new ArrayList<>(allBodyAtoms.toCollection());


        CQIE typedRule = OBDADataFactoryImpl.getInstance().getCQIE(typedHead, typedRuleBody);
        return typedRule;
    }

    /**
     * Applies the typing substitution to a list of atoms.
     */
    private static List<Function> typeAtoms(final Unifier typingSubstitution, final List<Function> atoms) {
        return atoms.map(new F<Function, Function>() {
            @Override
            public Function f(Function atom) {
                Function newAtom = (Function) atom.clone();
                // SIDE-EFFECT: makes the new head typed.
                UnifierUtilities.applyUnifier(newAtom, typingSubstitution);
                return newAtom;
            }
        });
    }

    private static List<Function> removeURITemplates(List<Function> extendedDataAtoms, final Unifier typingSubstitution) {
        return extendedDataAtoms.map(new F<Function, Function>() {
            @Override
            public Function f(Function atom) {
                Function typedAtom = (Function) atom.clone();
                UnifierUtilities.applyUnifier(typedAtom, typingSubstitution);

                /**
                 * Untyping removes URI template terms.
                 */
                Function untypedAtom = TypeLiftTools.removeTypeFromAtom(typedAtom);
                return untypedAtom;
            }
        });
    }

    /**
     * Returns the set of variables found in the atoms.
     */
    private static java.util.Set<Variable> extractVariables(List<Function> atoms) {
        List<Variable> variableList = atoms.bind(new F<Function, List<Variable>>() {
            @Override
            public List<Variable> f(Function atom) {
                return List.iterableList(atom.getVariables());
            }
        });

        return new java.util.HashSet(variableList.toCollection());
    }

}
