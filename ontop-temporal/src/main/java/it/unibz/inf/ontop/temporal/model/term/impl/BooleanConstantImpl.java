package it.unibz.inf.ontop.temporal.model.term.impl;

import it.unibz.inf.ontop.model.term.impl.ValueConstantImpl;
import it.unibz.inf.ontop.temporal.model.term.BooleanConstant;

import javax.annotation.Nonnull;

import static it.unibz.inf.ontop.spec.impl.TestingTools.TYPE_FACTORY;

public class BooleanConstantImpl extends ValueConstantImpl implements BooleanConstant{

    public BooleanConstantImpl(@Nonnull String value){
        super(value, TYPE_FACTORY.getXsdBooleanDatatype());
    }

    public boolean getBooleanValue(){
        if (this.getValue().equals("true") || this.getValue().equals("TRUE"))
            return true;
        else if (this.getValue().equals("false") || this.getValue().equals("FALSE"))
            return false;
        else
            throw  new IllegalArgumentException("invalid boolean value " + this.getValue());
    }
}
