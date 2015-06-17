package org.semanticweb.ontop.model;

/**
 * Either a variable or a ground term.
 *
 * Excludes functional terms containing variables (e.g., int(x))
 *
 */
public interface VariableOrGroundTerm extends ImmutableTerm {
}
