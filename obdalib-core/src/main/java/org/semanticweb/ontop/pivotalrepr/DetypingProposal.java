package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Variable;

/**
 * TODO: explain
 */
public interface DetypingProposal extends LocalOptimizationProposal {

    ImmutableMap<Function, Variable> getTypesToReplace();

}
