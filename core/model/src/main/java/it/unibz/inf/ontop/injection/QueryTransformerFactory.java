package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

public interface QueryTransformerFactory {

    QueryRenamer createRenamer(InjectiveSubstitution<Variable> injectiveVar2VarSubstitution);

}
