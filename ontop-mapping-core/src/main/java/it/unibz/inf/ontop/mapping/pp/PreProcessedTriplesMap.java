package it.unibz.inf.ontop.mapping.pp;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.mapping.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.model.ImmutableFunctionalTerm;

/**
 * TODO: describe
 */
public interface PreProcessedTriplesMap {

    ImmutableList<ImmutableFunctionalTerm> getTargetAtoms();

    PPTriplesMapProvenance getProvenance(ImmutableFunctionalTerm targetAtom);
}
