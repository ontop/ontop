package org.semanticweb.ontop.model.impl;

import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;

import java.util.HashSet;
import java.util.LinkedHashSet;
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
        HashSet<Variable> variables = new LinkedHashSet<Variable>();
        for (Term t : getTerms()) {
            for (Variable v : t.getReferencedVariables())
                variables.add(v);
        }
        return variables;
    }

    @Override
    public Set<Variable> getReferencedVariables() {
        Set<Variable> vars = new LinkedHashSet<Variable>();
        for (Term t : getTerms()) {
            for (Variable v : t.getReferencedVariables())
                vars.add(v);
        }
        return vars;
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
     * @param t
     *            the term in question.
     * @return true if the function contains the term, or false otherwise.
     */
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
        return this.functor.isDataPredicate();
    }

    @Override
    public boolean isBooleanFunction() {
        return this.functor.isBooleanPredicate();
    }

    @Override
    public boolean isAlgebraFunction() {
        return this.functor.isAlgebraPredicate();
    }

    @Override
    public boolean isArithmeticFunction() {
        return this.functor.isArithmeticPredicate();
    }

    @Override
    public boolean isDataTypeFunction() {
        return this.functor.isDataTypePredicate();
    }
}
