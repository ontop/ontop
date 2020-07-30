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
        return value.hashCode();
    }

    /**
     * This method directly refers to the notion of strict-equality.
     * Is under 2VL and treats the NULLs of strict-equalities as false.
     *
     * For DBConstants, term types are NOT CONSIDERED for this test. Only the lexical string matters.
     *
     * This tolerance initially comes from the fact that DBs like PostgreSQL offer almost-identical datatypes
     * such as SERIAL and INT4 over which one would like to join and over which foreign keys may hold.
     * For the sake of simplicity, Ontop treats these types as equivalent in strict-equality,
     * which enables FK-based optimization to be applied. Observes that the tiny technical difference between
     * SERIAL and INT4 is irrelevant for the scope of Ontop.
     *
     * Also observe that users CANNOT specify directly a strict-equality over DB terms.
     * Equalities in the user-provided mapping are first treated as "not yet typed" and are later transformed based
     * on their types. And for what regards SPARQL, it deals with RDF terms, not DB terms. This means
     * that strict-equalities between DB terms are produced internally, in situations where Ontop should be able to use
     * them in a meaningful manner.
     *
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof DBConstant &&
                    ((DBConstant) other).getValue().equals(this.value));
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
        if (otherTerm instanceof Constant) {
            return otherTerm.isNull()
                    ? IncrementalEvaluation.declareIsNull()
                    : equals(otherTerm)
                        ? IncrementalEvaluation.declareIsTrue()
                        : IncrementalEvaluation.declareIsFalse();
        }
        else
            return otherTerm.evaluateStrictEq(this, variableNullability);
    }
}
