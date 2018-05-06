package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.term.Variable;

public class NullableVariableInMappingException extends OntopInternalBugException {

    protected NullableVariableInMappingException(IQ definition, ImmutableSet<Variable> nullableVariables) {
        super("The following definition projects nullable variables: " + nullableVariables
                + ".\n Definition:\n" + definition);
    }
}
