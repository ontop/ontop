package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.type.COL_TYPE;

public class DefaultTermConstants {

    public static final ValueConstant NULL = new ValueConstantImpl("null", COL_TYPE.STRING);
    public static final ValueConstant TRUE = new ValueConstantImpl("true", COL_TYPE.BOOLEAN);
    public static final ValueConstant FALSE = new ValueConstantImpl("false", COL_TYPE.BOOLEAN);
}
