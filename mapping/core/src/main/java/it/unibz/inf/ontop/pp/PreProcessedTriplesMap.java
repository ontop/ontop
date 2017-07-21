package it.unibz.inf.ontop.pp;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;

/**
 * TODO: describe
 */
public interface PreProcessedTriplesMap {

    ImmutableList<ImmutableFunctionalTerm> getTargetAtoms();

    PPTriplesMapProvenance getProvenance(ImmutableFunctionalTerm targetAtom);

    /**
     * Description at the level of the triplesMap
     */
    String getTriplesMapLevelProvenanceInfo();
}
