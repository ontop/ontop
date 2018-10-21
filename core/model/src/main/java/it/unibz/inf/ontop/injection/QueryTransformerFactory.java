package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;

public interface QueryTransformerFactory {

    QueryRenamer createRenamer(InjectiveVar2VarSubstitution injectiveVar2VarSubstitution);

}
