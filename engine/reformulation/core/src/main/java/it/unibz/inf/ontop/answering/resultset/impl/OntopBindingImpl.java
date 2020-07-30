package it.unibz.inf.ontop.answering.resultset.impl;

import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.Variable;

public class OntopBindingImpl implements OntopBinding {

    private final String name;
    private final RDFConstant constant;

    public OntopBindingImpl(Variable var, RDFConstant constant) {
        this.name = var.toString();
        this.constant = constant;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RDFConstant getValue() {
        return constant;
    }

    @Override
    public String toString() {
        return String.format("%s/%s", name, constant);
    }
}
