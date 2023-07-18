package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;

public interface Homomorphism {

    VariableOrGroundTerm apply(VariableOrGroundTerm term);

    ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression, TermFactory termFactory);

    Builder builder();

    interface Builder {

        Homomorphism build();

        Builder extend(ImmutableTerm from, ImmutableTerm to);

        Builder extend(ImmutableList<? extends ImmutableTerm> from, ImmutableList<? extends ImmutableTerm> to);

        boolean isValid();

    }
}
