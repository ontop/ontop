package it.unibz.inf.ontop.model.term.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.*;

public class ImmutabilityTools {

    public static VariableOrGroundTerm convertIntoVariableOrGroundTerm(ImmutableTerm term) {
        if (term instanceof Variable) {
            return (Variable) term;
        }
        else if (term.isGround()) {
            return (GroundTerm) term;
        }
        else {
            throw new IllegalArgumentException("Not a variable nor a ground term: " + term);
        }
    }

}
