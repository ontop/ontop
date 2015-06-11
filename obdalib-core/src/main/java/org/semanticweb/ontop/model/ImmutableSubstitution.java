package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 *
 */
public interface ImmutableSubstitution extends LocallyImmutableSubstitution {

    ImmutableMap<VariableImpl, ImmutableTerm> getImmutableMap();
}
