package it.unibz.inf.ontop.datalog;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

public class LinearInclusionDependencies {
    
	private final ImmutableMultimap<AtomPredicate, CQIE> rules;

	public LinearInclusionDependencies(ImmutableMultimap<AtomPredicate, CQIE> predicateRuleMap) {
		rules = predicateRuleMap;
	}

	public ImmutableCollection<CQIE> getRules(Predicate pred) {

	    if (pred instanceof AtomPredicate) {
            ImmutableCollection<CQIE> rrs = rules.get((AtomPredicate)pred);
            if (rrs == null)
                return ImmutableList.of();
            return rrs;
        }
        return ImmutableList.of();
	}

    @Override
    public String toString() {
    	return rules.toString();
    }
}
