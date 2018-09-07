package it.unibz.inf.ontop.datalog;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class LinearInclusionDependencies {
    
	private final ImmutableMultimap<Predicate, LinearInclusionDependency> rules;

	public LinearInclusionDependencies(ImmutableList<LinearInclusionDependency> dependencies) {
        this.rules = dependencies.stream()
                .collect(ImmutableCollectors.toMultimap(
                        d -> d.getHead().getFunctionSymbol(),
                        d -> d));
	}

	public ImmutableCollection<LinearInclusionDependency> getRules(Predicate pred) {
        return rules.get(pred);
	}

    @Override
    public String toString() {
    	return rules.toString();
    }
}
