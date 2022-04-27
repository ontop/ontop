package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 *
 * For comparison, please use equals(o)
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface IQ {

    DistinctVariableOnlyDataAtom getProjectionAtom();

    IQTree getTree();

    VariableGenerator getVariableGenerator();

    IQ normalizeForOptimization();

    void validate() throws InvalidIntermediateQueryException;
}
