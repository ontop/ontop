package it.unibz.inf.ontop.model.term;

public interface RDFStarTripleConstant extends RDFConstant {

    RDFConstant getSubject();

    IRIConstant getPredicate();

    RDFConstant getObject();
}
