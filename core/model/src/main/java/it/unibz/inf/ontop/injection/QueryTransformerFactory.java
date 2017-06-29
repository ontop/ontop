package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.model.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.pivotalrepr.transform.QueryRenamer;

public interface QueryTransformerFactory {

    QueryRenamer createRenamer(InjectiveVar2VarSubstitution injectiveVar2VarSubstitution);

}
