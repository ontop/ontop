package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

public class DefaultTermConstants {

    public static final ValueConstant NULL = new ValueConstantImpl("null", Predicate.COL_TYPE.STRING);
    public static final ValueConstant TRUE = new ValueConstantImpl("true", Predicate.COL_TYPE.BOOLEAN);
    public static final ValueConstant FALSE = new ValueConstantImpl("false", Predicate.COL_TYPE.BOOLEAN);
}
