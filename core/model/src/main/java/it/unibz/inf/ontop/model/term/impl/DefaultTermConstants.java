package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.ValueConstant;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

public class DefaultTermConstants {

    public static final ValueConstant NULL = new ValueConstantImpl("null", TYPE_FACTORY.getXsdStringDatatype());
    public static final ValueConstant TRUE = new ValueConstantImpl("true", TYPE_FACTORY.getXsdBooleanDatatype());
    public static final ValueConstant FALSE = new ValueConstantImpl("false", TYPE_FACTORY.getXsdBooleanDatatype());
}
