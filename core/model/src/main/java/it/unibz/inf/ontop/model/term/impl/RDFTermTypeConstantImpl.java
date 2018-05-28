package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.stream.Stream;

public class RDFTermTypeConstantImpl implements RDFTermTypeConstant {

    private final RDFTermType termType;

    protected RDFTermTypeConstantImpl(RDFTermType rdfTermType) {
        termType = rdfTermType;
    }

    @Override
    public RDFTermType getType() {
        return termType;
    }

    @Override
    public String getValue() {
        return termType.toString();
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
        return termType.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof RDFTermTypeConstant)
                && termType.equals(((RDFTermTypeConstant) other).getType());
    }

    @Override
    public String toString() {
        return getValue();
    }

    /**
     * TODO: remove it
     */
    @Deprecated
    @Override
    public Term clone() {
        return new RDFTermTypeConstantImpl(termType);
    }
}
