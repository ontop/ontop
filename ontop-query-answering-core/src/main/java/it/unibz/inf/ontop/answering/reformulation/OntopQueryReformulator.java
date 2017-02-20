package it.unibz.inf.ontop.answering.reformulation;


import it.unibz.inf.ontop.answering.input.InputQuery;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;

public interface OntopQueryReformulator {

    ExecutableQuery translateIntoNativeQuery(InputQuery inputQuery) throws OntopReformulationException;

    boolean hasDistinctResultSet();

    DBMetadata getDBMetadata();

    /**
     * For analysis purposes
     */
    String getRewritingRendering(InputQuery query) throws OntopReformulationException;
}
