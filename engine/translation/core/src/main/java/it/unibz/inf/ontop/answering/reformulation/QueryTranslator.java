package it.unibz.inf.ontop.answering.reformulation;


import it.unibz.inf.ontop.answering.input.InputQuery;
import it.unibz.inf.ontop.exception.OntopTranslationException;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;

public interface QueryTranslator {

    ExecutableQuery translateIntoNativeQuery(InputQuery inputQuery) throws OntopTranslationException;

    /**
     * For analysis purposes
     */
    String getRewritingRendering(InputQuery query) throws OntopTranslationException;
}
