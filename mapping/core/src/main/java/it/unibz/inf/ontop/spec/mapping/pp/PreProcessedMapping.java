package it.unibz.inf.ontop.spec.mapping.pp;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;

public interface PreProcessedMapping<T extends PreProcessedTriplesMap> {

    PrefixManager getPrefixManager();

    ImmutableList<T> getTripleMaps();
}
