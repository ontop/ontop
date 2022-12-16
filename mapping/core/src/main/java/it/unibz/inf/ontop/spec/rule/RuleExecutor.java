package it.unibz.inf.ontop.spec.rule;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

/**
 * TODO: find a better name
 */
public interface RuleExecutor {

    /**
     * The input mapping is expected to contain no more than one triple or quad assertion per class or property.
     */
    ImmutableList<MappingAssertion> apply(ImmutableList<MappingAssertion> mapping, ImmutableList<IQ> rules);
}
