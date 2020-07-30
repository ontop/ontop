package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.stream.Stream;

public class RDFTermTypeConstantImpl extends AbstractNonNullConstant implements RDFTermTypeConstant {

    private final RDFTermType rdfTermType;
    private final MetaRDFTermType metaType;

    protected RDFTermTypeConstantImpl(RDFTermType rdfTermType, MetaRDFTermType metaType) {
        this.rdfTermType = rdfTermType;
        this.metaType = metaType;
    }

    @Override
    public MetaRDFTermType getType() {
        return metaType;
    }

    @Override
    public RDFTermType getRDFTermType() {
        return rdfTermType;
    }

    @Override
    public String getValue() {
        return rdfTermType.toString();
    }

    @Override
    public boolean isGround() {
        return true;
    }

    @Override
    public Stream<Variable> getVariableStream() {
        return Stream.empty();
    }

    @Override
    public int hashCode() {
        return rdfTermType.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof RDFTermTypeConstant)
                && rdfTermType.equals(((RDFTermTypeConstant) other).getRDFTermType());
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
        if (otherTerm instanceof Constant) {
            if (((Constant) otherTerm).isNull())
                return IncrementalEvaluation.declareIsNull();
            return equals(otherTerm)
                    ? IncrementalEvaluation.declareIsTrue()
                    : IncrementalEvaluation.declareIsFalse();
        }
        else
            return otherTerm.evaluateStrictEq(this, variableNullability);
    }
}
