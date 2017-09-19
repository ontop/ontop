package it.unibz.inf.ontop.answering.reformulation;


import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.exception.OntopTranslationException;

public interface QueryReformulator {

    ExecutableQuery reformulateIntoNativeQuery(InputQuery inputQuery) throws OntopTranslationException;

    /**
     * For analysis purposes
     */
    String getRewritingRendering(InputQuery query) throws OntopTranslationException;
}
