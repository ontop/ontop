package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 *
 * For comparison, please use equals(o)
 */
public interface IQ {

    DistinctVariableOnlyDataAtom getProjectionAtom();

    IQTree getTree();

    VariableGenerator getVariableGenerator();

    IQ liftBinding();

    void validate() throws InvalidIntermediateQueryException;
}
