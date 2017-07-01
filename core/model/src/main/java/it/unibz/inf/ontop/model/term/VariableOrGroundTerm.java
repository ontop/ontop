package it.unibz.inf.ontop.model.term;

/**
 * Either a variable or a ground term.
 *
 * Excludes functional terms containing variables (e.g., int(x))
 *
 */
public interface VariableOrGroundTerm extends ImmutableTerm {
}
