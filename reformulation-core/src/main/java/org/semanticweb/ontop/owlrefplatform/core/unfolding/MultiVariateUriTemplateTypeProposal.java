package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.Effect;
import fj.F;
import fj.Ord;
import fj.P2;
import fj.data.List;
import fj.data.Set;
import fj.data.Stream;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;

import java.util.UUID;

/**
 * For URI templates with more than one variable.
 *
 * TODO: implement it
 * All URI templates.
 *
 */
public class MultiVariateUriTemplateTypeProposal extends TypeProposalImpl {

    /**
     * Indexes in the proposed atom.
     * They remain valid in the unifiable atom (since new variables are added at the right).
     */
    private final List<Integer> uriTemplateIndexesInReverseOrder;

    private final Set<Variable> extraVariables;
    private final Function unifiableAtom;

    public MultiVariateUriTemplateTypeProposal(Function proposedAtom) {
        super(proposedAtom);

        uriTemplateIndexesInReverseOrder = locateMultiVariateURITemplates(proposedAtom);
        extraVariables = extractExtraVariables(proposedAtom);
        unifiableAtom = insertExtraVariables(proposedAtom, extraVariables);
    }

    @Override
    public Function getUnifiableAtom() {
        return unifiableAtom;
    }


    /**
     * TODO: remove
     */
    @Deprecated
    @Override
    public List<CQIE> propagateChildArityChangeToBodies(List<CQIE> initialRules) {
        final Predicate predicate = getPredicate();

        return initialRules.map(new F<CQIE, CQIE>() {
            @Override
            public CQIE f(CQIE initialRule) {
                final CQIE newRule = initialRule.clone();
                /**
                 * Updates the body atom(s?) corresponding to this type proposal.
                 */
                Stream.iterableStream(newRule.getBody()).foreach(new Effect<Function>() {
                    @Override
                    public void e(Function atom) {
                        if (atom.getFunctionSymbol().equals(predicate)) {
                            final java.util.List<Term> subTerms = atom.getTerms();

                            /**
                             * Removes the variables corresponding to URI templates.
                             */
                            uriTemplateIndexesInReverseOrder.foreach(new Effect<Integer>() {
                                @Override
                                public void e(Integer index) {
                                    // UGLY!!! But imposed by the current API....
                                    subTerms.remove(index);
                                }
                            });
                        }
                    }
                });
                return newRule;
            }
        });
    }


    /**
     * Adds new variables at the right in the atom.
     * Makes sure these variable names are new (no conflict introduced).
     */
    @Override
    public Function prepareBodyAtomForUnification(Function bodyAtom, java.util.Set<Variable> alreadyKnownRuleVariables) {
        Set<Variable> renamedExtraVariables = giveNonConflictingNamesToVariables(extraVariables, alreadyKnownRuleVariables);
        Function newAtom = insertExtraVariables(bodyAtom, renamedExtraVariables);
        return newAtom;
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

    private static List<Integer> locateMultiVariateURITemplates(Function proposedAtom) {
        return Stream.iterableStream(proposedAtom.getTerms()).zipIndex().filter(new F<P2<Term, Integer>, Boolean>() {
            @Override
            public Boolean f(P2<Term, Integer> pair) {
                return TypeLift.isURITemplate(pair._1());
            }
        }).map(new F<P2<Term, Integer>, Integer>() {
            @Override
            public Integer f(P2<Term, Integer> pair) {
                return pair._2();
            }
        }).toList().reverse();
    }

    /**
     * "Extracts" the other variables from the URI template
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
                return TypeLift.isURITemplate(term);
            }
        }).map(new F<Term, Function>() {
            @Override
            public Function f(Term term) {
                return (Function) term;
            }
        });


        /**
         * All the variables used by the URI templates .
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
         * : non-top variables found in the URI templates
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
