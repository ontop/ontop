package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.datalog.AlgebraOperatorPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract implementation that can reasonably be shared because
 * an immutable and a crazy implementation.
 */
public abstract class AbstractFunctionalTermImpl implements Function {

    private Predicate functor;

    protected AbstractFunctionalTermImpl(Predicate functor) {
        this.functor = functor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Function)) {
            return false;
        }
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int getArity() {
        return functor.getArity();
    }


    @Override
    public Predicate getFunctionSymbol() {
        return functor;
    }

    @Override
    public void setPredicate(Predicate predicate) {
        this.functor = predicate;
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> variables = new HashSet<>();
        for (Term t : getTerms()) {
            TermUtils.addReferencedVariablesTo(variables, t);
        }
        return variables;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(functor.toString());
        sb.append("(");
        boolean separator = false;
        for (Term innerTerm : getTerms()) {
            if (separator) {
                sb.append(",");
            }
            sb.append(innerTerm.toString());
            separator = true;
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Check whether the function contains a particular term argument or not.
     *
     * @param t the term in question.
     * @return true if the function contains the term, or false otherwise.
     */
    @Override
    public boolean containsTerm(Term t) {
        List<Term> terms = getTerms();
        for (int i = 0; i < terms.size(); i++) {
            Term t2 = terms.get(i);
            if (t2.equals(t))
                return true;
        }
        return false;
    }

    @Override
    public Function clone() {
        throw new RuntimeException("MUST be implemented by concrete sub-classes. " +
                "Added because required by the compiler");
    }


    @Override
    public boolean isDataFunction() {
        return (!(isOperation() || isAlgebraFunction() || isDataTypeFunction()));
    }

    @Override
    public boolean isOperation() {
        return functor instanceof OperationPredicate;
    }

    @Override
    public boolean isAlgebraFunction() {
        return functor instanceof AlgebraOperatorPredicate;
    }

    @Override
    public boolean isDataTypeFunction() {
        return functor instanceof DatatypePredicate;
    }
}
