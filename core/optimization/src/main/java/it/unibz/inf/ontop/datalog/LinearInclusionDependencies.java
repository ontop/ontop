package it.unibz.inf.ontop.datalog;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.atom.AtomPredicate;

public class LinearInclusionDependencies {
    
	private final ImmutableMultimap<AtomPredicate, CQIE> rules;

	public LinearInclusionDependencies(ImmutableMultimap<AtomPredicate, CQIE> predicateRuleMap) {
		rules = predicateRuleMap;


	public ImmutableCollection<CQIE> getRules(AtomPredicate pred) {
		ImmutableCollection<CQIE> rrs = rules.get(pred);
		if (rrs == null)
			return ImmutableList.of();
		
		return rrs;
	}

    @Override
    public String toString() {
    	return rules.toString();
    }
}
