package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.Ord;
import fj.data.List;
import fj.data.Set;
import fj.data.Stream;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;

/**
 * For URI templates using more than one variable.
 *
 * TODO: implement it
 */
public class MultiVariateUriTemplateTypeProposal extends TypeProposalImpl {

    private final Function nonTypePropagatedAtom;

    /**
     * TODO: update it
     */
    public MultiVariateUriTemplateTypeProposal(Function nonTypePropagatedAtom) {
        super(computeUnifiableAtom(nonTypePropagatedAtom));

        this.nonTypePropagatedAtom = nonTypePropagatedAtom;
    }

    /**
     * Basically, augments the arity. "Extracts" the other variables from the URI template.
     */
    private static Function computeUnifiableAtom(final Function nonTypePropagatedAtom) {
        final Function newAtom = (Function) nonTypePropagatedAtom.clone();

        final Set<Variable> topVariables = extractTopVariables(newAtom);

        /**
         * All the URI templates terms using more than one variable
         */
        final Stream<Function> uriTemplateTerms = Stream.iterableStream(newAtom.getTerms()).filter(new F<Term, Boolean>() {
            @Override
            public Boolean f(Term term) {
                return TypeLift.isMultiVariateURITemplate(term);
            }
        }).map(new F<Term, Function>() {
            @Override
            public Function f(Term term) {
                return (Function) term;
            }
        });


        /**
         *All the variables used by the URI templates except the first ones.
         */
        final Stream<Variable> nonFirstTemplateVariableStream = uriTemplateTerms.bind(new F<Function, Stream<Variable>>() {
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

                /**
                 * We do not consider the first variable (associated to the URITemplate).
                 */
                return variables.tail()._1();
            }
        });
        final Set<Variable> nonFirstTemplateVariableSet = Set.iterableSet(Ord.<Variable>hashEqualsOrd(),
                nonFirstTemplateVariableStream);

        /**
         * Variables to add to the atom: new not-first variables found in the URI templates
         */
        final Set<Variable> extraVariables = nonFirstTemplateVariableSet.minus(topVariables);


        /**
         * SIDE EFFECT: adds the extra variables to the atom.
         * ---> Augments the effective arity.
         */
        // UGLY!!!! But necessary with the current API...
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
                return term instanceof Variable;
            }
        }).map(new F<Term, Variable>() {
            @Override
            public Variable f(Term term) {
                return (Variable) term;
            }
        });

        return Set.iterableSet(Ord.<Variable>hashEqualsOrd(), variableStream);
    }

    /**
     * TODO: implement it!
     */
    @Override
    public List<CQIE> applyType(List<CQIE> initialRules) {
        return null;
    }

    /**
     * TODO: implement it!
     */
    @Override
    public List<CQIE> removeType(List<CQIE> initialRules) {
        return null;
    }

    /**
     * TODO: implement it!
     */
    @Override
    public List<CQIE> propagateChildArityChangeToBodies(List<CQIE> initialRules) {
        return null;
    }
}
