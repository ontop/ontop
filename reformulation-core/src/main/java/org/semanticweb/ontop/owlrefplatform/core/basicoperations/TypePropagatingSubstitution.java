package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import fj.F;
import fj.Ord;
import fj.P;
import fj.P2;
import fj.data.HashMap;
import fj.data.Stream;
import fj.data.TreeMap;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Type propagation utilities and inner methods.
 *
 * This class needs to be refactored:
 *   - Simplified (simpler than unification)
 *   - Find good names and good explanations.
 *
 * TODO: could we move its static methods to TypePropagationUtilities?
 *
 * TODO: try to apply the same pattern (substitution union) that inside TypeLift. This might finally become clear.
 */
public class TypePropagatingSubstitution extends SubstitutionImpl {

    /**
     * Default and normal constructor.
     */
    private TypePropagatingSubstitution() {
        super();
    }

    /**
     * When we want to define a custom unifier.
     *
     * Tip: make it immutable if you can.
     */
    private TypePropagatingSubstitution(Map<VariableImpl, Term> substitutions) {
        super(substitutions);
    }


    private static OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

    /**
     * TODO: understand, simplify and explain
     *
     * By contrast with unification, atom order matters.
     */
    public static Substitution createTypePropagatingSubstitution(Function firstAtom, Function secondAtom,
                                                                 Multimap<Predicate, Integer> multiTypedPredicateIndex) {

        /**
         * Basic condition: if predicates are different or their arity is different,
         * then no type propagation
         */

        if ((firstAtom.getArity() != secondAtom.getArity()
                || !firstAtom.getFunctionSymbol().equals(secondAtom.getFunctionSymbol()))) {
            return null;
        }

        Function clonedFirstAtom = (Function) firstAtom.clone();
        Function clonedSecondAtom = (Function) secondAtom.clone();

        int arity = firstAtom.getArity();
        // Mutable object
        TypePropagatingSubstitution substitution = new TypePropagatingSubstitution();

        /**
         * Computing the disagreement set
         */
        for (int termidx = 0; termidx < arity; termidx++) {

            // Checking if there are already substitutions calculated for the
            // current terms. If there are any, then we have to take the
            // substituted terms instead of the original ones.
            // ??????

            Term term1 = clonedFirstAtom.getTerm(termidx);
            Term term2 = clonedSecondAtom.getTerm(termidx);

            boolean changed = false;

            /**
             * We have two cases, unifying 'simple' terms, and unifying function terms.
             */
            if (!(term1 instanceof Function) || !(term2 instanceof Function)) {

                if (!substitution.composeForTypePropagation(term1, term2))
                    return null;

                changed = true;
            }
            /**
             * If both of them are function terms then we need to do some check in the inner terms
             */
            else {
                Function fterm1 = (Function) term1;
                Function fterm2 = (Function) term2;

                /**
                 * TODO: factorize it. Redundant with the first line of this method.
                 */
                if ((fterm1.getTerms().size() != fterm2.getTerms().size()) ||
                        !fterm1.getFunctionSymbol().equals(fterm2.getFunctionSymbol())) {
                    return null;
                }

                int innerarity = fterm1.getTerms().size();
                for (int innertermidx = 0; innertermidx < innerarity; innertermidx++) {
                    if (!substitution.composeAndPropagateType(fterm1, fterm2.getTerm(innertermidx), innertermidx,
                            multiTypedPredicateIndex))
                        return null;

                    changed = true;

                    /**
                     * Applying the newly computed substitution to the 'replacement' of
                     * the existing substitutions
                     */
                    SubstitutionUtilities.applySubstitution(fterm1, substitution, innertermidx + 1);
                    SubstitutionUtilities.applySubstitution(fterm2, substitution, innertermidx + 1);
                }
            }
            if (changed) {

                // Applying the newly computed substitution to the 'replacement' of
                // the existing substitutions
                SubstitutionUtilities.applySubstitution(clonedFirstAtom, substitution, termidx + 1);
                SubstitutionUtilities.applySubstitution(clonedSecondAtom, substitution, termidx + 1);
            }
        }
        return substitution;
    }

    /**
     * Apparently not a composition
     * TODO: rename it and clarify the connection with substitution union.
     */
    private boolean composeForTypePropagation(Term term1, Term term2) {
        Substitution s = createTypePropagatingSubstitution(term1, term2);

        boolean acceptSubstitution = putSubstitution(s);
        return acceptSubstitution;
    }

    /**
     * SIDE-EFFECT method. Not just a simple test.
     *
     * Use case: handle aggregates.
     * TODO: explain further.
     *
     * TODO: rename it because it is apparently not a valid composition.
     *
     */
    private boolean composeAndPropagateType(Function atom1, Term term2, int termIndex, Multimap<Predicate, Integer> multiTypedPredicateIndex) {
        Predicate functionSymbol1 = atom1.getFunctionSymbol();
        Term term1 = atom1.getTerm(termIndex);

        Substitution s;
        if (multiTypedPredicateIndex.containsKey(functionSymbol1) ){ // it is a problematic predicate regarding templates
            if (multiTypedPredicateIndex.get(functionSymbol1).contains(termIndex)){ //the term is the problematic one
                s = new NeutralSubstitution();
            } else{
                s = createTypePropagatingSubstitution(term1, term2);
            }
        }
        else{
            s = createTypePropagatingSubstitution(term1, term2);
        }

        boolean acceptSubstitution = putSubstitution(s);
        return acceptSubstitution;
    }

    /***
     * Inspired by Substitution.createUnifier (version 1).
     *
     * TODO: explain why the order of terms is important.
     *
     */
    private static Substitution createTypePropagatingSubstitution(Term term1, Term term2) {

        if (!(term1 instanceof VariableImpl) && !(term2 instanceof VariableImpl)) {

            // neither is a variable, impossible to unify unless the two terms are
            // equal, in which case there the substitution is empty
            if ((term1 instanceof AnonymousVariable) || (term2 instanceof AnonymousVariable)) {
                // this is questionable -- consider R(_,_) and R(c,c)
                return new NeutralSubstitution();
            }

            if ((term1 instanceof VariableImpl) || (term1 instanceof FunctionalTermImpl)
                    || (term1 instanceof ValueConstantImpl) || (term1 instanceof URIConstantImpl)) {

                // ROMAN: why is BNodeConstantImpl not mentioned?

                if (term1.equals(term2))
                    return new NeutralSubstitution();
                else
                    return null;
            }
            throw new RuntimeException("Exception comparing two terms, unknown term class. Terms: "
                    + term1 + ", " + term2 + " Classes: " + term1.getClass()
                    + ", " + term2.getClass());
        }

        // arranging the terms so that the first is always a variable
        VariableImpl t1;
        Term t2;
        if (term1 instanceof VariableImpl) {
            t1 = (VariableImpl)term1;
            t2 = term2;
        /**
         * NB: this condition differs from unification.
         */
        } else if (term1 instanceof Function) {
            P2<VariableImpl, Term> proposedTerms = getTypePropagatingSubstitution((Function) term1, term2);
            t1 = proposedTerms._1();
            t2 = proposedTerms._2();
        }
        /**
         * A substitution takes variables as input and returns terms.
         *
         * Thus, we should make sure the first term is variable.
         *
         * TODO: further explain why it is ok with type propagation (where order matters
         * in some places).
         */
        else {
            t1 = (VariableImpl)term2;
            t2 = term1;
        }

        // Undistinguished variables do not need a substitution,
        // the unifier knows about this
        if (t2 instanceof AnonymousVariable) { // ROMAN: no need in (t1 instanceof AnonymousVariable)
            return new NeutralSubstitution();
        }
        else if (t2 instanceof VariableImpl) {
            if (t1.equals(t2))   // ROMAN: no need in isEqual(t1, t2) -- both are proper variables
                return new NeutralSubstitution();
            else
                return new SingletonSubstitution(t1, t2);
        }
        else if ((t2 instanceof ValueConstantImpl) || (t2 instanceof URIConstantImpl)) {
            return new SingletonSubstitution(t1, t2);
        }
        else if (t2 instanceof FunctionalTermImpl) {
            return new SingletonSubstitution(t1, t2);
        }
        // this should never happen
        throw new RuntimeException("Unsupported unification case: " + term1 + " " + term2);
    }

    /**
     * TODO: explain and rename.
     */
    private static P2<VariableImpl, Term> getTypePropagatingSubstitution(Function functionalTerm1, Term term2) {
        Predicate functionSymbol1 = functionalTerm1.getFunctionSymbol();

        /**
         * Term1 is an aggregate --> looks inside its first sub-term.
         */
        if (functionSymbol1.isAggregationPredicate()) {
            Term subTerm = functionalTerm1.getTerm(0);

            /**
             * If its first sub-term is functional, it may be a data type.
             */
            if (subTerm instanceof Function) {
                Predicate subTermSymbol = ((Function) subTerm).getFunctionSymbol();

                /**
                 * If is a type, applies this type to the second term.
                 */
                if (subTermSymbol.isDataTypePredicate()) {
                    Term typedTerm2 = dataFactory.getFunction(subTermSymbol, term2);
                    return P.p((VariableImpl) term2, typedTerm2);
                }
            }

            /**
             * Term 1 is a data type.
             *
             * Then, we have to look if there is an aggregate inside.
             */
        } else if (functionSymbol1.isDataTypePredicate()) {
            Predicate type = functionSymbol1;
            Term subTerm = functionalTerm1.getTerm(0);

            //case where the aggregate is inside type, Count for instance
            /**
             * The sub-term is functional ...
             */
            if (subTerm instanceof Function) {
                functionSymbol1 = ((Function) subTerm).getFunctionSymbol();

                /**
                 *  ... and is an aggregate
                 */
                if (functionSymbol1.isAggregationPredicate()) {
                    Term subSubTerm = ((Function) subTerm).getTerm(0);

                    if (subSubTerm instanceof Function) {
                        Term typedTerm2 = dataFactory.getFunction(type, term2);
                        return P.p((VariableImpl)term2, typedTerm2);
                    }

                }
            }
        }
        /**
         * If term1 is a variable, at least term2 is a term.
         */
        return P.p((VariableImpl)term2, (Term)functionalTerm1);
    }

    /**
     * Derives a new substitution function that makes sure the replacing term use the replaced variable
     * when this term is functional.
     *
     * Functional terms with 0 or more than 1 variable are not added to the new substitution function.
     *
     * Returns the new substitution function.
     *
     * TODO: move it to TypePropagationUtilities
     */
    public static Substitution forceVariableReuse(Substitution initialSubstitutionFct) {
        Stream<P2<VariableImpl, Term>> unifierEntries = Stream.iterableStream(TreeMap.fromMutableMap(Ord.<VariableImpl>hashOrd(),
                initialSubstitutionFct.getMap()));

        Stream<P2<VariableImpl, Term>> newUnifierEntries = unifierEntries.filter(
                /**
                 * Filters (removes) functional terms with 0 or more than 1 variable
                 */
                new F<P2<VariableImpl, Term>, Boolean>() {
                    @Override
                    public Boolean f(P2<VariableImpl, Term> entry) {
                        Term term = entry._2();
                        if (term instanceof Function) {
                            if (term.getReferencedVariables().size() != 1)
                                return false;
                        }
                        return true;
                    }
                }).map(
                /**
                 * Transforms the unary functional terms so that they reuse
                 * the same variable.
                 *
                 * Others remain the same.
                 */
                new F<P2<VariableImpl, Term>, P2<VariableImpl, Term>>() {
                    @Override
                    public P2<VariableImpl, Term> f(P2<VariableImpl, Term> entry) {
                        VariableImpl variableToKeep = entry._1();
                        Term term = entry._2();

                        /**
                         * Transformation only concerns some functional terms.
                         */
                        if (term instanceof Function) {
                            VariableImpl variableToChange = (VariableImpl) term.getReferencedVariables().iterator().next();

                            /**
                             * When the two variables are not the same,
                             * makes a unification to update the functional term.
                             */
                            if (!variableToChange.equals(variableToKeep)) {
                                Substitution miniSubstitutionFct = new SubstitutionImpl(ImmutableMap.of(variableToChange, (Term) variableToKeep));

                                Function translatedFunctionalTerm = (Function) term.clone();
                                //Side-effect update
                                SubstitutionUtilities.applySubstitution(translatedFunctionalTerm, miniSubstitutionFct);

                                return P.p(variableToKeep, (Term) translatedFunctionalTerm);
                            }
                        }
                        /**
                         * No transformation.
                         */
                        return entry;
                    }
                });

        Substitution newSubstitutionFct = new TypePropagatingSubstitution(HashMap.from(newUnifierEntries).toMap());
        return newSubstitutionFct;
    }

    /**
     * If there is a non-neutral substitution,
     * puts its entries into the current unifier and returns true;
     *
     * Otherwise, does nothing and returns false;
     *
     * TODO: update. Do not update like this the map!!
     *
     */
    protected boolean putSubstitution(Substitution s) {
        if (s == null)
            return false;

        if (s instanceof NeutralSubstitution)
            return true;

        /**
         * Otherwise --> is a SingletonSubstitution
         *
         * TODO: refactor this code!.
         */

        if (!(s instanceof  SingletonSubstitution)) {
            throw new RuntimeException("Bug! A single substitution was excepted.");
        }
        SingletonSubstitution substitution = (SingletonSubstitution) s;


        // UGLY!! TODO: refactor this!
        Map<VariableImpl, Term> map = getMap();

        List<VariableImpl> forRemoval = new ArrayList<>();
        for (Map.Entry<VariableImpl,Term> entry : map.entrySet()) {
            VariableImpl v = entry.getKey();
            Term t = entry.getValue();
            if (substitution.getVariable().equals(t)) { // ROMAN: no need in isEqual(t, s.getVariable())
                if (v.equals(substitution.getTerm())) {  // ROMAN: no need in isEqual(v, s.getTerm())
                    // The substitution for the current variable has become
                    // trivial, e.g., x/x with the current composition. We
                    // remove it to keep only a non-trivial unifier
                    forRemoval.add(v);
                } else
                    map.put(v, substitution.getTerm());

            }
            else if (t instanceof FunctionalTermImpl) {
                FunctionalTermImpl fclone = (FunctionalTermImpl)t.clone();
                List<Term> innerTerms = fclone.getTerms();
                boolean innerchanges = false;
                // TODO this ways of changing inner terms in functions is not
                // optimal, modify

                for (int i = 0; i < innerTerms.size(); i++) {
                    Term innerTerm = innerTerms.get(i);

                    if (substitution.getVariable().equals(innerTerm)) { // ROMAN: no need in isEqual(innerTerm, s.getVariable())
                        fclone.getTerms().set(i, substitution.getTerm());
                        innerchanges = true;
                    }
                }
                if (innerchanges)
                    map.put(v, fclone);
            }
        }
        map.keySet().removeAll(forRemoval);
        map.put(substitution.getVariable(), substitution.getTerm());
        return true;
    }



}
