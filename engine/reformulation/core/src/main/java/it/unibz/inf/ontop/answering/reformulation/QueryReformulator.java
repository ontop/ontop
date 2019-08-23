package it.unibz.inf.ontop.answering.reformulation;


import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.exception.OntopReformulationException;

public interface QueryReformulator {

    ExecutableQuery reformulateIntoNativeQuery(InputQuery inputQuery) throws OntopReformulationException;

    /**
     * For analysis purposes
     */
    String getRewritingRendering(InputQuery query) throws OntopReformulationException;

    InputQueryFactory getInputQueryFactory();
}
