package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import com.google.common.collect.ImmutableSet;
import fj.*;
import fj.data.Set;
import fj.data.Stream;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;

import java.util.UUID;

/**
 * For all URI templates.
 *
 * The tricky URI template case is responsible of the creation of this abstraction.
 *
 */
public class UriTemplateTypeProposal implements TypeProposal {

    private final Set<Variable> extraVariables;
    private final Function unifiableAtom;

    public UriTemplateTypeProposal(Function proposedAtom) {
        extraVariables = extractExtraVariables(proposedAtom);
        unifiableAtom = insertExtraVariables(proposedAtom, extraVariables);
    }

    @Override
    public Function getTypedAtom() {
        return unifiableAtom;
    }

    @Override
    public Predicate getPredicate() {
        return unifiableAtom.getFunctionSymbol();
    }

    /**
     * Adds new variables on the right side of the atom.
     * Makes sure these variable names are new (no conflict introduced).
     */
    @Override
    public P2<Function, java.util.Set<Variable>> convertIntoUnifiableAtom(Function bodyAtom, ImmutableSet<Variable> alreadyKnownRuleVariables) {
        Set<Variable> renamedExtraVariables = giveNonConflictingNamesToVariables(extraVariables, alreadyKnownRuleVariables);
        Function newAtom = insertExtraVariables(bodyAtom, renamedExtraVariables);
        return P.p(newAtom, (java.util.Set<Variable>) ImmutableSet.copyOf(renamedExtraVariables));
    }

    /**
     * Gives new names (randomly generated) to variables that are already known.
     */
    private static Set<Variable> giveNonConflictingNamesToVariables(Set<Variable> extraVariables,
                                                                    final java.util.Set<Variable> alreadyKnownRuleVariables) {
        final OBDADataFactory obdaDataFactory = OBDADataFactoryImpl.getInstance();

        return extraVariables.map(Ord.<Variable>hashEqualsOrd(), new F<Variable, Variable>() {
            @Override
            public Variable f(Variable variable) {
                // Keep the variable if not conflicting
                if (!alreadyKnownRuleVariables.contains(variable)) {
                    return variable;
                }
                // New variable
                return obdaDataFactory.getVariable("v" + UUID.randomUUID());
            }
        });
    }

    /**
     * "Extracts" the other variables from the URI template.
     */
    private static Set<Variable> extractExtraVariables(Function proposedAtom) {
        /**
         * Variables (possibility typed) that we found "at the top" of the atom (not in a URI template).
         */
        final Set<Variable> topVariables = extractTopVariables(proposedAtom);

        /**
         * All the URI templates terms using more than one variable
         */
        final Stream<Function> uriTemplateTerms = Stream.iterableStream(proposedAtom.getTerms()).filter(new F<Term, Boolean>() {
            @Override
            public Boolean f(Term term) {
                return TypeLiftTools.isURITemplate(term);
            }
        }).map(new F<Term, Function>() {
            @Override
            public Function f(Term term) {
                return (Function) term;
            }
        });


        /**
         * All the variables used by the URI templates.
         */
        final Stream<Variable> templateVariableStream = uriTemplateTerms.bind(new F<Function, Stream<Variable>>() {
            @Override
            public Stream<Variable> f(Function uriTemplateTerm) {
                Stream<Variable> variables = Stream.iterableStream(uriTemplateTerm.getTerms()).filter(new F<Term, Boolean>() {
                    @Override
                    public Boolean f(Term term) {
                        return term instanceof Variable;
                    }
                }).map(new F<Term, Variable>() {
                    @Override
                    public Variable f(Term term) {
                        return (Variable) term;
                    }
                });

                return variables;
            }
        });
        final Set<Variable> templateVariableSet = Set.iterableSet(Ord.<Variable>hashEqualsOrd(),
                templateVariableStream);

        /**
         * Extra variables: non-top variables found in the URI templates.
         */
        final Set<Variable> extraVariables = templateVariableSet.minus(topVariables);
        return extraVariables;
    }

    /**
     * Adds the extra variables at the right inside the atom.
     * Returns the new atom.
     */
    private static Function insertExtraVariables(Function atom, Set<Variable> extraVariables) {
        /**
         * SIDE EFFECT: adds the extra variables to the atom.
         * ---> Augments the effective arity.
         */
        // UGLY!!!! But necessary with the current API...
        Function newAtom = (Function) atom.clone();
        newAtom.getTerms().addAll(extraVariables.toStream().toCollection());

        return newAtom;
    }

    /**
     * By top variable we mean, variables are are direct sub-term of the given functional term.
     */
    private static Set<Variable> extractTopVariables(Function functionalTerm) {
        Stream<Term> termStream = Stream.iterableStream(functionalTerm.getTerms());
        Stream<Variable> variableStream = termStream.filter(new F<Term, Boolean>() {
            @Override
            public Boolean f(Term term) {
                // Untyped variable
                if (term instanceof Variable)
                    return true;
                // Typed variable
                if ((term instanceof Function)) {
                    Function functionalTerm = (Function) term;
                    if (!functionalTerm.isDataTypeFunction())
                        return false;
                    java.util.List<Term> subTerms = functionalTerm.getTerms();
                    if (subTerms.size() != 1) {
                        throw new RuntimeException("Datatype that has not an arity of 1 " + functionalTerm);
                    }
                    return subTerms.get(0) instanceof Variable;
                }
                return false;
            }
        }).map(new F<Term, Variable>() {
            @Override
            public Variable f(Term term) {
                // Untyped variable
                if (term instanceof Variable)
                    return (Variable) term;
                // Typed variable
                return (Variable) ((Function) term).getTerm(0);
            }
        });

        return Set.iterableSet(Ord.<Variable>hashEqualsOrd(), variableStream);
    }

}
