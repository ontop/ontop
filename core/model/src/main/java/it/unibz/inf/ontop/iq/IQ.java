package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface IQ {

    DistinctVariableOnlyDataAtom getProjectionAtom();

    IQTree getTree();

    VariableGenerator getVariableGenerator();

    IQ liftBinding();
}
