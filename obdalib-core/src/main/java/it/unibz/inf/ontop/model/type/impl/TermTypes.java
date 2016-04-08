package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.TermType;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.*;

public class TermTypes {

    public static final TermType INTEGER_TERM_TYPE = new TermTypeImpl(INTEGER);
    public static final TermType DECIMAL_TERM_TYPE = new TermTypeImpl(DECIMAL);
    public static final TermType LITERAL_LANG_TERM_TYPE = new TermTypeImpl(LITERAL_LANG);
    public static final TermType OBJECT_TERM_TYPE = new TermTypeImpl(OBJECT);
    public static final TermType BNODE_TERM_TYPE = new TermTypeImpl(BNODE);
    public static final TermType NULL_TERM_TYPE = new TermTypeImpl(NULL);
}
