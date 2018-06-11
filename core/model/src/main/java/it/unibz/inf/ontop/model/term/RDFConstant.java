package it.unibz.inf.ontop.model.term;


import it.unibz.inf.ontop.model.type.RDFTermType;

public interface RDFConstant extends NonNullConstant {

    @Override
    RDFTermType getType();
}
