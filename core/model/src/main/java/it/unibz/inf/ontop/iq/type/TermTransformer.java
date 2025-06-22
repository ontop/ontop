package it.unibz.inf.ontop.iq.type;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundTerm;

public interface TermTransformer {

    ImmutableTerm transformTerm(ImmutableTerm term, IQTree tree);

    NonGroundTerm transformNonGroundTerm(NonGroundTerm term, IQTree tree);

    ImmutableExpression transformExpression(ImmutableExpression expression, IQTree tree);

    ImmutableFunctionalTerm transformFunctionalTerm(ImmutableFunctionalTerm functionalTerm, IQTree tree);
}