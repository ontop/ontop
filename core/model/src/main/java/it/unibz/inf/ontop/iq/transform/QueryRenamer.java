package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

public interface QueryRenamer {

    IQ applyInDepthRenaming(InjectiveSubstitution<Variable> renaming, IQ originalQuery);

    IQTree applyInDepthRenaming(InjectiveSubstitution<Variable> renaming, IQTree originalTree);
}
