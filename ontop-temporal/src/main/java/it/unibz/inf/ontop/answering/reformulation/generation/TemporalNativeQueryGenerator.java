package it.unibz.inf.ontop.answering.reformulation.generation;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.iq.IntermediateQuery;

import java.io.Serializable;

public interface TemporalNativeQueryGenerator extends Serializable{
    /**
     * Translates the given datalog program into a source query, which can later
     * be evaluated by a evaluation engine.
     *
     */
    ExecutableQuery generateSourceQuery(IntermediateQuery query, ImmutableList<String> signature)
            throws OntopReformulationException;

    ExecutableQuery generateEmptyQuery(ImmutableList<String> signature);
}
