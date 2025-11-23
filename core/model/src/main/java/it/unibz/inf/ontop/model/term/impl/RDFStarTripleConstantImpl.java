package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.Objects;
import java.util.stream.Stream;

public class RDFStarTripleConstantImpl extends AbstractNonNullConstant implements RDFStarTripleConstant {

    private final RDFConstant subject;
    private final IRIConstant predicate;
    private final RDFConstant object;
    private final RDFTermType tripleType;
    private final String lexicalValue;

    public RDFStarTripleConstantImpl(RDFConstant subject, IRIConstant predicate, RDFConstant object,
                                     RDFTermType tripleType, String lexicalValue) {
        this.subject = Objects.requireNonNull(subject);
        this.predicate = Objects.requireNonNull(predicate);
        this.object = Objects.requireNonNull(object);
        this.tripleType = Objects.requireNonNull(tripleType);
        this.lexicalValue = Objects.requireNonNull(lexicalValue);
    }

    @Override
    public RDFConstant getSubject() {
        return subject;
    }

    @Override
    public IRIConstant getPredicate() {
        return predicate;
    }

    @Override
    public RDFConstant getObject() {
        return object;
    }

    @Override
    public Stream<Variable> getVariableStream() {
        return Stream.of();
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
        if (otherTerm instanceof RDFStarTripleConstant) {
            return equals(otherTerm)
                    ? IncrementalEvaluation.declareIsTrue()
                    : IncrementalEvaluation.declareIsFalse();
        }
        else if (otherTerm instanceof Constant) {
            return otherTerm.isNull()
                    ? IncrementalEvaluation.declareIsNull()
                    : IncrementalEvaluation.declareIsFalse();
        }
        return otherTerm.evaluateStrictEq(this, variableNullability);
    }

    @Override
    public RDFTermType getType() {
        return tripleType;
    }

    @Override
    public String getValue() {
        return lexicalValue;
    }

    @Override
    public String toString() {
        return lexicalValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof RDFStarTripleConstant))
            return false;

        RDFStarTripleConstant other = (RDFStarTripleConstant) obj;
        return subject.equals(other.getSubject())
                && predicate.equals(other.getPredicate())
                && object.equals(other.getObject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, predicate, object);
    }
}
