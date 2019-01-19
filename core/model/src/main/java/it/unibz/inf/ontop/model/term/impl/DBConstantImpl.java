package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.stream.Stream;

public class DBConstantImpl extends AbstractNonNullConstant implements DBConstant {
    private final String value;
    private final DBTermType termType;

    public DBConstantImpl(String value, DBTermType termType) {
        this.value = value;
        this.termType = termType;
    }

    @Override
    public DBTermType getType() {
        return termType;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isGround() {
        return true;
    }

    @Override
    public Stream<Variable> getVariableStream() {
        return Stream.empty();
    }

    /**
     * TODO: should we print differently?
     */
    @Override
    public String toString() {
        return "\"" + value + "\"^^" + termType;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DBConstant) {
            DBConstant otherConstant = (DBConstant) other;
            return otherConstant.getType().equals(termType)
                    && otherConstant.getValue().equals(value);
        }
        else
            return false;
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
        if (otherTerm instanceof Constant) {
            return ((Constant) otherTerm).isNull()
                    ? IncrementalEvaluation.declareIsNull()
                    : equals(otherTerm)
                        ? IncrementalEvaluation.declareIsTrue()
                        : IncrementalEvaluation.declareIsFalse();
        }
        else
            return otherTerm.evaluateStrictEq(this, variableNullability);
    }

    @Override
    public Term clone() {
        return new DBConstantImpl(value, termType);
    }
}
