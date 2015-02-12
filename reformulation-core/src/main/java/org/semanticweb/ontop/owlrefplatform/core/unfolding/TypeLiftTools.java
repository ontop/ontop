package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fj.Effect;
import fj.F;
import fj.P;
import fj.P2;
import fj.data.HashMap;
import fj.data.List;
import fj.data.Option;
import fj.data.Stream;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitution;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.SubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.SubstitutionUtilities;

import java.util.ArrayList;
import java.util.Set;

/**
 * TODO: explain
 */
public class TypeLiftTools {

    /**
     * Thrown after receiving an SubstitutionException.
     *
     * This indicates that the predicate for which the type propagation
     * has been tried should be considered as multi-typed.
     */
    protected static class MultiTypeException extends Exception {
    }


    /**
     * TODO: describe
     *
     */
    public static boolean containsURITemplate(Function atom) {
        for(Term term : atom.getTerms()) {
            if (isURITemplate(term))
                return true;
        }
        return false;
    }


    /**
     * Uri-templates.
     */
    public static boolean isURITemplate(Term term) {
        if (!(term instanceof Function))
            return false;

        Function functionalTerm = (Function) term;

        if (functionalTerm.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI)) {
            /**
             * Distinguish normal URI types from URI templates
             */
            return functionalTerm.getTerms().size() > 1;
        }
        return false;
    }


    /**
     * Looks for predicates are not yet declared as multi-typed (while they should).
     *
     * This tests relies on the ability of rules defining one predicate to be unified.
     *
     * This class strongly relies on the assumption that the multi-typed predicate index is complete.
     * This method offers such a protection against non-detections by previous components.
     */
    protected static Multimap<Predicate, Integer> updateMultiTypedFunctionSymbolIndex(final TreeBasedDatalogProgram initialDatalogProgram,
                                                                                      final Multimap<Predicate, Integer> multiTypedFunctionSymbolIndex) {
        // Mutable index (may be updated)
        final Multimap<Predicate, Integer> newIndex = ArrayListMultimap.create(multiTypedFunctionSymbolIndex);

        final Stream<P2<Predicate, List<CQIE>>> ruleEntries = Stream.iterableStream(initialDatalogProgram.getRuleTree());
        /**
         * Applies the following effect on each rule entry:
         *   If the predicate has not been declared as multi-typed, checks if it really is.
         *
         *   When a false negative is detected, adds it to the index (side-effect).
         */
        ruleEntries.foreach(new Effect<P2<Predicate, List<CQIE>>>() {
            @Override
            public void e(P2<Predicate, List<CQIE>> ruleEntry) {
                Predicate predicate = ruleEntry._1();
                if (multiTypedFunctionSymbolIndex.containsKey(predicate))
                    return;

                List<CQIE> rules = ruleEntry._2();
                if (isMultiTypedPredicate(rules)) {
                    // TODO: Is there some usage for this count?
                    int count = 1;
                    newIndex.put(predicate, count);
                }
            }
        });
        return newIndex;
    }

    /**
     * Tests if the rules defining one predicate cannot be unified
     * because they have different types.
     *
     * Returns true if the predicate is detected as multi-typed
     * (or some of its rules are not supported).
     *
     * TODO: make a clear distinction between multi-typed and having some unsupported rules.
     *
     */
    private static boolean isMultiTypedPredicate(List<CQIE> predicateDefinitionRules) {
        /**
         * TODO: after removing isRuleSupportedForTypeLift, test len(predicateDefRules <= 1
         */
        if (predicateDefinitionRules.isEmpty())
            return false;

        CQIE currentRule = predicateDefinitionRules.head();

        /**
         * Checks restriction for the current rule:
         *  --> interpreted (abusively) as multi-typed
         */
        if (!isRuleSupportedForTypeLift(currentRule))
            return true;

        Function headFirstRule = currentRule.getHead();

        return isMultiTypedPredicate(constructTypeProposal(headFirstRule), predicateDefinitionRules.tail());
    }

    /**
     * Tail recursive sub-method that "iterates" over the rules.
     */
    private static boolean isMultiTypedPredicate(TypeProposal currentTypeProposal, List<CQIE> remainingRules) {
        if (remainingRules.isEmpty())
            return false;

        CQIE currentRule = remainingRules.head();

        /**
         * Checks restriction for the current rule
         * --> interpreted (abusively) as multi-typed
         */
        if (!isRuleSupportedForTypeLift(currentRule))
            return true;

        Function ruleHead = currentRule.getHead();
        try {
            Function newType = applyTypeProposal(ruleHead, currentTypeProposal);

            // Tail recursion
            return isMultiTypedPredicate(new BasicTypeProposal(newType), remainingRules.tail());
            /**
             * Multi-type problem detected
             */
        } catch (SubstitutionUtilities.SubstitutionException e) {
            return true;
        }
    }

    /**
     * Current restriction: Use of meta-atoms (left-joins, etc.)
     */
    private static boolean isRuleSupportedForTypeLift(CQIE rule) {

        /**
         * Checks the body atoms
         */
        boolean validBodyAtoms = Stream.iterableStream(rule.getBody()).forall(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                /**
                 * Join and Left join meta-predicates are not supported here (but group is...)
                 * (joins must have been unfolded so that its meta-predicate is not needed.).
                 */
                Predicate predicate = atom.getFunctionSymbol();
                if (predicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN) || predicate.equals(OBDAVocabulary.SPARQL_JOIN))
                    return false;
                return true;
            }
        });
        return validBodyAtoms;
    }

    /**
     * Propagates type from a typeProposal to one target atom.
     */
    public static Function applyTypeProposal(Function targetAtom, TypeProposal typeProposal) throws SubstitutionUtilities.SubstitutionException {
        Substitution substitution = computeTypePropagatingSubstitution(typeProposal.getExtendedTypedAtom(), targetAtom);

        // Mutable object
        Function newHead = (Function) targetAtom.clone();
        // Limited side-effect
        SubstitutionUtilities.applySubstitution(newHead, substitution);

        return newHead;
    }

    /**
     * Sometimes rule bodies contains algebra functions (e.g. left joins).
     * These should not be considered as atoms.
     *
     * These method makes sure only real (non algebra) atoms are returned.
     * Some of these atoms may be found inside algebra functions.
     *
     */
    public static List<Function> extractBodyAtoms(CQIE rule) {
        List<Function> directBody = List.iterableList(rule.getBody());

        return directBody.bind(new F<Function, List<Function>>() {
            @Override
            public List<Function> f(Function atom) {
                return extractAtoms(atom);
            }
        });
    }

    /**
     * Extracts real atoms from a functional term.
     *
     * If this functional term is not algebra, it is an atom and is
     * thus directly returned.
     *
     * Otherwise, looks for atoms recursively by looking
     * at the functional sub terms for the algebra function.
     *
     * Recursive function.
     *
     * TODO: Improvement: transform into a F() object.
     */
    private static List<Function> extractAtoms(Function atom) {
        /**
         * Normal case: not an algebra function (e.g. left join).
         */
        if (!atom.isAlgebraFunction()) {
            return List.cons(atom, List.<Function>nil());
        }

        /**
         * Sub-terms that are functional.
         */
        List<Function> subAtoms = List.iterableList(atom.getTerms()).filter(new F<Term, Boolean>() {
            @Override
            public Boolean f(Term term) {
                return term instanceof Function;
            }
        }).map(new F<Term, Function>() {
            @Override
            public Function f(Term term) {
                return (Function) term;
            }
        });

        /**
         * Recursive call over these sub-atoms.
         * The atoms they returned are then joined.
         * Their union is then returned.
         */
        return subAtoms.bind(new F<Function, List<Function>>() {
            @Override
            public List<Function> f(Function subAtom) {
                return extractAtoms(subAtom);
            }
        });

    }

    public static Function removeTypeFromAtom(Function atom) {
        List<Term> initialHeadTerms =  List.iterableList(atom.getTerms());

        /**
         * Computes untyped arguments for the head predicate.
         */
        List<Term> newHeadTerms = Option.somes(initialHeadTerms.map(new F<Term, Option<Term>>() {
            @Override
            public Option<Term> f(Term term) {
                return untypeTerm(term);
            }
        }));

        /**
         * Builds a new atom.
         */
        Function newAtom = (Function)atom.clone();
        newAtom.updateTerms(new ArrayList<>(newHeadTerms.toCollection()));
        return newAtom;
    }

    /**
     * Removes the type for a given term.
     * This method also deals with special cases that should not be untyped.
     *
     * Note that type removal only concern functional terms.
     * If the returned value is None, the term must be eliminated.
     *
     */
    public static Option<Term> untypeTerm(Term term) {
        /**
         * Types are assumed to functional terms.
         *
         * Other type of terms are not concerned.
         */
        if (!(term instanceof Function)) {
            return Option.some(term);
        }

        /**
         * Special case that should not be untyped:
         *   - Aggregates
         */
        if (DatalogUnfolder.detectAggregateInArgument(term))
            return Option.some(term);


        /**
         * Special case: URI templates --> to be removed.
         *
         */
        if (isURITemplate(term)) {
            return Option.none();
        }

        /**
         * Other functional terms are expected to be type
         * and to have an arity of 1.
         *
         * Raises an exception if it is not the case.
         */
        Function functionalTerm = (Function) term;
        java.util.List<Term> functionArguments = functionalTerm.getTerms();
        if (functionArguments.size() != 1) {
            throw new RuntimeException("Removing types of non-unary functional terms is not supported.");
        }
        return Option.some(functionArguments.get(0));
    }

    /**
     * Constructs a TypeProposal of the proper type.
     */
    public static TypeProposal constructTypeProposal(Function unextendedTypedAtom) {
        /**
         * Special case: multi-variate URI template.
         */
        if (containsURITemplate(unextendedTypedAtom)) {
            return new UriTemplateTypeProposal(unextendedTypedAtom);
        }
        /**
         * Default case
         */
        return new BasicTypeProposal(unextendedTypedAtom);
    }

    /**
     * Makes a TypeProposal by applying the substitution to the head of the rule.
     */
    public static TypeProposal makeTypeProposal(CQIE rule, Substitution substitution) {
        final Function unextendedTypeAtom = (Function) rule.getHead().clone();
        // Side-effect!
        SubstitutionUtilities.applySubstitution(unextendedTypeAtom, substitution);
        final TypeProposal newProposal = constructTypeProposal(unextendedTypeAtom);

        return newProposal;
    }

    /**
     * Assumption: the target atom is only composed of variables!
     *
     * TODO: split it!
     * TODO: comment it!
     */
    public static Substitution computeTypePropagatingSubstitution(final Function sourceAtom, final Function targetAtom) {
        java.util.List<Term> sourceTerms = sourceAtom.getTerms();
        java.util.List<Term> targetTerms = targetAtom.getTerms();

        if (sourceTerms.size() != targetTerms.size()) {
            return null;
        }

        List<VariableImpl> targetVariables = List.iterableList(targetTerms).map(new F<Term, VariableImpl>() {
            @Override
            public VariableImpl f(Term term) {
                if (!(term instanceof VariableImpl))
                    throw new IllegalArgumentException("The target atom must be only composed of variables: " + targetAtom);
                return (VariableImpl) term;
            }
        });


        /**
         * { Source variable --> target variable }
         */
        List<P2<Term, VariableImpl>> sourceTargetPairs = List.iterableList(sourceTerms).zip(targetVariables);
        HashMap<VariableImpl, Term> variableMappings = HashMap.from(Option.somes(sourceTargetPairs.map(
                new F<P2<Term, VariableImpl>, Option<P2<VariableImpl, Term>>>() {
            @Override
            public Option<P2<VariableImpl, Term>> f(P2<Term, VariableImpl> pair) {
                Term sourceTerm = pair._1();
                VariableImpl targetVariable = pair._2();

                Set<Variable> sourceVars = sourceTerm.getReferencedVariables();
                if (sourceVars.size() != 1)
                    return Option.none();

                return Option.some(P.p((VariableImpl) sourceVars.iterator().next(), (Term)targetVariable));
            }
        })));


        Substitution targetToSourceVarSubstitution = new SubstitutionImpl(variableMappings.toMap());

        Function renamedSourceAtom = (Function) sourceAtom.clone();
        //SIDE-EFFECT!
        SubstitutionUtilities.applySubstitution(renamedSourceAtom, targetToSourceVarSubstitution);


        /**
         *  { Target variable --> typed function }
         */
        List<P2<VariableImpl, Term>> targetSourceFunctionPairs = targetVariables.zip(List.iterableList(renamedSourceAtom.getTerms()));
        HashMap<VariableImpl, Term> variableToTypeMappings = HashMap.from(targetSourceFunctionPairs.filter(new F<P2<VariableImpl, Term>, Boolean>() {
            @Override
            public Boolean f(P2<VariableImpl, Term> pair) {
                return pair._2() instanceof Function;
            }
        }));

        /**
         * TODO: clean type proposal!
         */

        return new SubstitutionImpl(variableToTypeMappings.toMap());
    }

    /**
     * TODO: move some code here!
     */
    private Substitution computeBasicVariableSubstitution(final Function sourceAtom, final Function targetAtom) {
        return null;
    }

}
