package it.unibz.inf.ontop.model.term.functionsymbol;


import it.unibz.inf.ontop.model.type.ObjectRDFType;

public interface ObjectRDFConstructionSymbol extends RDFTermConstructionSymbol {

    @Override
    ObjectRDFType getReturnedType();
}
