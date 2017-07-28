package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.OntopBinding;
import it.unibz.inf.ontop.model.MainTypeLangValues;
import it.unibz.inf.ontop.model.term.Constant;

public class SimpleOntopBinding implements OntopBinding {

    private final String name;
    private final OntopConstantConverter constantRetriever;
    private final MainTypeLangValues cell;

    public SimpleOntopBinding(String name, MainTypeLangValues cell, OntopConstantConverter constantRetriever){
        this.name = name;
        this.cell = cell;
        this.constantRetriever = constantRetriever;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Constant getValue() throws OntopResultConversionException {
        return constantRetriever.getConstantFromJDBC(cell);
    }

    @Override
    public String toString() {
        try {
            return getName() + "=" + getValue();
        } catch (OntopResultConversionException e) {
            return getName() + "=";
        }
    }
}
