package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.OntopBinding;
import it.unibz.inf.ontop.model.term.Constant;

public class SimpleOntopBinding implements OntopBinding {

    private final String name;
    private Constant value;

    public SimpleOntopBinding(String name, Constant value){
        this.name = name;
        this.value = value;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Constant getValue() throws ConversionException {
        return value;
    }
}
