package it.unibz.inf.ontop.model.term;

/**
 * Term that is not a variable nor contains any variable.
 *
 * Is either a Constant or a variable-free functional term.
 *
 */
public interface GroundTerm extends VariableOrGroundTerm, NonVariableTerm {

    boolean isDeterministic();
}
