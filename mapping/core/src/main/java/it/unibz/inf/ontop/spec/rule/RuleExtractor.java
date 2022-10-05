package it.unibz.inf.ontop.spec.rule;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.OBDASpecInput;

public interface RuleExtractor {

    /**
     * Returns an ordered list of rules where dependencies appear before their dependents.
     * Throws an OBDASpecificationException if the dependency graph is not acyclic (must be a DAG).
     */
    ImmutableList<IQ> extract(OBDASpecInput specInput) throws OBDASpecificationException;
}
