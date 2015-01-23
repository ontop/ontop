package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.F;
import fj.data.Array;
import fj.data.List;
import fj.data.Stream;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

        final java.util.List<Variable> topVariables = extractTopVariables(newAtom);
        final java.util.List<Variable> variablesToAdd = new ArrayList<>();

        /**
         * For all the URI templates using more than one variable...
         */
        for(Term subTerm : newAtom.getTerms()) {
            if (TypeLift.isMultiVariateURITemplate(subTerm)) {

                /**
                 * Template string and the first variable are ignored.
                 * ---> We only consider the other variables
                 */
                java.util.List<Term> templateSubTerms = ((Function) subTerm).getTerms();
                for(int i = 2; i < templateSubTerms.size(); i++ ) {
                    Term templateSubTerm = templateSubTerms.get(i);

                    // MUST be a variable
                    if (!(templateSubTerm instanceof Variable)) {
                        throw new RuntimeException("Inconsistent URI template: " + subTerm.toString());
                    }

                    Variable variable = (Variable) templateSubTerm;
                    if   (topVariables.contains(variable)) {
                        //TODO: continue
                    }
                }
            }
        }

        //TODO: continue


        return newAtom;
    }


    /**
     * By top variable we mean, variables are are direct sub-term of the given functional term.
     */
    private static java.util.List<Variable> extractTopVariables(Function functionalTerm) {
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

        return new ArrayList<Variable>(variableStream.toCollection());
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
